package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.FunctionProvider
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
    processingState: KazukiSymbolProcessor.ProcessingState,
): Collection<FunctionProviderProperty> {
    processingState.logger.debug("Getting function providers for $classDcl", classDcl)
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

        val ancestorTypeParameters = classDcl.resolveAncestorTypeParameterNames(superClassDcl.qualifiedName!!.asString())
        processingState.logger.debug("Type parameters: $ancestorTypeParameters")
        superFunctionProviderProperties.map {
            // TODO -- there are likely more complex instances here and we could do with a generic utility to resolve more generally
            val providerType = it.type.resolve()
            val providerClassName = providerType.toClassName()
            val resolvedProviderTypeArgs = providerType.arguments.map { typeArg ->
                val typeArgType = typeArg.type!!.resolve().declaration
                if (typeArgType is KSTypeParameter) {
                    val resolvedTypeName = ancestorTypeParameters.getTypeName(typeArgType)
                    processingState.logger.debug("Mapped $typeArgType -> $resolvedTypeName")
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
    val resolvedFunctionProviderProperties = (localFunctionProviderProperties + superFunctionProviderProperties).groupBy { it.name }.map { (_, properties) ->
        if (properties.size == 1) {
            properties.single()
        } else {
            val overridden = properties.mapNotNull { it.property.findOverridee() }
            properties.single { it.property !in overridden }
        }
    }
    return resolvedFunctionProviderProperties.toList()
}


@OptIn(KspExperimental::class)
internal fun TypeSpec.Builder.addFunctionProviders(
    functionProviderProperties: Collection<FunctionProviderProperty>,
    processingState: KazukiSymbolProcessor.ProcessingState,
) {
    val logger = processingState.logger
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
                initializer(
                    CodeBlock.builder().apply {
                        addStatement("$providerQualifiedName(this)")
                    }.build()
                )
            }.build()
        )
    }
}