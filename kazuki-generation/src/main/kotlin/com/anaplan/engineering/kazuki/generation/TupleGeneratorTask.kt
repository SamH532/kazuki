package com.anaplan.engineering.kazuki.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.reflect.KClass


// add internal tuple interface and add comparable
// have records use internal tuple interface
// split into 2 files ..

@CacheableTask
abstract class TupleGeneratorTask : DefaultTask() {

    companion object {
        private const val MaxNary = 10
    }

    val generationSrcDir: File
        @OutputDirectory
        get() = project.generationSrcDir()

    @TaskAction
    fun apply() {
        FileSpec.builder(PackageName, FileName)
            .addFileComment("This file is generated -- do not edit!")
            .apply {
                (1..MaxNary).forEach {
                    addNAryTuple(it)
                }
            }
            .build()
            .writeTo(generationSrcDir)
        FileSpec.builder(InternalPackageName, FileName)
            .addFileComment("This file is generated -- do not edit!")
            .apply {
                (1..MaxNary).forEach {
                    addNAryTupleInternal(it)
                }
            }
            .build()
            .writeTo(generationSrcDir)
    }
}

private const val FileName = "Tuples"

private fun publicInterfaceName(nary: Int) = "Tuple$nary"
private fun internalInterfaceName(nary: Int) = "_${publicInterfaceName(nary)}"
private fun internalDataClassName(nary: Int) = "_${internalInterfaceName(nary)}"

fun FileSpec.Builder.addNAryTuple(nary: Int) {
    val interfaceName = publicInterfaceName(nary)
    val className = internalDataClassName(nary)
    val typeNames = (1..nary).map { TypeVariableName("T$it") }

    addType(TypeSpec.interfaceBuilder(interfaceName).apply {
        addTypeVariables(typeNames)
        (1..nary).forEach {
            addProperty(PropertySpec.builder("_$it", TypeVariableName("T$it")).build())
            addFunction(FunSpec.builder("component$it").addModifiers(KModifier.OPERATOR).addStatement("return _$it").returns(TypeVariableName("T$it")).build())        }
    }.build())

    addFunction(FunSpec.builder("mk_").apply {
        addTypeVariables(typeNames)
        (1..nary).forEach {
            addParameter("_$it", TypeVariableName("T$it"))
        }
        addCode("return $InternalPackageName.$className(${(1..nary).joinToString(", ") { "_$it" }})")
        returns(ClassName(PackageName, interfaceName).parameterizedBy(typeNames))
    }.build())
}

// No obvious way to share with core
private const val comparableWithPropertyName = "comparableWith"
private val comparableWithTypeName = KClass::class.asTypeName().parameterizedBy(STAR)

fun FileSpec.Builder.addNAryTupleInternal(nary: Int) {
    val internalInterfaceName = internalInterfaceName(nary)
    val className = internalDataClassName(nary)
    val typeNames = (1..nary).map { TypeVariableName("T$it") }
    val constructor = FunSpec.constructorBuilder().apply {
        (1..nary).forEach {
            addParameter("_$it", TypeVariableName("T$it"))
        }
    }.build()

    val publicInterfaceName = publicInterfaceName(nary)
    val publicInterfaceClassName = ClassName(PackageName, publicInterfaceName)
    addType(TypeSpec.interfaceBuilder(internalInterfaceName).apply {
        addTypeVariables(typeNames)
        addSuperinterface(publicInterfaceClassName.parameterizedBy(typeNames))
        addProperty(
            PropertySpec.builder(comparableWithPropertyName, comparableWithTypeName, KModifier.OPEN).build()
        )
    }.build())

    addType(TypeSpec.classBuilder(className).apply {
        addModifiers(KModifier.DATA, KModifier.INTERNAL)
        addTypeVariables(typeNames)
        addSuperinterface(ClassName(InternalPackageName, internalInterfaceName).parameterizedBy(typeNames))
        primaryConstructor(constructor)
        (1..nary).forEach {
            addProperty(
                PropertySpec.builder("_$it", TypeVariableName("T$it")).addModifiers(KModifier.OVERRIDE)
                    .initializer("_$it").build()
            )
        }
        addProperty(
            PropertySpec.builder(comparableWithPropertyName, comparableWithTypeName, KModifier.OPEN, KModifier.OVERRIDE)
                .initializer(CodeBlock.of("$publicInterfaceName::class")).build()
        )
        // TODO -- toString
    }.build())

}