@file:OptIn(KspExperimental::class)

package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.ComparableProperty
import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.internal._Record
import com.anaplan.engineering.kazuki.ksp.InbuiltNames.coreInternalPackage
import com.anaplan.engineering.kazuki.ksp.InbuiltNames.corePackage
import com.anaplan.engineering.kazuki.ksp.findUnusedGenericName
import com.anaplan.engineering.kazuki.ksp.superModules
import com.anaplan.engineering.kazuki.ksp.type.property.PropertyProcessor
import com.anaplan.engineering.kazuki.ksp.type.property.addFunctionProviders
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addRecordType(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) {
    // TODO -- fail if class·is·not interface
    val interfaceType = interfaceClassDcl.asType(emptyList())
    val interfaceTypeArguments = interfaceClassDcl.typeParameters.map { it.toTypeVariableName() }
    val interfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments)
    }

    val properties = PropertyProcessor(interfaceClassDcl, typeGenerationContext, allowFields = true).process()

    val tupleComponents = properties.tupleComponents
    if (tupleComponents.isEmpty()) {
        throw IllegalStateException("Record $interfaceTypeName must have fields")
    }
    val tupleClassName = ClassName(corePackage, "Tuple${tupleComponents.size}")
    val tupleType = tupleClassName.parameterizedBy(
        tupleComponents.map { it.typeName }
    )
    val internalTupleClassName = ClassName(coreInternalPackage, "_Tuple${tupleComponents.size}")
    val internalTupleType = internalTupleClassName.parameterizedBy(
        tupleComponents.map { it.typeName } + interfaceTypeName
    )
    val erasedTupleType = tupleClassName.parameterizedBy(
        tupleComponents.map { STAR }
    )
    val compatibleSuperTypes =
        (interfaceClassDcl.superModules.map {
            it.resolve().starProjection().toTypeName()
        } + interfaceType.starProjection().toTypeName())

    val interfaceName = interfaceClassDcl.simpleName.asString()
    val implClassName = "${interfaceName}_Rec"
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE, KModifier.DATA)
        addAnnotation(AnnotationSpec.builder(_Record::class).apply {
            addMember(tupleComponents.joinToString(", ") { "\"" + it.name + "\"" })
        }.build())
        addSuperinterface(interfaceTypeName)
        addSuperinterface(internalTupleType)
        primaryConstructor(FunSpec.constructorBuilder().apply {
            tupleComponents.forEach { tc -> addParameter(tc.name, tc.typeName) }
            addParameter(
                ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                    .build()
            )
        }.build())

        tupleComponents.forEach { tc ->
            addProperty(
                PropertySpec.builder(
                    tc.name,
                    tc.typeName,
                    KModifier.OVERRIDE,
                ).initializer(tc.name)
                    .build()
            )
            addProperty(
                PropertySpec.builder(
                    "_${tc.index}",
                    tc.typeName,
                    KModifier.OVERRIDE
                ).initializer(tc.name)
                    .build()
            )
        }

        (1..tupleComponents.size).forEach { conNary ->
            addFunction(FunSpec.builder(constructFunctionName).apply {
                addModifiers(KModifier.OVERRIDE)
                (1..conNary).forEach {
                    val tc = tupleComponents[it - 1]
                    addParameter("t${tc.index}", tc.typeName)
                }
                returns(interfaceTypeName)
                val params =
                    (1..conNary).map { "t${tupleComponents[it - 1].index}" } + (conNary + 1..tupleComponents.size).map { "_$it" }
                if (makeable) {
                    addStatement("return %N(${params.joinToString(",")})", implClassName)
                } else {
                    addStatement("throw UnsupportedOperationException()")
                }
            }.build())
        }

        addProperty(
            PropertySpec.builder(enforceInvariantParameterName, Boolean::class, KModifier.PRIVATE).initializer(
                enforceInvariantParameterName
            ).build()
        )
        addFunctionProviders(properties.functionProviders, makeable, typeGenerationContext)

        val comparableWith = addComparableWith(interfaceClassDcl, tupleClassName, typeGenerationContext)

        // N.B. it·is·important to have properties before init block
        addInvariantFrom(interfaceClassDcl, typeGenerationContext)

        addFunction(
            FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
                .returns(String::class).addCode(CodeBlock.builder().apply {
                    beginControlFlow("val sb = %T().apply", StringBuilder::class)
                    addStatement("append(\"%N\")", interfaceType.declaration.simpleName.asString())
                    val useComparableForOutput =
                        comparableWith.property?.getAnnotationsByType(ComparableProperty::class)?.single()?.useForOutput
                    if (useComparableForOutput == true) {
                        addStatement("append(%P)", "\$${comparableWith.property.simpleName.asString()}")
                    } else {
                        addStatement("append(%S)", "(")
                        tupleComponents.dropLast(1).forEach {
                            val propertyName = it.name
                            addStatement("append(%P)", "$propertyName=\$$propertyName, ")
                        }
                        val lastPropertyName = tupleComponents.last().name
                        addStatement("append(%P)", "$lastPropertyName=\$$lastPropertyName")
                        addStatement("append(%S)", ")")
                    }
                    endControlFlow()
                    addStatement("return sb.toString()")
                }.build()).build()
        )

        addFunction(
            FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE)
                .returns(Int::class).apply {
                    if (comparableWith.property == null) {
                        addStatement("return ${corePackage}.mk_(${tupleComponents.joinToString(", ") { "_${it.index}" }}).hashCode()")
                    } else {
                        addStatement("return %N.hashCode()", comparableWith.property.simpleName.getShortName())
                    }
                }.build()
        )

        addFunction(
            FunSpec.builder("equals").addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    ParameterSpec.builder(otherParameterName, Any::class.asTypeName().copy(nullable = true))
                        .build()
                )
                .returns(Boolean::class).addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (this === %N)", otherParameterName)
                    addStatement("return true")
                    endControlFlow()

                    beginControlFlow("if (null == %N)", otherParameterName)
                    addStatement("return false")
                    endControlFlow()

                    val erasedInternalTupleType = internalTupleClassName.parameterizedBy(
                        tupleComponents.map { STAR } + STAR
                    )
                    beginControlFlow(
                        "if (%N !is %T)",
                        otherParameterName,
                        erasedInternalTupleType
                    )
                    addStatement("return false")
                    endControlFlow()

                    beginControlFlow(
                        "if (!(%N.%N.isInstance(this) && this.%N.isInstance(%N)))",
                        otherParameterName,
                        comparableWithPropertyName,
                        comparableWithPropertyName,
                        otherParameterName
                    )
                    addStatement("return false")
                    endControlFlow()

                    if (comparableWith.property == null) {
                        addStatement("return ${tupleComponents.joinToString(" && ") { "_${it.index} == $otherParameterName._${it.index}" }}")
                    } else {
                        val comparablePropertyName = comparableWith.property.simpleName.getShortName()
                        addStatement(
                            "return this.%N == (%N as %T).%N",
                            comparablePropertyName,
                            otherParameterName,
                            comparableWith.comparableTypeLimitTypeName,
                            comparablePropertyName
                        )
                    }
                }.build()).build()
        )

        addType(TypeSpec.companionObjectBuilder().apply {
            addFunction(
                FunSpec.builder(isRelatedFunctionName).apply {
                    addParameter(otherParameterName, Any::class)
                    returns(Boolean::class)
                    addModifiers(KModifier.INTERNAL)
                    addStatement(
                        "return ${compatibleSuperTypes.joinToString(" || ") { "%N·is·%T" }}",
                        *compatibleSuperTypes.flatMap { listOf(otherParameterName, it) }.toTypedArray()
                    )
                }.build()
            )
        }.build())

    }.build()
    addType(implTypeSpec)

    addFunction(FunSpec.builder("as_Tuple").apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        receiver(interfaceTypeName)
        returns(tupleType)
        addCode(CodeBlock.builder().apply {
            beginControlFlow("if (this·is·%T)", erasedTupleType)
            addStatement("return this as %T", tupleType)
            nextControlFlow("else")
            addStatement(
                "throw %T(%S)",
                PreconditionFailure::class.asClassName(),
                "Cannot convert instance of $interfaceName created outside Kazuki"
            )
            endControlFlow()
        }.build())
    }.build()).build()

    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(otherParameterName, Any::class)
            returns(Boolean::class)
            addCode(CodeBlock.builder().apply {
                beginControlFlow("if (%N·!is·%T)", otherParameterName, erasedTupleType)
                addStatement("return false")
                endControlFlow()

                beginControlFlow("if (!$implClassName.$isRelatedFunctionName($otherParameterName))")
                addStatement("return false")
                endControlFlow()

                tupleComponents.forEach { tc ->
                    val type = tc.typeReference.resolve()
                    if (type.declaration !is KSTypeParameter) {
                        beginControlFlow(
                            "if (%N._${tc.index}·!is·%T)",
                            otherParameterName,
                            type.starProjection().toTypeName()
                        )
                        addStatement("return false")
                        endControlFlow()
                    }
                }

                val implTypeArgs = if (interfaceTypeArguments.isEmpty()) {
                    ""
                } else {
                    "<" + interfaceTypeArguments.joinToString(", ") + ">"
                }
                addStatement(
                    "return %N$implTypeArgs(${tupleComponents.joinToString { "%N.%N as %T" }}, false).%N()",
                    implClassName,
                    *tupleComponents.flatMap { listOf(otherParameterName, "_${it.index}", it.typeName) }.toTypedArray(),
                    validityFunctionName
                )
            }.build())
        }.build()
    )

    if (makeable) {
        // TODO -- optimization -- if already this type just return it
        addFunction(
            FunSpec.builder("as_$interfaceName").apply {
                if (interfaceTypeArguments.isNotEmpty()) {
                    addTypeVariables(interfaceTypeArguments)
                }
                addModifiers(KModifier.PRIVATE)
                addParameter(otherParameterName, tupleType)
                returns(interfaceTypeName)
                addCode(CodeBlock.builder().apply {
                    addStatement(
                        "return %N(${tupleComponents.joinToString { "%N.%N" }})",
                        implClassName,
                        *tupleComponents.flatMap { listOf(otherParameterName, "_${it.index}") }.toTypedArray()
                    )
                }.build())
            }.build()
        )

        addFunction(
            FunSpec.builder("as_$interfaceName").apply {
                if (interfaceTypeArguments.isNotEmpty()) {
                    addTypeVariables(interfaceTypeArguments)
                }
                addParameter(otherParameterName, Any::class.asClassName())
                returns(interfaceTypeName)
                addCode(CodeBlock.builder().apply {
                    val typeArgs = if (interfaceTypeArguments.isEmpty()) {
                        ""
                    } else {
                        "<${interfaceTypeArguments.joinToString { "$it" }}>"
                    }
                    beginControlFlow("if (!is_$interfaceName$typeArgs($otherParameterName))")
                    // TODO -- want to print value of other
                    addStatement(
                        "throw %T(%P)",
                        PreconditionFailure::class.asClassName(),
                        "\$$otherParameterName is not a $interfaceName"
                    )
                    nextControlFlow("else")
                    addStatement("return as_$interfaceName($otherParameterName as %T)", tupleType)
                    endControlFlow()
                }.build())
            }.build()
        )

        tupleComponents.forEach { tc ->
            addFunction(
                FunSpec.builder("component${tc.index}").apply {
                    if (interfaceTypeArguments.isNotEmpty()) {
                        addTypeVariables(interfaceTypeArguments)
                    }
                    receiver(interfaceTypeName)
                    addModifiers(KModifier.OPERATOR)
                    returns(tc.typeName)
                    addStatement("return this.%N", tc.name)
                }.build()
            )
        }

        addFunction(
            FunSpec.builder("set").apply {
                val t =
                    TypeVariableName(findUnusedGenericName(interfaceTypeArguments), bounds = listOf(interfaceTypeName))
                addTypeVariables(interfaceTypeArguments + t)
                receiver(t)
                tupleComponents.forEach { tc ->
                    addParameter(ParameterSpec.builder(tc.name, tc.typeName).apply {
                        defaultValue("this.%N", tc.name)
                    }.build())
                }
                returns(t)
                addCode(CodeBlock.builder().apply {
                    val constructableClassName = ClassName(
                        coreInternalPackage,
                        "_Constructable${tupleComponents.size}"
                    )
                    val constructableTypeName =
                        constructableClassName.parameterizedBy(tupleComponents.map { it.typeName } + t)
                    val erasedConstructableTypeName =
                        constructableClassName.parameterizedBy(tupleComponents.map { STAR } + STAR)


                    beginControlFlow("if (this·is·%T)", erasedConstructableTypeName)
                    addStatement(
                        "return (this·as·%T).construct(${tupleComponents.joinToString { "%N" }})",
                        constructableTypeName,
                        *tupleComponents.map { it.name }.toTypedArray()
                    )
                    nextControlFlow("else")
                    addStatement(
                        "throw %T(%S)",
                        PreconditionFailure::class.asClassName(),
                        "Cannot set on instance of $interfaceName created outside Kazuki"
                    )
                    endControlFlow()
                }.build())
            }.build()
        )

        addFunction(
            FunSpec.builder("mk_$interfaceName").apply {
                if (interfaceTypeArguments.isNotEmpty()) {
                    addTypeVariables(interfaceTypeArguments)
                }
                tupleComponents.forEach { tc -> addParameter(tc.name, tc.typeName) }
                returns(interfaceTypeName)
                addStatement(
                    "return %N(${tupleComponents.joinToString { "%N" }})",
                    implTypeSpec,
                    *tupleComponents.map { it.name }.toTypedArray()
                )
            }.build()
        )
    }
}

private const val otherParameterName = "other"
private const val isRelatedFunctionName = "isRelated"
private const val constructFunctionName = "construct"
