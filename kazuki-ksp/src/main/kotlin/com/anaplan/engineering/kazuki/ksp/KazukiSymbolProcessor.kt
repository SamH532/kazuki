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

    class KazukiLogger(private val environment: SymbolProcessorEnvironment): KSPLogger by environment.logger {

        enum class Level {
            DEBUG, INFO, WARN, ERROR
        }

        companion object {
            const val DebugLevelPropertyName = "com.anaplan.engineering.kazuki.compile.debug.level"
        }
        // Enables redirection of Kazuki debug logging so that we can view this info without enabling compiler debug
        // logging more widely
        private val debugRedirect by lazy {
            val property = environment.options[DebugLevelPropertyName] ?: Level.DEBUG.name
            val level = Level.valueOf(property.uppercase())
            environment.logger.info("Kazuki debug level logging will be redirected to $level")
            when (level) {
                Level.DEBUG -> { m: String, s: KSNode? -> logging(m, s) }
                Level.INFO -> { m: String, s: KSNode? -> info(m, s) }
                Level.WARN -> { m: String, s: KSNode? -> warn(m, s) }
                Level.ERROR -> { m: String, s: KSNode? -> error(m, s) }
            }
        }

        fun debug(message: String, symbol: KSNode? = null) = debugRedirect(message, symbol)
    }

    class ProcessingState(environment: SymbolProcessorEnvironment) {
        val primitiveInvariants: MutableMap<String, KSFunctionDeclaration> = mutableMapOf()
        val errors: MutableList<String> = mutableListOf()
        val logger = KazukiLogger(environment)

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