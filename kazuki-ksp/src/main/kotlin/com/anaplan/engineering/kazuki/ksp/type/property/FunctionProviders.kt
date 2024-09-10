package com.anaplan.engineering.kazuki.ksp.type.property

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.ksp.resolveAncestorTypeParameterNames
import com.anaplan.engineering.kazuki.ksp.superModules
import com.anaplan.engineering.kazuki.ksp.type.TypeGenerationContext
import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

internal data class FunctionProviderProperty(
    val property: KSPropertyDeclaration,
    val typeName: TypeName,
    val name: String = property.simpleName.asString(),
)

@OptIn(KspExperimental::class)
internal fun getFunctionProviderProperties(
    classDcl: KSClassDeclaration,
    typeGenerationContext: TypeGenerationContext,
): Collection<FunctionProviderProperty> {
    typeGenerationContext.logger.debug("Getting function providers for $classDcl", classDcl)
    val properties = classDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    val localTypeParameterResolver = classDcl.typeParameters.toTypeParameterResolver()
    val localFunctionProviderProperties = properties.filter { it.isAnnotationPresent(FunctionProvider::class) }.map {
        FunctionProviderProperty(it, it.type.toTypeName(localTypeParameterResolver))
    }
    val superFunctionProviderProperties = classDcl.superModules.flatMap { type ->
        val superClassDcl = type.resolve().declaration as KSClassDeclaration
        val superTypeParameterResolver = superClassDcl.typeParameters.toTypeParameterResolver()
        val superProperties = superClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
        val superFunctionProviderProperties = superProperties.filter { it.isAnnotationPresent(FunctionProvider::class) }

        val ancestorTypeParameters =
            classDcl.resolveAncestorTypeParameterNames(superClassDcl.qualifiedName!!.asString())
        typeGenerationContext.logger.debug("Type parameters: $ancestorTypeParameters")
        superFunctionProviderProperties.map {
            // TODO -- there are likely more complex instances here and we could do with a generic utility to resolve more generally
            val providerType = it.type.resolve()
            val providerClassName = providerType.toClassName()
            val resolvedProviderTypeArgs = providerType.arguments.map { typeArg ->
                val typeArgType = typeArg.type!!.resolve().declaration
                if (typeArgType is KSTypeParameter) {
                    val resolvedTypeName = ancestorTypeParameters.getTypeName(typeArgType)
                    typeGenerationContext.logger.debug("Mapped $typeArgType -> $resolvedTypeName")
                    resolvedTypeName
                } else {
                    typeArg.toTypeName(superTypeParameterResolver)
                }
            }
            val providerTypeName = if (resolvedProviderTypeArgs.isEmpty()) {
                providerClassName
            } else {
                providerClassName.parameterizedBy(*resolvedProviderTypeArgs.toTypedArray())
            }
            FunctionProviderProperty(it, providerTypeName)
        }
    }
    val resolvedFunctionProviderProperties =
        (localFunctionProviderProperties + superFunctionProviderProperties).groupBy { it.name }
            .map { (name, fpProperties) -> resolveFunctionProviderProperty(name, fpProperties, typeGenerationContext) }
    return resolvedFunctionProviderProperties.toList()
}

private fun resolveFunctionProviderProperty(
    name: String,
    fpProperties: List<FunctionProviderProperty>,
    typeGenerationContext: TypeGenerationContext
) =
    if (fpProperties.size == 1) {
        fpProperties.single()
    } else {
        val fpPropertiesSet = fpProperties.toSet()
        val properties = fpPropertiesSet.map { it.property }
        val overridden = properties.filter { property ->
            properties.any { typeGenerationContext.resolver.overrides(it, property) }
        }
        val nonOverridden = fpPropertiesSet.filter { it.property !in overridden }
        if (nonOverridden.size != 1) {
            val nonOverriddenNames = nonOverridden.map { it.property }.joinToString(", ") {
                it.qualifiedName?.asString() ?: it.simpleName.asString()
            }
            typeGenerationContext.logger.error("Found more than one non-overridden property for '$name': $nonOverriddenNames")
        }
        nonOverridden.single()
    }

@OptIn(KspExperimental::class)
internal fun TypeSpec.Builder.addFunctionProviders(
    functionProviderProperties: Collection<FunctionProviderProperty>,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) {
    val logger = typeGenerationContext.logger
    functionProviderProperties.forEach { property ->
        logger.debug("Adding function provider ${property.typeName}.${property.name}")
        val functionProvider = property.property.getAnnotationsByType(FunctionProvider::class).single()
        val providerQualifiedName = try {
            functionProvider.provider
            throw IllegalStateException("Expected to get a KSTypeNotPresentException")
        } catch (e: KSTypeNotPresentException) {
            e.ksType.declaration.qualifiedName!!.asString()
        }
        addProperty(
            PropertySpec.builder(
                property.name,
                property.typeName,
                KModifier.OVERRIDE
            ).apply {
                if (makeable) {
                    initializer(
                        CodeBlock.builder().apply {
                            addStatement("$providerQualifiedName(this)")
                        }.build()
                    )
                } else {
                    getter(FunSpec.getterBuilder().apply {
                        addStatement("throw UnsupportedOperationException()")
                    }.build())
                }
            }.build()
        )
    }
}