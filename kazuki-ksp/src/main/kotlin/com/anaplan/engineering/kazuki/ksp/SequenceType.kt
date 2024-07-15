package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.internal._KSequence
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addSeqType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addSequenceType(interfaceClassDcl, processingState, false)

internal fun TypeSpec.Builder.addSeq1Type(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addSequenceType(interfaceClassDcl, processingState, true)

@OptIn(KspExperimental::class)
private fun TypeSpec.Builder.addSequenceType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
    requiresNonEmpty: Boolean
) {
    val interfaceName = interfaceClassDcl.simpleName.asString()
    val interfaceTypeArguments = interfaceClassDcl.typeParameters.map { it.toTypeVariableName() }
    val interfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments)
    }
    val interfaceTypeParameterResolver = interfaceClassDcl.typeParameters.toTypeParameterResolver()

    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    val functionProviderProperties = properties.filter { it.isAnnotationPresent(FunctionProvider::class) }
    if ((properties - functionProviderProperties).filter { it.isAbstract() }.firstOrNull() != null) {
        val propertyNames = properties.map { it.simpleName.asString() }.toList()
        processingState.errors.add("Sequence type $interfaceTypeName may not have properties: $propertyNames")
    }

    val superInterface = if (requiresNonEmpty) Sequence1::class else Sequence::class
    val elementTypeName = interfaceClassDcl.resolveTypeNameOfAncestorGenericParameter(superInterface, 0)
    val elementsPropertyName = "elements"
    val enforceInvariantParameterName = "enforceInvariant"
    val superListTypeName = List::class.asClassName().parameterizedBy(elementTypeName)
    val suffix = if (requiresNonEmpty) "Seq1" else "Seq"
    val implClassName = "${interfaceName}_$suffix"
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(interfaceTypeName)
        addSuperinterface(_KSequence::class.asClassName().parameterizedBy(elementTypeName, interfaceTypeName))
        addSuperinterface(superListTypeName, CodeBlock.of(elementsPropertyName))
        addSuperclassConstructorParameter(elementsPropertyName)
        primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(elementsPropertyName, superListTypeName)
            .addParameter(ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                .build())
            .build()
        )
        addProperty(
            PropertySpec.builder(elementsPropertyName, superListTypeName, KModifier.OPEN, KModifier.OVERRIDE)
                .initializer(elementsPropertyName).build()
        )
        val lenPropertyName = "len"
        addProperty(PropertySpec.builder(lenPropertyName, nat::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
            .delegate("$elementsPropertyName::size").build())
        val correspondingSetInterface = if (requiresNonEmpty) Set1::class else Set::class
        val correspondingSetConstructor = if (requiresNonEmpty) InbuiltNames.asSet1 else InbuiltNames.asSet
        addProperty(PropertySpec.builder("elems",
            correspondingSetInterface.asClassName().parameterizedBy(elementTypeName))
            .addModifiers(
                KModifier.OVERRIDE)
            .lazy("%M(%N)", correspondingSetConstructor, elementsPropertyName).build())
        addProperty(PropertySpec.builder("inds",
            correspondingSetInterface.asClassName().parameterizedBy(nat1::class.asClassName()))
            .addModifiers(
                KModifier.OVERRIDE)
            .lazy("%M(1 .. len)", correspondingSetConstructor).build())
        addFunctionProviders(functionProviderProperties, interfaceTypeParameterResolver)

        // N.B. it is important to have properties before init block
        // TODO -- should we get this from super interface -- Sequence1.atLeastOneElement()
        val additionalInvariantParts = if (requiresNonEmpty) listOf("len > 0") else emptyList()
        addInvariantFrom(interfaceClassDcl,
            false,
            enforceInvariantParameterName,
            processingState,
            additionalInvariantParts)

        addFunction(
            FunSpec.builder("construct").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(elementsPropertyName, superListTypeName)
                returns(interfaceTypeName)
                addStatement("return %N(%N)", implClassName, elementsPropertyName)
            }.build()
        )
        addFunction(
            FunSpec.builder("get").apply {
                val indexParameterName = "index"
                addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                addParameter(ParameterSpec.builder(indexParameterName, nat1::class.asTypeName()).build())
                returns(elementTypeName)
                addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (%N < 1 || %N > %N)", indexParameterName, indexParameterName, lenPropertyName)
                    addStatement("throw %T()", PreconditionFailure::class)
                    endControlFlow()
                    addStatement("return %N.get(%N - 1)", elementsPropertyName, indexParameterName)
                }.build())
            }.build()
        )
        addFunction(
            FunSpec.builder("indexOf").apply {
                val elementParameterName = "element"
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec.builder(elementParameterName, elementTypeName).build())
                returns(nat1::class.asTypeName())
                addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (%N !in %N)", elementParameterName, elementsPropertyName)
                    addStatement("throw %T()", PreconditionFailure::class)
                    endControlFlow()
                    addStatement("return %N.indexOf(%N) + 1", elementsPropertyName, elementParameterName)
                }.build())
            }.build()
        )
        addFunction(
            FunSpec.builder("lastIndexOf").apply {
                val elementParameterName = "element"
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec.builder(elementParameterName, elementTypeName).build())
                returns(nat1::class.asTypeName())
                addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (%N !in %N)", elementParameterName, elementsPropertyName)
                    addStatement("throw %T()", PreconditionFailure::class)
                    endControlFlow()
                    addStatement("return %N.lastIndexOf(%N) + 1", elementsPropertyName, elementParameterName)
                }.build())
            }.build()
        )
        addFunction(FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return \"%N\$%N\"", interfaceName, elementsPropertyName)
            .build())
        addFunction(FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE)
            .returns(Int::class).addStatement("return %N.hashCode()", elementsPropertyName).build())
        val equalsParameterName = "other"
        addFunction(FunSpec.builder("equals").addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec.builder(equalsParameterName, Any::class.asTypeName().copy(nullable = true))
                .build())
            .returns(Boolean::class).addCode(CodeBlock.builder().apply {
                beginControlFlow("if (this === %N)", equalsParameterName)
                addStatement("return true")
                endControlFlow()

                beginControlFlow("if (%N !is %T)", equalsParameterName, superInterface.asTypeName().parameterizedBy(
                    STAR))
                addStatement("return false")
                endControlFlow()

                addStatement("return %N == %N", elementsPropertyName, equalsParameterName)

            }.build()).build())
    }.build()
    addType(implTypeSpec)

    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(elementsPropertyName, superListTypeName)
            returns(interfaceTypeName)
            addStatement("return %N(%N)", implTypeSpec, elementsPropertyName)
        }.build()
    )
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(elementsPropertyName, elementTypeName, KModifier.VARARG)
            returns(interfaceTypeName)
            addStatement("return %N(%N.toList())", implTypeSpec, elementsPropertyName)
        }.build()
    )
    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            val implTypeArgs = if (interfaceTypeArguments.isEmpty()) {
                ""
            } else {
                "<" + interfaceTypeArguments.joinToString(", ") + ">"
            }
            addParameter(elementsPropertyName, superListTypeName)
            returns(Boolean::class)
            addStatement("return %N$implTypeArgs(%N, false).%N()", implClassName, elementsPropertyName, validityFunctionName)
        }.build()
    )
    addFunction(
        FunSpec.builder("as_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(elementsPropertyName, superListTypeName)
            returns(interfaceTypeName)
            addStatement("return %N(%N)", "mk_$interfaceName", elementsPropertyName)
        }.build()
    )
}


