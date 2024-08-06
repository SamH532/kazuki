package com.anaplan.engineering.kazuki.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@CacheableTask
abstract class FunctionGeneratorTask : DefaultTask() {

    companion object {
        private const val MaxInputCount = 10
    }

    val generationSrcDir: File
        @OutputDirectory
        get() = project.generationSrcDir()

    @TaskAction
    fun apply() {
        FileSpec.builder(RootPackageName, FileName)
            .addFileComment("This file is generated -- do not edit!")
            .apply {
                (0..MaxInputCount).forEach {
                    addNArgFunction(it)
                }
            }
            .build()
            .writeTo(generationSrcDir)
    }
}

private const val FileName = "Functions"
private const val KotlinFunctionPackage = "kotlin"
private const val CommandPropertyName = "command"
private const val PrePropertyName = "pre"
private const val PostPropertyName = "post"
private const val MeasurePropertyName = "measure"
private const val InvocationsPropertyName = "Invocations"

fun FileSpec.Builder.addNArgFunction(argCount: Int) {
    val className = "VFunction$argCount"
    val inputTypeNames = (1..argCount).map { TypeVariableName("I$it") }
    val outputTypeName = TypeVariableName("O")
    val booleanName = Boolean::class.asClassName()
    val intName = Int::class.asClassName()
    val superInterfaceName = ClassName(KotlinFunctionPackage, "Function$argCount")
        .parameterizedBy(inputTypeNames + outputTypeName)
    val preTypeName = ClassName(KotlinFunctionPackage, "Function$argCount")
        .parameterizedBy(inputTypeNames + booleanName)
    val postTypeName = ClassName(KotlinFunctionPackage, "Function${argCount + 1}")
        .parameterizedBy(inputTypeNames + outputTypeName + booleanName)
    val measureTypeName = ClassName(KotlinFunctionPackage, "Function$argCount")
        .parameterizedBy(inputTypeNames + intName).copy(nullable = true)
    val constructor = FunSpec.constructorBuilder().apply {
        addParameter(CommandPropertyName, superInterfaceName)
        addParameter(PrePropertyName, preTypeName)
        addParameter(PostPropertyName, postTypeName)
        addParameter(MeasurePropertyName, measureTypeName)
    }.build()

    addType(TypeSpec.classBuilder(className).apply {
        addTypeVariables(inputTypeNames + outputTypeName)
        addSuperinterface(superInterfaceName)
        primaryConstructor(constructor)
        addProperty(
            PropertySpec.builder(CommandPropertyName, superInterfaceName, KModifier.PRIVATE)
                .initializer(CommandPropertyName).build()
        )
        addProperty(
            PropertySpec.builder(PrePropertyName, preTypeName).initializer(PrePropertyName).build()
        )
        addProperty(
            PropertySpec.builder(PostPropertyName, postTypeName).initializer(PostPropertyName)
                .build()
        )
        addProperty(
            PropertySpec.builder(MeasurePropertyName, measureTypeName).initializer(MeasurePropertyName)
                .build()
        )
        addType(TypeSpec.companionObjectBuilder().apply {
            val invocationsTypeName = ConcurrentHashMap::class.asClassName().parameterizedBy(
                ClassName(RootPackageName, className).parameterizedBy((0..argCount).map { STAR }),
                intName
            )
            addProperty(
                PropertySpec.builder(InvocationsPropertyName, invocationsTypeName)
                    .initializer("%T()", invocationsTypeName)
                    .build()
            )
        }.build())
        addFunction(FunSpec.builder("invoke").apply {
            addModifiers(KModifier.OVERRIDE)
            (1..argCount).forEach {
                addParameter("i$it", TypeVariableName("I$it"))
            }
            returns(outputTypeName)
            addCode(CodeBlock.builder().apply {
                val lastMeasureValName = "lastMeasure"
                val currentMeasureValName = "currentMeasure"
                val initialRecursionValName = "initialRecursion"
                val inputs = (1..argCount).joinToString(", ") { "i$it" }
                val resultValName = "result"

                addComment("TODO -- detect recursion without measure?")
                addStatement("val %N = %N[this]", lastMeasureValName, InvocationsPropertyName)
                addStatement(
                    "val %N = %N != null && %N == null",
                    initialRecursionValName,
                    MeasurePropertyName,
                    lastMeasureValName
                )

                // not thread safe - could include current thread in tracked object -- but doesn't account for MT in command
                beginControlFlow("if (%N != null)", MeasurePropertyName)
                addStatement("val %N = %N.invoke($inputs)", currentMeasureValName, MeasurePropertyName)
                beginControlFlow(
                    "if (%N != null && %N >= %N)",
                    lastMeasureValName,
                    currentMeasureValName,
                    lastMeasureValName
                )
                addStatement("%N.remove(this)", InvocationsPropertyName)
                addStatement("throw MeasureFailure()")
                endControlFlow()
                addStatement("%N.put(this, %N)", InvocationsPropertyName, currentMeasureValName)
                endControlFlow()

                beginControlFlow("try")

                addComment("TODO -- validate primitive args and result")
//              val validParams = validatePrimitive(i2) && validatePrimitive(i2)
//              if (!validParams) {
//                  throw InvariantFailure()
//              }

                beginControlFlow("if (!$PrePropertyName($inputs))")
                addStatement("throw PreconditionFailure()")
                endControlFlow()

                addStatement("val $resultValName = $CommandPropertyName($inputs)")

//                val validResult = validatePrimitive(result)
//                if (!validResult) {
//                    throw InvariantFailure()
//                }

                val postInputs = ((1..argCount).map { "i$it" } + resultValName).joinToString(", ")
                beginControlFlow("if (!$PostPropertyName($postInputs))")
                addStatement("throw PostconditionFailure()")
                endControlFlow()

                addStatement("return $resultValName")

                nextControlFlow("finally")
                beginControlFlow("if (%N)", initialRecursionValName)
                addStatement("%N.remove(this)", InvocationsPropertyName)
                endControlFlow()
                endControlFlow()
            }.build())
        }.build())
    }.build())

    addFunction(FunSpec.builder("function").apply {
        addTypeVariables(inputTypeNames + outputTypeName)
        addParameter(ParameterSpec.builder(CommandPropertyName, superInterfaceName).build())
        addParameter(ParameterSpec.builder(PrePropertyName, preTypeName).apply {
            val inputs = (1..argCount).joinToString(", ") { "_" }
            defaultValue("{ $inputs -> true }")
        }.build())
        addParameter(ParameterSpec.builder(PostPropertyName, postTypeName).apply {
            val inputs = (1..argCount + 1).joinToString(", ") { "_" }
            defaultValue("{ $inputs -> true }")
        }.build())
        addParameter(ParameterSpec.builder(MeasurePropertyName, measureTypeName).apply {
            defaultValue("null")
        }.build())
        addCode("return $className($CommandPropertyName, $PrePropertyName, $PostPropertyName, $MeasurePropertyName)")
        returns(ClassName(RootPackageName, className).parameterizedBy(inputTypeNames + outputTypeName))
    }.build())
}