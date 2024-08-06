package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.core.internal._InvariantClause
import com.anaplan.engineering.kazuki.core.internal._InvariantClauseEvaluation
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal const val validityFunctionName = "isValid"
internal const val evaluateInvariantFunctionName = "evaluateInvariant"
internal const val invariantClausesPropertyName = "invariantClauses"
internal const val enforceInvariantParameterName = "enforceInvariant"

private const val invariantClauseEvaluationsVariableName = "invariantClauseEvaluations"
private const val failedClausesVariableName = "failedClauses"

internal sealed interface InvariantClause {
    val name: String
    val functionString: String
}

// TODO - optionally get name from annotation?
internal class InvariantFunction(fn: KSFunctionDeclaration) : InvariantClause {
    override val name = fn.simpleName.asString()
    override val functionString = "::${this.name}"
}

internal class InvariantPrimitiveProperty(
    validationFn: KSFunctionDeclaration,
    property: KSPropertyDeclaration
) : InvariantClause {
    override val name = property.simpleName.asString()
    override val functionString = "{ ${validationFn.qualifiedName!!.asString()}(${this.name}) }"
}

internal class FreeformInvariant(override val name: String, override val functionString: String) : InvariantClause

@OptIn(KspExperimental::class)
// TODO properties of collections of primitives with invariants
internal fun TypeSpec.Builder.addInvariantFrom(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
    additionalInvariantParts: List<InvariantClause> = emptyList(),
) {
    val invariantClauses = mutableListOf<InvariantClause>().apply {
        interfaceClassDcl.getAllFunctions()
            .filter { it.isAnnotationPresent(Invariant::class) }
            .forEach { add(InvariantFunction(it)) }
        interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
            .map { it to processingState.primitiveInvariants[it.type.resolve().declaration.qualifiedName?.asString()] }
            .filter { it.second != null }
            .forEach { add(InvariantPrimitiveProperty(it.second!!, it.first)) }
        addAll(additionalInvariantParts)
    }
    if (invariantClauses.isEmpty()) {
        addFunction(FunSpec.builder(validityFunctionName).apply {
            addModifiers(KModifier.INTERNAL)
            returns(Boolean::class)
            addStatement("return true")
        }.build())
    } else {
        addProperty(
            PropertySpec.builder(
                invariantClausesPropertyName,
                List::class.parameterizedBy(_InvariantClause::class)
            ).apply {
                addModifiers(KModifier.PRIVATE)
                val clauses =
                    invariantClauses.joinToString(", ") { "${_InvariantClause::class.qualifiedName}(\"${it.name}\",·${it.functionString})" }
                initializer("listOf($clauses)")
            }.build()
        )
        // TODO -- use slf4j and log instance state
        addInitializerBlock(CodeBlock.builder().apply {
            beginControlFlow("if ($enforceInvariantParameterName)")
            addStatement("val $invariantClauseEvaluationsVariableName = ${evaluateInvariantFunctionName}()")
            beginControlFlow("if (${invariantClauseEvaluationsVariableName}.any·{ !it.holds })")
            addStatement("val $failedClausesVariableName = ${invariantClauseEvaluationsVariableName}.filter·{ !it.holds }.joinToString(\"·and·\")·{ it.clause.clauseName }")
            addStatement("throw %T(\"${interfaceClassDcl.simpleName.asString()} invariant failed in: \" + $failedClausesVariableName)", InvariantFailure::class)
            endControlFlow()
            endControlFlow()
        }.build())
        addFunction(FunSpec.builder(evaluateInvariantFunctionName).apply {
            addModifiers(KModifier.INTERNAL)
            returns(List::class.parameterizedBy(_InvariantClauseEvaluation::class))
            addStatement("return $invariantClausesPropertyName.map·{ ${_InvariantClauseEvaluation::class.qualifiedName}(it,·it.clauseFn()) }")
        }.build())
        addFunction(FunSpec.builder(validityFunctionName).apply {
            addModifiers(KModifier.INTERNAL)
            returns(Boolean::class)
            addStatement("return $evaluateInvariantFunctionName().all·{ it.holds }")
        }.build())
    }
}