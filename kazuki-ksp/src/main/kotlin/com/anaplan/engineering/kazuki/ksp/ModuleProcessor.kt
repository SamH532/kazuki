package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.ksp.type.*
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

internal class ModuleProcessor(
    private val typeGenerationContext: TypeGenerationContext,
    private val codeGenerator: CodeGenerator
) {

    fun generateImplementation(clazz: KSClassDeclaration) {
        if (clazz.classKind == ClassKind.OBJECT) {
            processModuleObject(clazz)
        } else {
            processModuleClass(clazz)
        }
    }

    private fun KSClassDeclaration.kazukiType(): KazukiType {
        val superTypeNames = allSuperTypes.map { it.resolve().declaration.qualifiedName?.asString() }
        // TODO -- might be more than one!
        return if (Sequence1::class.qualifiedName in superTypeNames) {
            KazukiType.Sequence1Type
        } else if (Sequence::class.qualifiedName in superTypeNames) {
            KazukiType.SequenceType
        } else if (InjectiveMapping1::class.qualifiedName in superTypeNames) {
            KazukiType.InjectiveMapping1Type
        } else if (InjectiveMapping::class.qualifiedName in superTypeNames) {
            KazukiType.InjectiveMappingType
        } else if (Mapping1::class.qualifiedName in superTypeNames) {
            KazukiType.Mapping1Type
        } else if (Mapping::class.qualifiedName in superTypeNames) {
            KazukiType.MappingType
        } else if (Set1::class.qualifiedName in superTypeNames) {
            KazukiType.Set1Type
        } else if (Set::class.qualifiedName in superTypeNames) {
            KazukiType.SetType
        } else if (classKind == ClassKind.ENUM_CLASS) {
            KazukiType.QuoteType
        } else {
            KazukiType.RecordType
        }
    }

    @OptIn(KspExperimental::class)
    private fun processModuleClass(clazz: KSClassDeclaration) {
        typeGenerationContext.logger.debug("Processing module: ${clazz.qualifiedName!!.asString()}")
        val moduleClassName = "${clazz.simpleName.asString()}_Module"
        val makeable = clazz.getAnnotationsByType(Module::class).single().makeable
        val moduleTypeSpec = TypeSpec.objectBuilder(moduleClassName).apply {
            when (clazz.kazukiType()) {
                KazukiType.Sequence1Type -> addSeq1Type(clazz, makeable, typeGenerationContext)
                KazukiType.SequenceType -> addSeqType(clazz, makeable, typeGenerationContext)
                KazukiType.Set1Type -> addSet1Type(clazz, makeable, typeGenerationContext)
                KazukiType.SetType -> addSetType(clazz, makeable, typeGenerationContext)
                KazukiType.QuoteType -> processQuoteType(clazz, makeable, typeGenerationContext)
                KazukiType.RecordType -> addRecordType(clazz, makeable, typeGenerationContext)
                KazukiType.InjectiveMappingType -> addInjectiveMappingType(clazz, makeable, typeGenerationContext)
                KazukiType.InjectiveMapping1Type -> addInjectiveMapping1Type(clazz, makeable, typeGenerationContext)
                KazukiType.MappingType -> addMappingType(clazz, makeable, typeGenerationContext)
                KazukiType.Mapping1Type -> addMapping1Type(clazz, makeable, typeGenerationContext)
            }
        }.build()

        writeModule(clazz, moduleClassName, moduleTypeSpec)
    }

    private fun processModuleObject(clazz: KSClassDeclaration) {
        // TODO - type extension
        val types =
            clazz.declarations.filterIsInstance<KSClassDeclaration>().filter { it.getVisibility() == Visibility.PUBLIC }
                .groupBy { it.kazukiType() }

        val seq1Types = types[KazukiType.Sequence1Type] ?: emptyList()
        val seqTypes = types[KazukiType.SequenceType] ?: emptyList()
        val quoteTypes = types[KazukiType.QuoteType] ?: emptyList()
        val recordTypes = types[KazukiType.RecordType] ?: emptyList()

        val moduleClassName = "${clazz.simpleName.asString()}_Module"
        val moduleTypeSpec = TypeSpec.objectBuilder(moduleClassName).apply {
            seq1Types.forEach { addSeq1Type(it, true, typeGenerationContext) }
            seqTypes.forEach { addSeqType(it, true, typeGenerationContext) }
            quoteTypes.forEach { processQuoteType(it, true, typeGenerationContext) }
            recordTypes.forEach { addRecordType(it, true, typeGenerationContext) }
        }.build()

        writeModule(clazz, moduleClassName, moduleTypeSpec)
    }

    private fun writeModule(
        clazz: KSClassDeclaration,
        moduleClassName: String,
        moduleTypeSpec: TypeSpec
    ) {
        val clazzName =
            ClassName(packageName = clazz.packageName.asString(), clazz.simpleName.asString())
        val imports =
            clazz.declarations.filterIsInstance<KSClassDeclaration>().filter { it.getVisibility() == Visibility.PUBLIC }
                .map { it.simpleName.asString() }.toList()
        FileSpec.builder(clazz.packageName.asString(), moduleClassName)
            .addImport(clazzName, imports)
            .addType(moduleTypeSpec).build()
            .writeTo(codeGenerator, Dependencies(true, clazz.containingFile!!))
    }
}