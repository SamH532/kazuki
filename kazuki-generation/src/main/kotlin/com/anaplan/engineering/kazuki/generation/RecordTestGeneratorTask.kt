package com.anaplan.engineering.kazuki.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.random.Random
import kotlin.reflect.KClass

internal const val RecordTestPackageName = "$RootPackageName.record"

// TODO -- need capability to generate tests that include mix of compiled dependencies and source records
@CacheableTask
abstract class RecordTestGeneratorTask : DefaultTask() {

    companion object {

    }

    val generationTestSrcDir: File
        @OutputDirectory get() = project.generationTestSrcDir()


    @TaskAction
    fun apply() {
        FileSpec.builder(RecordTestPackageName, FileName).addFileComment("This file is generated -- do not edit!")
            .apply {
                addType(TypeSpec.classBuilder(FileName).apply {
                    val testRecords = addPrimitiveRecords()

                    testRecords.forEach {
                        addPrimitiveRecordTests(it)
                    }

                }.build())
            }.build().writeTo(generationTestSrcDir)
    }
}

private const val MaxSize = 10
private val RecordSizeRange = 1..MaxSize

private sealed interface TypeInfo {
    val className: String
    val generate: () -> String
}

private data class PrimitiveTypeInfo(
    val klazz: KClass<*>, override val className: String = klazz.qualifiedName!!, override val generate: () -> String
) : TypeInfo

private val intPrimitiveTypeInfo = PrimitiveTypeInfo(Int::class) { Random.nextInt().toString() }
private val stringPrimitiveTypeInfo = PrimitiveTypeInfo(String::class) {
    "\"" + (0..Random.nextInt(10)).map { Random.nextInt().toChar() }.filter { it != '"' && it != '\\' }.joinToString("") + "\""
}
private val booleanPrimitiveTypeInfo = PrimitiveTypeInfo(Boolean::class) { Random.nextBoolean().toString() }
private val doublePrimitiveTypeInfo = PrimitiveTypeInfo(Double::class) { Random.nextDouble().toString() }

private val PrimitiveTypes = listOf(
    intPrimitiveTypeInfo,
    stringPrimitiveTypeInfo,
    intPrimitiveTypeInfo,
    stringPrimitiveTypeInfo,
    booleanPrimitiveTypeInfo,
    doublePrimitiveTypeInfo,
)

private val ModuleAnnotationClassName = ClassName(RootPackageName, "Module")

private val AssertEquals = "kotlin.test.assertEquals"


private data class TestRecord(
    val name: String,
    val explicitTuple: Boolean,
    val erasedTupleType: String,
    val types: List<TypeInfo>,
    val size: Int = types.size
) {

    fun addConstruction(valName: String = "r", builder: CodeBlock.Builder): List<String> {
        val args = types.map { it.generate() }
        builder.addStatement("val $valName = ${name}_Module.mk_${name}(${args.joinToString(", ")})")
        return args
    }

}

private fun FileSpec.Builder.addRecord(
    name: String, size: Int, explicitTuple: Boolean, superInterfaces: List<TypeName>, init: TypeSpec.Builder.(List<PrimitiveTypeInfo>) -> Unit
): TestRecord {
    val types = getPrimitiveTypesForSize(size)
    val resolvedSuperInterfaces = if (explicitTuple) {
        superInterfaces + ClassName(
            RootPackageName, "Tuple$size"
        ).parameterizedBy(getPrimitiveTypesForSize(size).map { it.klazz.asClassName() })
    } else {
        superInterfaces
    }
    addType(TypeSpec.interfaceBuilder(name).apply {
        addAnnotation(ModuleAnnotationClassName)
        addSuperinterfaces(resolvedSuperInterfaces)
        init(types)
    }.build())
    return TestRecord(name, false, erasedTupleTypeForSize(size), types )
}

