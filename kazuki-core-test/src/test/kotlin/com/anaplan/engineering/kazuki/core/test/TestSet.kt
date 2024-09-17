package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestSet {

    @Test
    fun inter() {
        assertEquals(mk_Set(), mk_Set<Int>() inter mk_Set())
        assertEquals(mk_Set(), mk_Set(1, 2, 3) inter mk_Set())
        assertEquals(mk_Set(2), mk_Set(1, 2, 3) inter mk_Set(2))
        assertEquals(mk_Set(2), mk_Set(1, 2, 3) inter mk_Set(2, 4))
        assertEquals(mk_Set(1, 2, 3), mk_Set(1, 2, 3) inter mk_Set(2, 4, 3, 1))
    }

    @Test
    fun union() {
        assertEquals(mk_Set(), mk_Set<Int>() union mk_Set())
        assertEquals(mk_Set(1, 2, 3), mk_Set(1, 2, 3) union mk_Set())
        assertEquals(mk_Set(1, 2, 3), mk_Set(1, 2, 3) union mk_Set(2))
        assertEquals(mk_Set(1, 2, 3, 4), mk_Set(1, 2, 3) union mk_Set(2, 4))
        assertEquals(mk_Set(1, 2, 3, 4), mk_Set(1, 2, 3) union mk_Set(2, 4, 3, 1))
    }

    @Test
    fun subset() {
        assertEquals(true, mk_Set<Nothing>() subset mk_Set())
        assertEquals(true, mk_Set<Int>() subset mk_Set(1, 2, 3))
        assertEquals(false, mk_Set<Int>(1) subset mk_Set())
        assertEquals(true, mk_Set(1) subset mk_Set(1, 2, 3))
        assertEquals(false, mk_Set(1, 2, 3) subset mk_Set(1))
        assertEquals(false, mk_Set(2) subset mk_Set(mk_Set(1, 2, 3), mk_Set(2), mk_Set()))
        assertEquals(true, mk_Set(mk_Set(3)) subset mk_Set(mk_Set(1, 2, 3), mk_Set(3), mk_Set()))
    }

    @Test
    fun cartesianProduct() {
        assertEquals(mk_Set(mk_(1, 2), mk_(1, 3)), mk_Set(1) x mk_Set(2, 3))
        assertEquals(mk_Set(), mk_Set(1, 2, 3) x mk_Set<Int>())
        assertEquals(mk_Set(mk_(3, 1), mk_(4, 1), mk_(3, 2), mk_(4, 2)), mk_Set(3, 4) x mk_Set<Int>(2, 1))
        assertEquals(mk_Set(mk_(1, 3), mk_(1, 4), mk_(2, 3), mk_(2, 4)), mk_Set(1, 2) x mk_Set<Int>(3, 4))
    }

    @Test
    fun arbitrary() {
        assertEquals(true, mk_Set(1, 2, 3).arbitrary() in mk_Set(1, 2, 3))
        assertEquals(false, mk_Set(1, 2, 3).arbitrary() in mk_Set(4, 5, 6))
        assertFailsWith<PreconditionFailure> { mk_Set<Int>().arbitrary() }
    }

    @Test
    fun card() {
        assertEquals(3, mk_Set(1, 2, 3).card)
        assertEquals(3, mk_Set(mk_Set(1, 2), 2, 3).card)
        assertEquals(0, mk_Set<Int>().card)
    }

    @Test
    fun dunion() {
        assertEquals(mk_Set<Int>(), dunion(mk_Set<Int>()))
        assertEquals(mk_Set(1, 2, 3), dunion(mk_Set(mk_Set(1, 2), mk_Set(3))))
        assertEquals(mk_Set(1, 2, 3), dunion(mk_Set(1, 2, 3)))
        assertEquals(mk_Set<Int>(), dunion(mk_Set<Int>()))
        assertEquals(mk_Set(mk_Set(1), 2, 3), dunion(mk_Set(mk_Set(1), 2), mk_Set(3)))
        assertEquals(mk_Set(mk_Set(1, 2), 3), dunion(mk_Set(mk_Set(1, 2), 3)))
    }

    @Test
    fun plus() {
        assertEquals(mk_Set(1), mk_Set<Int>().plus(1))
        assertEquals(mk_Set(1), mk_Set(1).plus(mk_Set<Int>()))
        assertEquals(mk_Set(1, 2), mk_Set<Int>(1).plus(mk_Set(1, 2)))
        assertEquals(mk_Set(1, 2), mk_Set<Int>(1).plus(2))
    }

    @Test
    fun minus() {
        assertEquals(mk_Set<Int>(), mk_Set<Int>().minus(1))
        assertEquals(mk_Set<Int>(), mk_Set(1).minus(1))
        assertEquals(mk_Set<Int>(2), mk_Set(1, 2).minus(mk_Set(1)))
        assertEquals(mk_Set(1), mk_Set(1).minus(mk_Set<Int>()))
        assertEquals(mk_Set<Int>(), mk_Set<Int>().minus(mk_Set<Int>()))
    }
}