package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.ComparableProperty
import com.anaplan.engineering.kazuki.core.ComparableTypeLimit
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

internal const val comparableWithPropertyName = "comparableWith"
private val comparableWithTypeName = KClass::class.asTypeName().parameterizedBy(STAR)

internal data class ComparableWith(
    val property: KSPropertyDeclaration?,
    val className: ClassName,
)

internal fun TypeSpec.Builder.addComparableWith(
    classDcl: KSClassDeclaration,
    default: ClassName,
    processingState: KazukiSymbolProcessor.ProcessingState
): ComparableWith {
    val comparableProperty = getComparableProperty(classDcl, processingState)
    val comparableWithClass = if (comparableProperty == null) {
        getExplicitComparableTypeLimit(classDcl, processingState)?.toClassName() ?: default
    } else {
        val comparableClassDcl = comparableProperty.parentDeclaration as KSClassDeclaration
        comparableClassDcl.toClassName()
    }
    addProperty(
        PropertySpec.builder(comparableWithPropertyName, comparableWithTypeName, KModifier.OPEN, KModifier.OVERRIDE)
            .initializer(CodeBlock.of("$comparableWithClass::class")).build()
    )
    return ComparableWith(comparableProperty, comparableWithClass)
}

@OptIn(KspExperimental::class)
fun getExplicitComparableTypeLimit(
    rootClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState
): KSClassDeclaration? {
    fun recurse(
        classDcl: KSClassDeclaration,
        processingState: KazukiSymbolProcessor.ProcessingState
    ) =
        if (classDcl.isAnnotationPresent(ComparableTypeLimit::class)) {
            processingState.logger.info("found: $classDcl")
            classDcl
        } else {
            val comparableTypeLimits = classDcl.superTypes.map {
                getExplicitComparableTypeLimit(it.resolve().declaration as KSClassDeclaration, processingState)
            }.filterNotNull().toList()

            if (comparableTypeLimits.size > 1) {
                processingState.errors.add("Ambiguous comparable type limits for $rootClassDcl: $comparableTypeLimits")
            }
            comparableTypeLimits.singleOrNull()
        }

    return recurse(rootClassDcl, processingState)
}

@OptIn(KspExperimental::class)
internal fun getComparableProperty(
    classDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState
): KSPropertyDeclaration? {
    val className = classDcl.toClassName()
    val localComparableProperties = classDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
        .filter { it.isAnnotationPresent(ComparableProperty::class) }.toList()
    localComparableProperties.filter { it.getter == null }.forEach {
        processingState.errors.add("Comparable property $className.$it should be backed by function")
    }
    return if (localComparableProperties.isEmpty()) {
        val nonOverriddenSuperComparableProperties = classDcl.superModules.flatMap { type ->
            val superClassDcl = type.resolve().declaration as KSClassDeclaration
            val superProperties = superClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
            val superFunctionProviderProperties =
                superProperties.filter { it.isAnnotationPresent(ComparableProperty::class) }
            superFunctionProviderProperties.filter { s -> localComparableProperties.none { l -> s.simpleName.asString() == l.simpleName.asString() } }
        }
        if (nonOverriddenSuperComparableProperties.size > 1) {
            processingState.errors.add("Unable to determine unique comparable property for $className found $nonOverriddenSuperComparableProperties")
        }
        nonOverriddenSuperComparableProperties.singleOrNull()
    } else {
        if (localComparableProperties.size > 1) {
            processingState.errors.add("Only one property of $className should be marked as comparable")
        }
        localComparableProperties.singleOrNull()
    }
}