private fun FileSpec.Builder.addTypes(
    name: String, size: Int, superInterfaces: List<TypeName>, init: TypeSpec.Builder.(List<PrimitiveTypeInfo>) -> Unit
): List<TestRecord> {
    val result = mutableListOf<TestRecord>()
    result.add(addRecord("$name$size", size, false, superInterfaces, init))
    result.add(addRecord("${name}ExplicitTuple$size", size, true, superInterfaces, init))
    result.add(addRecord(
        "${name}InvOnlyExt$size", size, false, superInterfaces + ClassName(RecordTestPackageName, "$name$size")
    ) {
        // TODO -- add extra inv
    })
    return result
}


// TODO -- primitives, derived properties, equals, hashcode etc
private fun FileSpec.Builder.addPrimitiveRecords() = RecordSizeRange.flatMap { size ->
    mutableListOf<TestRecord>().apply {
        addAll(addTypes("PrimitiveRecord", size, emptyList()) { types ->
            types.mapIndexed { i, type ->
                addProperty("p${i + 1}", type.klazz)
            }
        })
        if (size > 1) {
            val superInterfaces = listOf(
                if (size == 2) {
                    ClassName(RecordTestPackageName, "PrimitiveRecord1")
                } else {
                    ClassName(RecordTestPackageName, "ExtendingPrimitiveRecord${size - 1}")
                }
            )
            addAll(addTypes("ExtendingPrimitiveRecord", size, superInterfaces) { types ->
                addProperty("p$size", types[size - 1].klazz)
            })
        }
    }
}

private fun erasedTupleTypeForSize(size: Int) =
    "$RootPackageName.Tuple$size<${(0 until size).joinToString(", ") { "*" }}>"

private fun tupleTypeForSize(size: Int): String {
    val types = getPrimitiveTypesForSize(size)
    return "$RootPackageName.Tuple$size<${types.joinToString(", ") { it.klazz.simpleName!! }}>"
}

private fun TypeSpec.Builder.addPrimitiveRecordTests(testRecord: TestRecord) {
    addFunction(FunSpec.builder("test${testRecord.name}Construct").apply {
        addAnnotation(testAnnotationClassName)
        addCode(CodeBlock.builder().apply {
            val args = testRecord.addConstruction(builder = this)
            (1..testRecord.size).forEach { i ->
                addStatement("$AssertEquals(r.p$i, ${args[i - 1]})")
                if (testRecord.explicitTuple) {
                    addStatement("$AssertEquals(r.p$i, r._$i)")
                } else {
                    addStatement("$AssertEquals(r.p$i, (r as ${testRecord.erasedTupleType})._$i)")
                }
            }
        }.build())
    }.build())
    addFunction(FunSpec.builder("test${testRecord.name}Deconstruct").apply {
        addAnnotation(testAnnotationClassName)
        addCode(CodeBlock.builder().apply {
            val args = testRecord.addConstruction(builder = this)
            val cast = if (testRecord.explicitTuple) "" else " as ${testRecord.erasedTupleType}"
            addStatement("val (${(1..testRecord.size).joinToString(", ") { "d$it" }}) = r$cast")
            (1..testRecord.size).forEach { i ->
                addStatement("$AssertEquals(d$i, ${args[i - 1]})")
            }
        }.build())
    }.build())

}

private fun CodeBlock.Builder.constructPrimitiveRecordExplicitTuple(
    size: Int, name: String = "r"
) = constructPrimitiveRecord(size, type = "PrimitiveRecordExplicitTuple", name)

private fun CodeBlock.Builder.constructPrimitiveRecord(
    size: Int, type: String = "PrimitiveRecord", name: String = "r"
): List<String> {
    val args = getPrimitiveTypesForSize(size).map { it.generate() }
    addStatement("val $name = $type${size}_Module.mk_$type${size}(${args.joinToString(", ")})")
    return args
}

private fun getPrimitiveTypesForSize(size: Int) = (0 until size).map { PrimitiveTypes[it % PrimitiveTypes.size] }

internal val testAnnotationClassName = ClassName("kotlin.test", "Test")


private const val FileName = "TestRecords"
