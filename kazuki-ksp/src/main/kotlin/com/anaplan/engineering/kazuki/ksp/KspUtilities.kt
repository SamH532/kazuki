@file:OptIn(KspExperimental::class, KspExperimental::class, KspExperimental::class)

package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Module
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

// TODO -- extract KSP utilities to separate project and test independently!

internal val KSClassDeclaration.allSuperTypes
    get(): List<KSTypeReference> =
        superTypes.flatMap {
            val st = it.resolve().declaration
            mutableListOf(it).apply {
                if (st is KSClassDeclaration) {
                    addAll(st.allSuperTypes)
                }
            }
        }.toList()


internal fun KSClassDeclaration.getSuperTypePathTo(qualifiedClassName: String): List<KSTypeReference>? =
    superTypes.map {
        it.getSuperTypePathTo(qualifiedClassName)
    }.filterNotNull().firstOrNull() ?: throw IllegalStateException("No super type path found to $qualifiedClassName")

internal fun KSTypeReference.getSuperTypePathTo(qualifiedClassName: String): List<KSTypeReference>? {
    val classDeclaration = resolve().declaration
    if (classDeclaration !is KSClassDeclaration) {
        return null
    }
    return if (classDeclaration.qualifiedName?.asString() == qualifiedClassName) {
        listOf(this)
    } else {
        classDeclaration.superTypes.map {
            val superPath = it.getSuperTypePathTo(qualifiedClassName)
            if (superPath == null) {
                null
            } else {
                listOf(this) + superPath
            }
        }.filterNotNull().firstOrNull()
    }
}

internal val KSClassDeclaration.superModules get() = allSuperTypes.filter { it.resolve().declaration.isAnnotationPresent(Module::class) }

internal fun KSClassDeclaration.resolveTypeNameOfAncestorGenericParameter(
    ancestorQualifiedClassName: String,
    paramIndex: Int
) = resolveAncestorTypeParameterNames(ancestorQualifiedClassName).getTypeName(paramIndex)

class AncestorTypeParameters(
    private val typeParameters: List<KSTypeParameter>,
    private val indexToTypeName: Map<Int, TypeName>
) {
    private val nameToTypeName by lazy {
        typeParameters.mapIndexed { i, p ->
            p.name.asString() to indexToTypeName[i]
        }.toMap()
    }

    val typeNames by lazy { typeParameters.indices.map { getTypeName(it) }}

    fun getTypeName(index: Int) = indexToTypeName[index]!!

    fun getTypeName(name: String) = nameToTypeName[name]!!

    override fun toString() = nameToTypeName.toString()

    fun getTypeName(typeParam: KSTypeParameter) = indexToTypeName[typeParameters.indexOf(typeParam)]!!

}

internal fun KSClassDeclaration.resolveAncestorTypeParameterNames(
    ancestorQualifiedClassName: String,
): AncestorTypeParameters {
    var childClassDcl = this
    var path = getSuperTypePathTo(ancestorQualifiedClassName)!!
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

    val childTypeParameters = childClassDcl.typeParameters
    if (childTypeParameters.size != argList.size) {
        throw IllegalStateException("Unexpected mismatch in resolved and unresolved type parameters of $ancestorQualifiedClassName")
    }
    val indexToTypeName = argList.mapIndexed { i, arg ->
        val typeParameterResolver = typeParameters.toTypeParameterResolver()
        val typeName = if (arg is KSTypeParameter) {
            arg.toTypeVariableName(typeParameterResolver)
        } else if (arg is KSTypeArgument) {
            arg.toTypeName(typeParameterResolver)
        } else {
            throw IllegalStateException("Unable to identify parameter $i of ancestor $ancestorQualifiedClassName")
        }
        i to typeName
    }.toMap()

    return AncestorTypeParameters(
        childTypeParameters,
        indexToTypeName
    )
}

internal fun findUnusedGenericName(usedTypeVariableNames: List<TypeVariableName>): String {
    val candidates = ('A'..'Z').map { "_$it" }
    val usedNames = usedTypeVariableNames.map { it.name }.toSet()
    return (candidates - usedNames).first()
}
