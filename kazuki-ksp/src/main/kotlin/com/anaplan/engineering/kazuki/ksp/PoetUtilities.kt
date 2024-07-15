package com.anaplan.engineering.kazuki.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import kotlin.reflect.KClass

internal fun PropertySpec.Builder.lazy(format: String, vararg args: Any?) =
    delegate(
        CodeBlock.builder().beginControlFlow("lazy").add(format, *args).endControlFlow().build()
    )

