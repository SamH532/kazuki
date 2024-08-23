package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.internal._KInjectiveMapping
import com.anaplan.engineering.kazuki.core.internal._KMapping
import com.anaplan.engineering.kazuki.core.internal._KRelation
import com.anaplan.engineering.kazuki.ksp.InbuiltNames
import com.anaplan.engineering.kazuki.ksp.resolveAncestorTypeParameterNames
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addMappingType(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) = if (makeable) {
    addMappingType(interfaceClassDcl, typeGenerationContext, false, false)
} else {
    // TODO -- is_ / metadata?
}

internal fun TypeSpec.Builder.addMapping1Type(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) = if (makeable) {
    addMappingType(interfaceClassDcl, typeGenerationContext, true, false)
} else {
    // TODO -- is_ / metadata?
}


internal fun TypeSpec.Builder.addInjectiveMappingType(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) = if (makeable) {
    addMappingType(interfaceClassDcl, typeGenerationContext, false, true)
} else {
    // TODO -- is_ / metadata?
}


internal fun TypeSpec.Builder.addInjectiveMapping1Type(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) = if (makeable) {
    addMappingType(interfaceClassDcl, typeGenerationContext, true, true)
} else {
    // TODO -- is_ / metadata?
}


@OptIn(KspExperimental::class)
private fun TypeSpec.Builder.addMappingType(
    interfaceClassDcl: KSClassDeclaration,
    typeGenerationContext: TypeGenerationContext,
    requiresNonEmpty: Boolean,
    injective: Boolean,
) {
    val interfaceName = interfaceClassDcl.simpleName.asString()
    val interfaceTypeArguments = interfaceClassDcl.typeParameters.map { it.toTypeVariableName() }
    val interfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments)
    }
    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    val functionProviderProperties = getFunctionProviderProperties(interfaceClassDcl, typeGenerationContext)
    if ((properties - functionProviderProperties.map { it.property }).filter { it.isAbstract() }
            .firstOrNull() != null) {
        val propertyNames = properties.map { it.simpleName.asString() }.toList()
        typeGenerationContext.errors.add("Mapping type $interfaceTypeName may not have properties: $propertyNames")
    }

    val superInterface = if (injective) {
        if (requiresNonEmpty) InjectiveMapping1::class else InjectiveMapping::class
    } else {
        if (requiresNonEmpty) Mapping1::class else Mapping::class
    }
    val mappingType =
        interfaceClassDcl.superTypes.single { it.resolve().declaration.qualifiedName?.asString() == superInterface.qualifiedName }
            .resolve()
    val ancestorTypeParameters = interfaceClassDcl.resolveAncestorTypeParameterNames(superInterface.qualifiedName!!)
    val domainTypeName = ancestorTypeParameters.getTypeName(0)
    val rangeTypeName = ancestorTypeParameters.getTypeName(1)
    val baseMapPropertyName = "baseMap"
    val baseSetPropertyName = "baseSet"
    val superMappingTypeName = mappingType.toClassName().parameterizedBy(domainTypeName, rangeTypeName)
    val suffix = if (requiresNonEmpty) "Mapping1" else "Mapping"
    val implClassName = "${interfaceName}_$suffix"
    val mapType = Map::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName)
    val tupleType = Tuple2::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName)
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(interfaceTypeName)
        val mappingClass = if (injective) _KInjectiveMapping::class else _KMapping::class
        addSuperinterface(
            mappingClass.asClassName().parameterizedBy(domainTypeName, rangeTypeName, interfaceTypeName)
        )
        addSuperclassConstructorParameter(baseMapPropertyName)
        primaryConstructor(
            FunSpec.constructorBuilder().addParameter(baseMapPropertyName, mapType).addParameter(
                    ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true").build()
                ).build()
        )
        addProperty(
            PropertySpec.builder(baseMapPropertyName, mapType, KModifier.OVERRIDE).initializer(baseMapPropertyName)
                .build()
        )
        val setType = if (requiresNonEmpty) Set1::class else Set::class
        val setFunction = if (requiresNonEmpty) InbuiltNames.asSet1 else InbuiltNames.asSet
        addProperty(
            PropertySpec.builder("dom", setType.asClassName().parameterizedBy(domainTypeName), KModifier.OVERRIDE)
                .delegate(CodeBlock.builder().apply {
                    beginControlFlow("lazy")
                    addStatement("%M(%N.keys)", setFunction, baseMapPropertyName)
                    endControlFlow()
                }.build()).build()
        )
        addProperty(
            PropertySpec.builder("rng", setType.asClassName().parameterizedBy(rangeTypeName), KModifier.OVERRIDE)
                .delegate(CodeBlock.builder().apply {
                    beginControlFlow("lazy")
                    addStatement("%M(%N.values)", setFunction, baseMapPropertyName)
                    endControlFlow()
                }.build()).build()
        )
        addProperty(
            PropertySpec.builder("size", Int::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
                .delegate("$baseMapPropertyName::size").build()
        )
        if (requiresNonEmpty) {
            addProperty(
                PropertySpec.builder("card", nat1::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
                    .delegate("$baseMapPropertyName::size").build()
            )
        }
        // TODO -- inverse should be Mapping1 for IM1
        if (injective) {
            addProperty(
                PropertySpec.builder(
                    "inverse", Mapping::class.asTypeName().parameterizedBy(rangeTypeName, domainTypeName)
                ).addModifiers(KModifier.OVERRIDE).delegate(
                        CodeBlock.builder().apply {
                            beginControlFlow("lazy")
                            addStatement("${InbuiltNames.corePackage}.as_Mapping($baseSetPropertyName.map { (d, r) -> mk_(r, d) })")
                            endControlFlow()
                        }.build()
                    ).build()
            )
        }
        // TODO -- should this be Set? -- need _KRelation to extened _KSet if so
        val comparableWith = addComparableWith(interfaceClassDcl, Relation::class.asClassName(), typeGenerationContext)
        addFunctionProviders(functionProviderProperties, true, typeGenerationContext)

        // N.B. it is important to have properties before init block
        val additionalInvariantParts = if (requiresNonEmpty) {
            listOf(FreeformInvariant("nonEmpty", "{ card > 0 }"))
        } else {
            emptyList()
        }
        // TODO -- should we get this from super interface -- Sequence1.atLeastOneElement()
        addInvariantFrom(
            interfaceClassDcl, typeGenerationContext, additionalInvariantParts
        )

        addFunction(
            FunSpec.builder("get").apply {
                val dParameterName = "d"
                addModifiers(KModifier.OVERRIDE)
                addParameter(dParameterName, domainTypeName)
                returns(rangeTypeName)
                addStatement(
                    "return %N[%N] ?: throw %T(%P)",
                    baseMapPropertyName,
                    dParameterName,
                    PreconditionFailure::class,
                    "\${$dParameterName} not in mapping domain"
                )
            }.build()
        )
        addFunction(
            FunSpec.builder("contains").apply {
                val elementParameterName = "element"
                addModifiers(KModifier.OVERRIDE)
                addParameter(elementParameterName, tupleType)
                returns(Boolean::class)
                addStatement(
                    "return %N[%N._1]·==·%N._2",
                    baseMapPropertyName,
                    elementParameterName,
                    elementParameterName,
                )
            }.build()
        )
        addFunction(
            FunSpec.builder("containsAll").apply {
                val elementsParameterName = "elements"
                addModifiers(KModifier.OVERRIDE)
                addParameter(elementsParameterName, Collection::class.asClassName().parameterizedBy(tupleType))
                returns(Boolean::class)
                addStatement(
                    "return %M(%N)·{ contains(it) }",
                    InbuiltNames.forall,
                    elementsParameterName,
                )
            }.build()
        )
        addFunction(
            FunSpec.builder("construct").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(
                    baseMapPropertyName, mapType
                )
                returns(interfaceTypeName)
                addStatement("return %N(%N)", implClassName, baseMapPropertyName)
            }.build()
        )
        addFunction(
            FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE).returns(String::class)
                .addStatement("return \"%N\$%N\"", interfaceName, baseMapPropertyName).build()
        )
        addFunction(
            FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE).returns(Int::class).apply {
                    val hashPropertyName = if (comparableWith.property == null) {
                        baseSetPropertyName
                    } else {
                        comparableWith.property.simpleName.getShortName()
                    }
                    addStatement("return %N.hashCode()", hashPropertyName)
                }.build()
        )
        addFunction(
            FunSpec.builder("isEmpty").addModifiers(KModifier.OVERRIDE).returns(Boolean::class)
                .addStatement("return %N.isEmpty()", baseMapPropertyName).build()
        )
        addFunction(
            FunSpec.builder("iterator").addModifiers(KModifier.OVERRIDE).returns(
                    Iterator::class.asClassName().parameterizedBy(tupleType)
                ).addStatement("return %N.iterator()", baseSetPropertyName).build()
        )
        val equalsParameterName = "other"
        addFunction(
            FunSpec.builder("equals").addModifiers(KModifier.OVERRIDE).addParameter(
                    ParameterSpec.builder(equalsParameterName, Any::class.asTypeName().copy(nullable = true)).build()
                ).returns(Boolean::class).addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (this === %N)", equalsParameterName)
                    addStatement("return true")
                    endControlFlow()

                    beginControlFlow(
                        "if (%N !is %T)", equalsParameterName,
                        // TODO -- see comment above about Set
                        _KRelation::class.asClassName().parameterizedBy(STAR, STAR, STAR)
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
                        addStatement(
                            "return %N == %N.%N", baseSetPropertyName, equalsParameterName, baseSetPropertyName
                        )
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

    val mapletsParameterName = "maplets"
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(baseMapPropertyName, mapType)
            returns(interfaceTypeName)
            addStatement("return %N(%N)", implTypeSpec, baseMapPropertyName)
        }.build()
    )
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName, tupleType, KModifier.VARARG
            )
            returns(interfaceTypeName)
            addStatement(
                "return %N(%T().apply·{ %N.forEach{ put(it._1, it._2) } })",
                implClassName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                mapletsParameterName
            )
        }.build()
    )
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName, Iterable::class.asClassName().parameterizedBy(
                    tupleType
                )
            )
            returns(interfaceTypeName)
            addStatement(
                "return %N(%T().apply·{ %N.forEach{ put(it._1, it._2) } })",
                implClassName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                mapletsParameterName
            )
        }.build()
    )
    // TDOO - check for duplicate domains
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
            addParameter(
                mapletsParameterName, Iterable::class.asClassName().parameterizedBy(
                    tupleType
                )
            )
            returns(Boolean::class)
            addStatement(
                "return %N$implTypeArgs(%T().apply·{ %N.forEach{ put(it._1, it._2) } }, false).%N()",
                implClassName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                mapletsParameterName,
                validityFunctionName,
            )
        }.build()
    )
    // TDOO - check for duplicate domains
    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName, tupleType, KModifier.VARARG
            )
            returns(Boolean::class)
            addStatement(
                "return %N(%T().apply·{ %N.forEach·{ put(it._1, it._2) } }, false).%N()",
                implClassName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                mapletsParameterName,
                validityFunctionName
            )
        }.build()
    )
    addFunction(
        FunSpec.builder("as_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(baseMapPropertyName, superMappingTypeName)
            returns(interfaceTypeName)
            addStatement("return mk_%N(%N)", interfaceName, baseMapPropertyName)
        }.build()
    )
}
