package com.anaplan.engineering.kazuki.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import kotlin.reflect.KClass

// TODO -- extract KSP utilities to separate project and test independently!

internal fun KSClassDeclaration.allSuperTypes(): Set<KSTypeReference> =
    superTypes.flatMap {
        val st = it.resolve().declaration
        mutableSetOf(it).apply {
            if (st is KSClassDeclaration) {
                addAll(st.allSuperTypes())
            }
        }
    }.toSet()

internal fun KSClassDeclaration.getSuperTypePathTo(klass: KClass<*>): List<KSTypeReference>? =
    superTypes.map {
        it.getSuperTypePathTo(klass)
    }.filterNotNull().firstOrNull() ?: throw IllegalStateException("No super type path found to $klass")

internal fun KSTypeReference.getSuperTypePathTo(klass: KClass<*>): List<KSTypeReference>? {
    val classDeclaration = resolve().declaration
    if (classDeclaration !is KSClassDeclaration) {
        return null
    }
    return if (classDeclaration.qualifiedName?.asString() == klass.qualifiedName) {
        listOf(this)
    } else {
        classDeclaration.superTypes.map {
            val superPath = it.getSuperTypePathTo(klass)
            if (superPath == null) {
                null
            } else {
                listOf(this) + superPath
            }
        }.filterNotNull().firstOrNull()
    }
}

fun KSClassDeclaration.resolveTypeNameOfAncestorGenericParameter(
    ancestorKClass: KClass<*>,
    paramIndex: Int
): TypeName {
    var childClassDcl = this
    var path = getSuperTypePathTo(ancestorKClass)!!
    var argList: List<Any> = childClassDcl.typeParameters

    while (path.isNotEmpty()) {
        val parentType = path.first()
        val childTypeParams = childClassDcl.typeParameters
        val parentTypeArgs = parentType.element!!.typeArguments

        argList = parentTypeArgs.map { ta ->
            val declaration = ta.type!!.resolve().declaration
            if (declaration is KSTypeParameter) {
                argList[childTypeParams.indexOf(declaration)]
            } else {
                ta
            }
        }

        path = path.drop(1)
        childClassDcl = parentType.resolve().declaration as KSClassDeclaration
    }

    val paramType = argList.getOrNull(paramIndex)
    val typeParameterResolver = typeParameters.toTypeParameterResolver()
    val typeName = if (paramType is KSTypeParameter) {
        paramType.toTypeVariableName(typeParameterResolver)
    } else if (paramType is KSTypeArgument) {
        paramType.toTypeName(typeParameterResolver)
    } else {
        null
    }

    return typeName
        ?: throw IllegalStateException("Unable to identify parameter $paramIndex of ancestor $ancestorKClass")
}

