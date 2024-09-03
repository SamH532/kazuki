package com.anaplan.engineering.kazuki.core

@Module
interface Record {
    val a: Int

    @FunctionProvider(RecordFunctions::class)
    val functions: RecordFunctions
}

open class RecordFunctions(private val r: Record) {
    open val mutateA = function<Int>(command = { r.a * 2 })
}

@Module
interface RecordExtension : Record {
    val b: String
}

@Module
interface RecordExtensionAlternate : Record {
    val b: Int

    @FunctionProvider(RecordExtensionAlternateFunctions::class)
    override val functions: RecordExtensionAlternateFunctions
}

open class RecordExtensionAlternateFunctions(private val r: RecordExtensionAlternate) : RecordFunctions(r) {
    override val mutateA = function<Int>(command = { r.a * r.b })
}

@Module
interface RecordInvOnlyExtension : Record {
    @Invariant
    fun notZero() = a != 0
}

@Module
@ComparableTypeLimit
interface OtherRecord {
    val a: Int
}

@Module
interface GenericRecord<T> {
    val a: T
}

@Module
interface GenericRecordExtension<T> : GenericRecord<T> {
    val b: Set<T>
}

@Module
interface GenericRecordInvOnlyExtension : GenericRecord<Int> {
    @Invariant
    fun notZero() = a != 0
}

@Module
@ComparableTypeLimit
interface OtherGenericRecord<T> {
    val a: T
}

@Module
interface RecordDblExtension: RecordExtension, Tuple3<Int, String, Double> {
    val c: Double
}

interface Animal
open class Cat: Animal
class MaineCoon: Cat()

@Module
interface AnimalRecord {
    val me: Animal
}

@Module
interface CatRecord: AnimalRecord {
    override val me: Cat
}

@Module
interface MaineCoonRecord: CatRecord {
    override val me: MaineCoon
}

@Module
interface MoggyRecord: CatRecord {

}

@Module(makeable = false)
interface UnmakeableRecord {
    val a: Int
    val b: Int
    val c: Int
}

@Module
interface UnmakeableExtAllFields: UnmakeableRecord {
    val d: Int
}

@Module
interface UnmakeableExtWithDynamic: UnmakeableRecord {
    val d: Int
    override val b: Int get() = 2
}

@Module
interface UnmakeableInvOnlyAllFields: UnmakeableRecord {
    @Invariant
    fun notZero() = a != 0
}

@Module
interface UnmakeableInvOnlyWithDynamic: UnmakeableRecord {
    @Invariant
    fun notZero() = a != 0
    override val b: Int get() = 2
}

@Module
interface UnmakeableExtAllFieldsC: UnmakeableExtAllFields
@Module
interface UnmakeableExtWithDynamicC: UnmakeableExtWithDynamic
@Module
interface UnmakeableInvOnlyAllFieldsC: UnmakeableInvOnlyAllFields
@Module
interface UnmakeableInvOnlyWithDynamicC: UnmakeableInvOnlyWithDynamic



