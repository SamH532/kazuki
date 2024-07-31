package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.Cat
import com.anaplan.engineering.kazuki.core.CatRecord_Module.mk_CatRecord
import com.anaplan.engineering.kazuki.core.GenericRecordExtension_Module.component1
import com.anaplan.engineering.kazuki.core.GenericRecordExtension_Module.component2
import com.anaplan.engineering.kazuki.core.GenericRecordExtension_Module.mk_GenericRecordExtension
import com.anaplan.engineering.kazuki.core.GenericRecordInvOnlyExtension_Module.as_GenericRecordInvOnlyExtension
import com.anaplan.engineering.kazuki.core.GenericRecordInvOnlyExtension_Module.is_GenericRecordInvOnlyExtension
import com.anaplan.engineering.kazuki.core.GenericRecordInvOnlyExtension_Module.mk_GenericRecordInvOnlyExtension
import com.anaplan.engineering.kazuki.core.GenericRecord_Module.as_GenericRecord
import com.anaplan.engineering.kazuki.core.GenericRecord_Module.is_GenericRecord
import com.anaplan.engineering.kazuki.core.GenericRecord_Module.mk_GenericRecord
import com.anaplan.engineering.kazuki.core.MaineCoon
import com.anaplan.engineering.kazuki.core.MaineCoonRecord_Module.mk_MaineCoonRecord
import com.anaplan.engineering.kazuki.core.MoggyRecord_Module.mk_MoggyRecord
import com.anaplan.engineering.kazuki.core.OtherGenericRecord_Module.mk_OtherGenericRecord
import com.anaplan.engineering.kazuki.core.OtherRecord_Module.mk_OtherRecord
import com.anaplan.engineering.kazuki.core.RecordDblExtension_Module.mk_RecordDblExtension
import com.anaplan.engineering.kazuki.core.RecordExtensionAlternate_Module.mk_RecordExtensionAlternate
import com.anaplan.engineering.kazuki.core.RecordExtension_Module.component1
import com.anaplan.engineering.kazuki.core.RecordExtension_Module.component2
import com.anaplan.engineering.kazuki.core.RecordExtension_Module.mk_RecordExtension
import com.anaplan.engineering.kazuki.core.RecordExtension_Module.set
import com.anaplan.engineering.kazuki.core.RecordInvOnlyExtension_Module.as_RecordInvOnlyExtension
import com.anaplan.engineering.kazuki.core.RecordInvOnlyExtension_Module.is_RecordInvOnlyExtension
import com.anaplan.engineering.kazuki.core.RecordInvOnlyExtension_Module.mk_RecordInvOnlyExtension
import com.anaplan.engineering.kazuki.core.Record_Module.as_Record
import com.anaplan.engineering.kazuki.core.Record_Module.is_Record
import com.anaplan.engineering.kazuki.core.Record_Module.mk_Record
import com.anaplan.engineering.kazuki.core.mk_Set
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestRecordExtension {

    @Test
    fun creation() {
        val record = mk_RecordExtension(3, "a")
        assertEquals(record.a, 3)
        assertEquals(record.b, "a")

        // Ensuring override are handled correctly
        val cat = Cat()
        assertEquals(cat, mk_CatRecord(cat).me)
        assertEquals(cat, mk_MoggyRecord(cat).me)
        val maineCoon = MaineCoon()
        assertEquals(maineCoon, mk_MaineCoonRecord(maineCoon).me)
    }

    @Test
    fun equalityOfInvOnlyExtension() {
        assertEquals(mk_RecordInvOnlyExtension(2), mk_Record(2))
        assertEquals(mk_Record(2), mk_RecordInvOnlyExtension(2))

        assertNotEquals(mk_RecordInvOnlyExtension(2), mk_Record(3))
        assertNotEquals(mk_Record(3), mk_RecordInvOnlyExtension(2))

        assertEquals(mk_RecordInvOnlyExtension(2), mk_RecordInvOnlyExtension(2))
        assertEquals(mk_RecordInvOnlyExtension(2), mk_RecordInvOnlyExtension(2))

        assertNotEquals(mk_RecordInvOnlyExtension(2), mk_RecordInvOnlyExtension(3))
        assertNotEquals(mk_RecordInvOnlyExtension(3), mk_RecordInvOnlyExtension(2))

        // Comparable limits OtherRecord to static type compatibility
//        assertNotEquals<Any>(mk_OtherRecord(2), mk_Record(2))
//        assertNotEquals<Any>(mk_Record(2), mk_OtherRecord(2))

        assertEquals(mk_GenericRecordInvOnlyExtension(2), mk_GenericRecord(2))
        assertEquals(mk_GenericRecord(2), mk_GenericRecordInvOnlyExtension(2))

        assertNotEquals(mk_GenericRecordInvOnlyExtension(2), mk_GenericRecord(3))
        assertNotEquals(mk_GenericRecord(3), mk_GenericRecordInvOnlyExtension(2))

        assertEquals(mk_GenericRecordInvOnlyExtension(2), mk_GenericRecordInvOnlyExtension(2))
        assertEquals(mk_GenericRecordInvOnlyExtension(2), mk_GenericRecordInvOnlyExtension(2))

        assertNotEquals(mk_GenericRecordInvOnlyExtension(2), mk_GenericRecordInvOnlyExtension(3))
        assertNotEquals(mk_GenericRecordInvOnlyExtension(3), mk_GenericRecordInvOnlyExtension(2))

        // Making choice that type declaration is part of equality not just tuple
//        assertNotEquals<Any>(mk_OtherGenericRecord(2), mk_GenericRecord(2))
//        assertNotEquals<Any>(mk_GenericRecord(2), mk_OtherGenericRecord(2))
    }

    @Test
    fun hashCodeEqualityOfInvOnlyExtension() {
        assertEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_Record(2).hashCode())
        assertEquals(mk_Record(2).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())

        assertNotEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_Record(3).hashCode())
        assertNotEquals(mk_Record(3).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())

        assertEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())
        assertEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())

        assertNotEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_RecordInvOnlyExtension(3).hashCode())
        assertNotEquals(mk_RecordInvOnlyExtension(3).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())

        assertEquals(mk_GenericRecordInvOnlyExtension(2).hashCode(), mk_GenericRecord(2).hashCode())
        assertEquals(mk_GenericRecord(2).hashCode(), mk_GenericRecordInvOnlyExtension(2).hashCode())

        assertNotEquals(mk_GenericRecordInvOnlyExtension(2).hashCode(), mk_GenericRecord(3).hashCode())
        assertNotEquals(mk_GenericRecord(3).hashCode(), mk_GenericRecordInvOnlyExtension(2).hashCode())

        assertEquals(mk_GenericRecordInvOnlyExtension(2).hashCode(), mk_GenericRecordInvOnlyExtension(2).hashCode())
        assertEquals(mk_GenericRecordInvOnlyExtension(2).hashCode(), mk_GenericRecordInvOnlyExtension(2).hashCode())

        assertNotEquals(mk_GenericRecordInvOnlyExtension(2).hashCode(), mk_GenericRecordInvOnlyExtension(3).hashCode())
        assertNotEquals(mk_GenericRecordInvOnlyExtension(3).hashCode(), mk_GenericRecordInvOnlyExtension(2).hashCode())
    }

    // TODO should is/as admit anon tuple?
    @Test
    fun is_() {
        assertEquals(true, is_Record(mk_Record(2)))
        assertEquals(true, is_Record(mk_RecordInvOnlyExtension(2)))
        assertEquals(false, is_Record(mk_RecordExtension(2, "3")))
        assertEquals(false, is_Record(mk_OtherRecord(2)))

        assertEquals(true, is_RecordInvOnlyExtension(mk_Record(2)))
        assertEquals(false, is_RecordInvOnlyExtension(mk_Record(0)))
        assertEquals(true, is_RecordInvOnlyExtension(mk_RecordInvOnlyExtension(2)))
        assertEquals(false, is_RecordInvOnlyExtension(mk_RecordExtension(2, "3")))
        assertEquals(false, is_RecordInvOnlyExtension(mk_OtherRecord(2)))

        assertEquals(true, is_GenericRecord<Int>(mk_GenericRecord(2)))
        assertEquals(true, is_GenericRecord<Int>(mk_GenericRecordInvOnlyExtension(2)))
        assertEquals(false, is_GenericRecord<Int>(mk_GenericRecordExtension(2, mk_Set(3))))
        assertEquals(false, is_GenericRecord<Int>(mk_OtherGenericRecord(2)))

        assertEquals(true, is_GenericRecordInvOnlyExtension(mk_GenericRecord(2)))
        assertEquals(false, is_GenericRecordInvOnlyExtension(mk_GenericRecord(0)))
        assertEquals(true, is_GenericRecordInvOnlyExtension(mk_GenericRecordInvOnlyExtension(2)))
        assertEquals(false, is_GenericRecordInvOnlyExtension(mk_GenericRecordExtension(2, mk_Set(3))))
        assertEquals(false, is_GenericRecordInvOnlyExtension(mk_OtherGenericRecord(2)))
    }

    // TODO should is/as admit anon tuple?
    @Test
    fun as_() {
        assertEquals(mk_Record(2), as_Record(mk_Record(2)))
        assertEquals(mk_Record(2), as_Record(mk_RecordInvOnlyExtension(2)))

        assertEquals(mk_RecordInvOnlyExtension(2), as_RecordInvOnlyExtension(mk_Record(2)))
        assertEquals(mk_RecordInvOnlyExtension(2), as_RecordInvOnlyExtension(mk_RecordInvOnlyExtension(2)))

        assertEquals(mk_GenericRecord(2), as_GenericRecord(mk_GenericRecord(2)))
        assertEquals(mk_GenericRecord(2), as_GenericRecord(mk_GenericRecordInvOnlyExtension(2)))

        assertEquals(mk_GenericRecordInvOnlyExtension(2), as_GenericRecordInvOnlyExtension(mk_GenericRecord(2)))
        assertEquals(
            mk_GenericRecordInvOnlyExtension(2),
            as_GenericRecordInvOnlyExtension(mk_GenericRecordInvOnlyExtension(2))
        )
    }

    @Test
    fun set() {
        assertEquals(mk_RecordExtension(4, "3"), mk_RecordExtension(2, "3").set(a = 4))
        assertEquals(mk_RecordExtension(2, "hello"), mk_RecordExtension(2, "3").set(b = "hello"))
        assertEquals(mk_RecordExtension(4, "2"), mk_RecordExtension(2, "3").set(a = 4, b = "2"))
        assertEquals(mk_RecordExtension(2, "3"), mk_RecordExtension(2, "3").set())
    }

    // Note that for deconstruction, must:
    // * import component extensions (manually)
    // * OR have record implement corresponding Tuple interface
    @Test
    fun deconstruction() {
        val (a, b) = mk_RecordExtension(2, "3")
        assertEquals(2, a)
        assertEquals("3", b)

        val (c, d) = mk_GenericRecordExtension(2, mk_Set(3))
        assertEquals(2, c)
        assertEquals(mk_Set(3), d)

        val (e,f,g) = mk_RecordDblExtension(2, "3", 4.0)
        assertEquals(2, e)
        assertEquals("3", f)
        assertEquals(4.0, g)
    }

    @Test
    fun functionProviders() {
        assertEquals(4, mk_Record(2).functions.mutateA())
        assertEquals(4, mk_RecordExtension(2, "4").functions.mutateA())
        assertEquals(8, mk_RecordExtensionAlternate(2, 4).functions.mutateA())
    }

    // TODO -- test overriding variable with more specific type
}