package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.PrimitiveInvariant
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate

@OptIn(KspExperimental::class)
class KazukiSymbolProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    // TODO - auto generate tests for equals, hashcode, toString? -- maybe do with opt-in property
    private val processingState = ProcessingState(environment)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val allModules =
            resolver.getSymbolsWithAnnotation(Module::class.qualifiedName.orEmpty())
                .filterIsInstance<KSClassDeclaration>().groupBy { it.validate() }

        val primitiveTypeProcessor = PrimitiveTypeProcessor(processingState, environment.codeGenerator)
        initializePrimitiveInvariants(resolver, primitiveTypeProcessor)

        val moduleProcessor = ModuleProcessor(processingState, environment.codeGenerator)
        allModules[true]
            ?.filter { it.getAnnotationsByType(Module::class).single().makeable }
            ?.forEach { moduleProcessor.generateImplementation(it) }

        if (processingState.hasErrors()) {
            processingState.errors.forEach {
                environment.logger.error(it)
            }
            return emptyList()
        }
        return allModules[false] ?: emptyList()
    }

    class KazukiLogger(private val logger: KSPLogger): KSPLogger by logger {
        fun debug(message: String, symbol: KSNode? = null) = logging(message, symbol)
    }

    class ProcessingState(environment: SymbolProcessorEnvironment) {
        val primitiveInvariants: MutableMap<String, KSFunctionDeclaration> = mutableMapOf()
        val errors: MutableList<String> = mutableListOf()
        val logger = KazukiLogger(environment.logger)

        fun hasErrors() = errors.isNotEmpty()
    }

    // TOOD - load from libs
    private fun initializePrimitiveInvariants(resolver: Resolver, primitiveTypeProcessor: PrimitiveTypeProcessor) =
        processingState.primitiveInvariants.putAll(
            resolver.getSymbolsWithAnnotation(PrimitiveInvariant::class.qualifiedName.orEmpty())
                .filterIsInstance<KSFunctionDeclaration>()
                .filter(KSNode::validate).associateBy {
                    val primitiveTypeAlias = primitiveTypeProcessor.processPrimitiveType(it)
                    "${it.packageName.asString()}.${primitiveTypeAlias.name}"
                }
        )


}

class KazukiSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        KazukiSymbolProcessor(environment)

}