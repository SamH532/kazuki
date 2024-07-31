package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.CaselessStringRecord_Module.mk_CaselessStringRecord
import com.anaplan.engineering.kazuki.core.CaselessString_Module.mk_CaselessString
import com.anaplan.engineering.kazuki.core.Flight_Module.mk_Flight
import com.anaplan.engineering.kazuki.core.Id1_Module.mk_Id1
import com.anaplan.engineering.kazuki.core.Id2_Module.mk_Id2
import com.anaplan.engineering.kazuki.core.Id3_Module.mk_Id3
import com.anaplan.engineering.kazuki.core.NonEmptyCaselessString_Module.mk_NonEmptyCaselessString
import com.anaplan.engineering.kazuki.core.Time_Module.mk_Time
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestComparisons {

    @Test
    fun caselessString_equals() {
        assertEquals(toCaselessString("hello"), toCaselessString("hello"))
        assertEquals(toCaselessString("hEllO"), toCaselessString("Hello"))
    }

    @Test
    fun caselessString_notEquals() {
        assertNotEquals(toCaselessString("hell0"), toCaselessString("hello"))
        assertNotEquals(toCaselessString("hEllO"), toCaselessString("Hallo"))
    }

    @Test
    fun caselessString_hash() {
        assertEquals(1, mk_Set(toCaselessString("hello"), toCaselessString("hello")).card)
        assertEquals(1, mk_Set(toCaselessString("hEllO"), toCaselessString("Hello")).card)

        assertEquals(4, mk_Mapping(mk_(toCaselessString("hello"), 4))[toCaselessString("hello")])
        assertEquals(4, mk_Mapping(mk_(toCaselessString("HellO"), 4))[toCaselessString("hELLo")])
    }

    @Test
    fun caselessString_nested() {
        assertEquals(
            mk_Set(toCaselessString("hello"), toCaselessString("wOrld")),
            mk_Set(toCaselessString("WorlD"), toCaselessString("hEllO"))
        )
        assertEquals(
            mk_Seq(toCaselessString("Hello"), toCaselessString("wOrLd")),
            mk_Seq(toCaselessString("hEllO"), toCaselessString("WOrlD"))
        )
        assertEquals(
            mk_Mapping(mk_(toCaselessString("HelLo"), toCaselessString("wOrLd"))),
            mk_Mapping(mk_(toCaselessString("hEllO"), toCaselessString("WOrlD")))
        )
        assertEquals(
            mk_CaselessStringRecord(toCaselessString("HelLo")),
            mk_CaselessStringRecord(toCaselessString("hEllO"))
        )
    }

    @Test
    fun caselessString_descendant_noOverride_equals() {
        assertEquals(toCaselessString("hello"), toNonEmptyCaselessString("hello"))
        assertEquals(toNonEmptyCaselessString("hEllO"), toCaselessString("Hello"))

        assertEquals(toId1("hello"), toNonEmptyCaselessString("hello"))
        assertEquals(toId2("hEllO"), toCaselessString("Hello"))
    }

    @Test
    fun caselessString_descendant_siblings_equals() {
        assertEquals<CaselessString>(toId1("hello"), toId2("hello"))
        assertEquals<CaselessString>(toId2("hEllO"), toId1("Hello"))
    }

    @Test
    fun caselessString_descendant_override_equals() {
        assertEquals(toId3("hello"), toId3("hello"))
        assertEquals(toId3("h ello"), toId3("hell o"))
        assertEquals(toId3("h EllO"), toId3("H ell o"))
    }

    @Test
    fun caselessString_descendant_noOverride_notEquals() {
        assertNotEquals(toCaselessString("hell0"), toNonEmptyCaselessString("hello"))
        assertNotEquals(toNonEmptyCaselessString("hEllO"), toCaselessString("Hallo"))

        assertNotEquals(toId1("heillo"), toNonEmptyCaselessString("hello"))
        assertNotEquals(toId2("hAllO"), toCaselessString("Hello"))
    }

    @Test
    fun caselessString_descendant_siblings_notEquals() {
        assertNotEquals<CaselessString>(toId1("hel lo"), toId2("hello"))
        assertNotEquals<CaselessString>(toId2("hEllO"), toId1("He1lo"))
    }

    @Test
    fun caselessString_descendant_override_notEquals() {
        assertNotEquals(toCaselessString("hellO"), toId3("hello"))
        assertNotEquals(toId3("hEllO"), toCaselessString("Hello"))

        assertNotEquals(toId1("heillo"), toNonEmptyCaselessString("hello"))
        assertNotEquals(toId2("hAllO"), toCaselessString("Hello"))

        assertNotEquals(toId3("hel lo"), toId3("hell0"))
    }

    @Test
    fun caselessString_descendant_noOverride_hash() {
        assertEquals(1, mk_Set(toCaselessString("hello"), toNonEmptyCaselessString("hello")).card)
        assertEquals(1, mk_Set(toId1("hEllO"), toCaselessString("Hello")).card)

        assertEquals(4, mk_Mapping<CaselessString, Int>(mk_(toId1("hello"), 4))[toNonEmptyCaselessString("hello")])
        assertEquals(4, mk_Mapping(mk_(toCaselessString("HellO"), 4))[toId2("hELLo")])
    }

    @Test
    fun caselessString_descendant_siblings_hash() {
        assertEquals(1, mk_Set(toId1("hEllO"), toId2("Hello")).card)

        assertEquals(4, mk_Mapping<CaselessString, Int>(mk_(toId1("hello"), 4))[toId2("hello")])
        assertEquals(4, mk_Mapping<CaselessString, Int>(mk_(toId2("HellO"), 4))[toId1("hELLo")])
    }

    @Test
    fun caselessString_descendant_override_hash() {
        assertEquals(1, mk_Set(toId3("hel lo"), toId3(" hello")).card)
        assertEquals(2, mk_Set(toId1("hEllO"), toId3("Hello")).card)

        causesPreconditionFailure {
            mk_Mapping<CaselessString, Int>(mk_(toId1("hello"), 4))[toId3("hello")]
        }
        causesPreconditionFailure {
            mk_Mapping<CaselessString, Int>(mk_(toId3("hello"), 4))[toCaselessString("hello")]
        }
        assertEquals(4, mk_Mapping(mk_(toId3("H ellO"), 4))[toId3("hELLo ")])
    }

    @Test
    fun caselessString_descendant_noOverride_nested() {
        assertEquals(
            mk_Set(toCaselessString("hello"), toNonEmptyCaselessString("wOrld")),
            mk_Set(toId1("WorlD"), toCaselessString("hEllO"))
        )
        assertEquals(
            mk_Seq(toCaselessString("Hello"), toNonEmptyCaselessString("wOrLd")),
            mk_Seq(toId1("hEllO"), toCaselessString("WOrlD"))
        )
        assertEquals(
            mk_Mapping<CaselessString, CaselessString>(mk_(toNonEmptyCaselessString("HelLo"), toCaselessString("wOrLd"))),
            mk_Mapping<CaselessString, CaselessString>(mk_(toCaselessString("hEllO"), toId2("WOrlD")))
        )
        assertEquals(
            mk_CaselessStringRecord(toCaselessString("HelLo")),
            mk_CaselessStringRecord(toId2("hEllO"))
        )
    }

    @Test
    fun caselessString_descendant_noOverride_siblings() {
        assertEquals(
            mk_Set(toId2("hello"), toId1("wOrld")),
            mk_Set(toId1("WorlD"), toId2("hEllO"))
        )
        assertEquals(
            mk_Seq(toId2("Hello"), toId1("wOrLd")),
            mk_Seq(toId1("hEllO"), toId2("WOrlD"))
        )
        assertEquals(
            mk_Mapping<CaselessString, CaselessString>(mk_(toId2("HelLo"), toId1("wOrLd"))),
            mk_Mapping<CaselessString, CaselessString>(mk_(toId1("hEllO"), toId2("WOrlD")))
        )
        assertEquals(
            mk_CaselessStringRecord(toId1("HelLo")),
            mk_CaselessStringRecord(toId2("hEllO"))
        )
    }

    @Test
    fun caselessString_descendant_override_nested() {
        assertNotEquals(
            mk_Set(toCaselessString("hello"), toNonEmptyCaselessString("wOrld")),
            mk_Set(toId1("WorlD"), toId3("hEllO"))
        )
        assertNotEquals(
            mk_Seq(toCaselessString("Hello"), toNonEmptyCaselessString("wOrLd")),
            mk_Seq(toId3("hEllO"), toCaselessString("WOrlD"))
        )
        assertNotEquals(
            mk_Mapping<CaselessString, CaselessString>(mk_(toNonEmptyCaselessString("HelLo"), toCaselessString("wOrLd"))),
            mk_Mapping<CaselessString, CaselessString>(mk_(toCaselessString("hEllO"), toId3("WOrlD")))
        )
        assertNotEquals(
            mk_CaselessStringRecord(toCaselessString("HelLo")),
            mk_CaselessStringRecord(toId3("hEllO"))
        )

        assertEquals(
            mk_Set(toId3("hello"), toId3("wOrld")),
            mk_Set(toId3("WorlD"), toId3("hE llO"))
        )
        assertEquals(
            mk_Seq(toId3("He llo"), toId3("wOrLd")),
            mk_Seq(toId3("hEllO"), toId3("WOr  lD"))
        )
        assertEquals(
            mk_Mapping(mk_(toId3("Hel Lo"), toId3("wOrLd"))),
            mk_Mapping(mk_(toId3("hEllO"), toId3("WOr lD")))
        )
        assertEquals(
            mk_CaselessStringRecord(toId3("He lLo")),
            mk_CaselessStringRecord(toId3("hE llO"))
        )
    }

    private fun toCaselessString(string: String) = mk_CaselessString(*string.toCharArray())
    private fun toNonEmptyCaselessString(string: String) = mk_NonEmptyCaselessString(*string.toCharArray())
    private fun toId1(string: String) = mk_Id1(*string.toCharArray())
    private fun toId2(string: String) = mk_Id2(*string.toCharArray())
    private fun toId3(string: String) = mk_Id3(*string.toCharArray())

    // TODO -- comparison vs ancestors for record
    // TODO -- comparison with shared ancestor for record
    // TODO -- comparison with overridden ancestor for record

    @Test
    fun time_equals() {
        assertEquals(mk_Time(15, 23, 13, Time.Zone.GMT), mk_Time(16, 23, 13, Time.Zone.CET))
        assertEquals(mk_Time(15, 23, 13, Time.Zone.GMT), mk_Time(15, 23, 13, Time.Zone.GMT))
        assertEquals(mk_Time(10, 36, 18, Time.Zone.EST), mk_Time(15, 36, 18, Time.Zone.GMT))
    }

    @Test
    fun time_notEquals() {
        assertNotEquals(mk_Time(15, 23, 13, Time.Zone.GMT), mk_Time(17, 23, 13, Time.Zone.CET))
        assertNotEquals(mk_Time(15, 23, 13, Time.Zone.CET), mk_Time(15, 23, 13, Time.Zone.GMT))
        assertNotEquals(mk_Time(10, 36, 18, Time.Zone.EST), mk_Time(15, 36, 19, Time.Zone.GMT))
    }

    @Test
    fun time_hash() {
        assertEquals(1, mk_Set(mk_Time(15, 23, 13, Time.Zone.GMT), mk_Time(16, 23, 13, Time.Zone.CET)).card)
        assertEquals(1, mk_Set(mk_Time(15, 23, 13, Time.Zone.GMT), mk_Time(15, 23, 13, Time.Zone.GMT)).card)

        assertEquals(4, mk_Mapping(mk_(mk_Time(15, 23, 13, Time.Zone.GMT), 4))[mk_Time(15, 23, 13, Time.Zone.GMT)])
        assertEquals(4, mk_Mapping(mk_(mk_Time(15, 23, 13, Time.Zone.GMT), 4))[mk_Time(10, 23, 13, Time.Zone.EST)])
    }

    @Test
    fun time_nested() {
        assertEquals(
            mk_Set(mk_Time(15, 36, 18, Time.Zone.GMT), mk_Time(17, 38, 19, Time.Zone.CET)),
            mk_Set(mk_Time(11, 38, 19, Time.Zone.EST), mk_Time(10, 36, 18, Time.Zone.EST))
        )
        assertEquals(
            mk_Seq(mk_Time(15, 36, 18, Time.Zone.GMT), mk_Time(17, 38, 19, Time.Zone.CET)),
            mk_Seq(mk_Time(10, 36, 18, Time.Zone.EST), mk_Time(11, 38, 19, Time.Zone.EST))
        )
        assertEquals(
            mk_Mapping(mk_(mk_Time(15, 36, 18, Time.Zone.GMT), mk_Time(17, 38, 19, Time.Zone.CET))),
            mk_Mapping(mk_(mk_Time(10, 36, 18, Time.Zone.EST), mk_Time(11, 38, 19, Time.Zone.EST)))
        )
        assertEquals(
            mk_Flight(mk_Time(15, 23, 13, Time.Zone.GMT)),
            mk_Flight(mk_Time(16, 23, 13, Time.Zone.CET))
        )
    }


}