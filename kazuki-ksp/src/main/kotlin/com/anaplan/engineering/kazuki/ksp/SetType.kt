package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Set1
import com.anaplan.engineering.kazuki.core.internal._KSet
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addSetType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addSetType(interfaceClassDcl, processingState, false)

internal fun TypeSpec.Builder.addSet1Type(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addSetType(interfaceClassDcl, processingState, true)

@OptIn(KspExperimental::class)
private fun TypeSpec.Builder.addSetType(
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
    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    val functionProviderProperties = getFunctionProviderProperties(interfaceClassDcl, processingState)
    if ((properties - functionProviderProperties.map { it.property }).filter { it.isAbstract() }
            .firstOrNull() != null) {
        val propertyNames = properties.map { it.simpleName.asString() }.toList()
        processingState.errors.add("Set type $interfaceTypeName may not have properties: $propertyNames")
    }

    val superInterface = if (requiresNonEmpty) Set1::class else Set::class
    val elementTypeName = interfaceClassDcl.resolveTypeNameOfAncestorGenericParameter(superInterface.qualifiedName!!, 0)
    val elementsPropertyName = "elements"
    val superSetTypeName = Set::class.asClassName().parameterizedBy(elementTypeName)
    val suffix = if (requiresNonEmpty) "Set1" else "Set"
    val implClassName = "${interfaceName}_$suffix"
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(interfaceTypeName)
        addSuperinterface(_KSet::class.asClassName().parameterizedBy(elementTypeName, interfaceTypeName))
        addSuperinterface(superSetTypeName, CodeBlock.of(elementsPropertyName))
        addSuperclassConstructorParameter(elementsPropertyName)
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(elementsPropertyName, superSetTypeName)
                .addParameter(
                    ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                        .build()
                )
                .build()
        )
        addProperty(
            PropertySpec.builder(elementsPropertyName, superSetTypeName, KModifier.OPEN, KModifier.OVERRIDE)
                .initializer(elementsPropertyName).build()
        )
        val comparableWith = addComparableWith(interfaceClassDcl, Set::class.asClassName(), processingState)
        addFunctionProviders(functionProviderProperties, processingState)

        // N.B. it is important to have properties before init block
        val additionalInvariantParts = if (requiresNonEmpty) {
            listOf(FreeformInvariant("atLeastOneElement", "::atLeastOneElement"))
        } else {
            emptyList()
        }
        addInvariantFrom(
            interfaceClassDcl,
            processingState,
            additionalInvariantParts
        )

        addFunction(
            FunSpec.builder("construct").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(elementsPropertyName, superSetTypeName)
                returns(interfaceTypeName)
                addStatement("return %N(%N)", implClassName, elementsPropertyName)
            }.build()
        )
        addFunction(
            FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
                .returns(String::class)
                .addStatement("return \"%N\$%N\"", interfaceName, elementsPropertyName)
                .build()
        )
        addFunction(
            FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE)
                .returns(Int::class).apply {
                    val hashPropertyName = if (comparableWith.property == null) {
                        elementsPropertyName
                    } else {
                        comparableWith.property.simpleName.getShortName()
                    }
                    addStatement("return %N.hashCode()", hashPropertyName)
                }.build()
        )
        val equalsParameterName = "other"
        addFunction(
            FunSpec.builder("equals").addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    ParameterSpec.builder(equalsParameterName, Any::class.asTypeName().copy(nullable = true))
                        .build()
                )
                .returns(Boolean::class).addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (this === %N)", equalsParameterName)
                    addStatement("return true")
                    endControlFlow()

                    beginControlFlow(
                        "if (%N !is %T)",
                        equalsParameterName,
                        _KSet::class.asClassName().parameterizedBy(STAR, STAR)
                    )
                    addStatement("return false")
                    endControlFlow()

                    beginControlFlow(
                        "if (!(%N.%N.isInstance(this) && this.%N.isInstance(%N)))",
                        equalsParameterName,
                        comparableWithPropertyName,
                        comparableWithPropertyName,
                        equalsParameterName
                    )
                    addStatement("return false")
                    endControlFlow()

                    if (comparableWith.property == null) {
                        addStatement("return %N == %N", elementsPropertyName, equalsParameterName)
                    } else {
                        val comparablePropertyName = comparableWith.property.simpleName.getShortName()
                        addStatement(
                            "return this.%N == (%N as %T).%N",
                            comparablePropertyName,
                            equalsParameterName,
                            comparableWith.className,
                            comparablePropertyName
                        )
                    }

                }.build()).build()
        )
    }.build()
    addType(implTypeSpec)

    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(elementsPropertyName, superSetTypeName)
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
            addStatement("return %N(%N.toSet())", implTypeSpec, elementsPropertyName)
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
            addParameter(elementsPropertyName, superSetTypeName)
            returns(Boolean::class)
            addStatement(
                "return %N$implTypeArgs(%N, false).%N()",
                implClassName,
                elementsPropertyName,
                validityFunctionName
            )
        }.build()
    )
    addFunction(
        FunSpec.builder("as_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(elementsPropertyName, superSetTypeName)
            returns(interfaceTypeName)
            addStatement("return %N(%N)", "mk_$interfaceName", elementsPropertyName)
        }.build()
    )
}
