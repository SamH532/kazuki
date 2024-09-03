package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.inter
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.core.union
import kotlin.test.Test
import kotlin.test.assertEquals

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
}