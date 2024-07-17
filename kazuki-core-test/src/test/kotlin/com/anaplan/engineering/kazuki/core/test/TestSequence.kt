package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.Sequence1Extension_Module.mk_Sequence1Extension
import com.anaplan.engineering.kazuki.core.SequenceExtension_Module.mk_SequenceExtension
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test standard sequence operations on implementations of all mapping types
 */
@RunWith(Parameterized::class)
class TestSequence(
    private val allowsEmpty: Boolean,
    private val creator: (Collection<Int>) -> Sequence<Int>
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun creators(): Collection<Array<Any?>> =
            listOf(
                arrayOf(true, { m: Collection<Int> -> mk_Seq(*m.toTypedArray()) }),
                arrayOf(false, { m: Collection<Int> -> mk_Seq1(*m.toTypedArray()) }),
                arrayOf(true, { m: Collection<Int> -> mk_SequenceExtension(*m.toTypedArray()) }),
                arrayOf(false, { m: Collection<Int> -> mk_Sequence1Extension(*m.toTypedArray()) }),
            )
    }

    private fun create(vararg m: Int) = creator.invoke(m.toList())

    @Test
    fun len() {
        if (allowsEmpty) {
            assertEquals(0, create().len)
        }
        assertEquals(1, create(1).len)
        assertEquals(3, create(1, 2, 1).len)
    }

    @Test
    fun subseq() {
        if (allowsEmpty) {
            assertEquals(true, create() subseq create())
            assertEquals(true, create() subseq create(1))
            assertEquals(false, create(1) subseq create())
        }
        assertEquals(false, create(1) subseq create(2, 3, 4))
        assertEquals(true, create(1, 2) subseq create(1, 2, 3, 4))
        assertEquals(true, create(3, 4) subseq create(1, 2, 3, 4))
        assertEquals(false, create(4, 3) subseq create(1, 2, 3, 4))
        assertEquals(true, create(3, 4) subseq create(3, 4, 3, 4))
        assertEquals(true, create(4, 3) subseq create(3, 4, 3, 4))
        assertEquals(true, create(3, 3, 3) subseq create(3, 3, 3, 3))
        assertEquals(true, create(3, 3, 3, 3) subseq create(3, 3, 3, 3))
        assertEquals(false, create(3, 3, 3, 3) subseq create(3, 3, 3))
    }

    @Test
    fun insert() {
        if (allowsEmpty) {
            assertEquals(create(6), create().insert(6, 1))
        }
        assertEquals(create(6, 7, 8), create(7, 8).insert(6, 1))
        assertEquals(create(6, 7, 8), create(7, 8).insert(6, 1))
        assertEquals(create(6, 7, 8), create(7, 8).insert(6, 1))
        assertEquals(create(6, 7, 8), create(7, 8).insert(6, 1))
        assertEquals(create(7, 6, 8), create(7, 8).insert(6, 2))
        assertEquals(create(7, 8, 6), create(7, 8).insert(6, 3))
        causesPreconditionFailure { create(7, 8).insert(6, 0) }
        causesPreconditionFailure { create(7, 8).insert(6, 4) }
        assertEquals(create(7, 7, 8), create(7, 8).insert(7, 1))
    }

    @Test
    fun drop() {
        if (allowsEmpty) {
            assertEquals(create(), create().drop(1))
            assertEquals(create(), create(1).drop(1))
            assertEquals(create(), create(1).drop(2))
        } else {
            causesInvariantFailure { create(1).drop(1) }
        }
        assertEquals(create(6, 7, 8), create(5, 6, 7, 8).drop(1))
        assertEquals(create(7, 8), create(5, 6, 7, 8).drop(2))
    }
    
    @Test
    fun take() {
        if (allowsEmpty) {
            assertEquals(create(), create().take(1))
            assertEquals(create(), create(1).take(0))
            assertEquals(create(1), create(1).take(1))
            assertEquals(create(1), create(1).take(2))
        } else {
            causesInvariantFailure { create(1).take(0) }
        }
        assertEquals(create(5), create(5, 6, 7, 8).take(1))
        assertEquals(create(5, 6), create(5, 6, 7, 8).take(2))
        assertEquals(create(5, 6, 7, 8), create(5, 6, 7, 8).take(5))
    }

    @Test
    fun reverse() {
        if (allowsEmpty) {
            assertEquals(create(), create().reverse())
        }
        assertEquals(create(5), create(5).reverse())
        assertEquals(create(5, 6, 6, 5), create(5, 6, 6, 5).reverse())
        assertEquals(create(5, 8, 6, 5), create(5, 6, 8, 5).reverse())
        assertEquals(create(8, 7, 6, 5), create(5, 6, 7, 8).reverse())
    }

    @Test
    fun domRestrictTo() {
        if (allowsEmpty) {
            assertEquals(create(), create() drt mk_Set(1))
            assertEquals(create(), create(5) drt mk_Set())
            assertEquals(create(), create(5) drt mk_Set(2))
        }
        assertEquals(create(5), create(5) drt mk_Set(1))
        assertEquals(create(6, 5), create(5, 6, 6, 5) drt mk_Set(2, 4))
        assertEquals(create(5, 5), create(5, 6, 8, 5) drt mk_Set(1, 4))
        assertEquals(create(8), create(5, 6, 7, 8) domRestrictTo mk_Set(4))
    }
    
    @Test
    fun domSubtract() {
        if (allowsEmpty) {
            assertEquals(create(), create() dsub mk_Set(1))
            assertEquals(create(), create(5) dsub mk_Set(1))
        }
        assertEquals(create(5), create(5) dsub mk_Set())
        assertEquals(create(5), create(5) dsub mk_Set(2))
        assertEquals(create(5, 6), create(5, 6, 6, 5) dsub mk_Set(2, 4))
        assertEquals(create(6, 8), create(5, 6, 8, 5) dsub mk_Set(1, 4))
        assertEquals(create(5, 6, 7), create(5, 6, 7, 8) domSubtract mk_Set(4))
    }

    @Test
    fun rngRestrictTo() {
        if (allowsEmpty) {
            assertEquals(create(), create() rrt mk_Set(1))
            assertEquals(create(), create(5) rrt mk_Set())
            assertEquals(create(), create(5) rrt mk_Set(2))
        }
        assertEquals(create(5), create(5) rrt mk_Set(5))
        assertEquals(create(6, 6), create(5, 6, 6, 5) rrt mk_Set(6))
        assertEquals(create(5, 6, 5), create(5, 6, 8, 5) rrt mk_Set(6, 5))
        assertEquals(create(8), create(5, 6, 7, 8) rngRestrictTo mk_Set(8))
    }
    
    @Test
    fun rngSubtract() {
        if (allowsEmpty) {
            assertEquals(create(), create() rsub mk_Set(1))
            assertEquals(create(), create(5) rsub mk_Set(5))
            assertEquals(create(), create(5) rsub mk_Set(5))
        }
        assertEquals(create(5), create(5) rsub mk_Set(2))
        assertEquals(create(5, 5), create(5, 6, 6, 5) rsub mk_Set(6))
        assertEquals(create(8), create(5, 6, 8, 5) rsub mk_Set(6, 5))
        assertEquals(create(5, 6, 7), create(5, 6, 7, 8) rngSubtract mk_Set(8))
    }

    @Test
    fun cat() {
        if (allowsEmpty) {
            assertEquals(create(1), create() cat create(1))
            assertEquals(create(1), create(1) cat create())
            assertEquals(create(), create() cat create())
            assertEquals(create(1), create() cat mk_Seq(1))
            assertEquals(mk_Seq(1), mk_Seq(1) cat create())
        }
        assertEquals(create(2, 1), create(2) cat mk_Seq(1))
        assertEquals(mk_Seq(1, 2), mk_Seq(1) cat create(2))
        assertEquals(create(5, 5), create(5) cat create(5))
        assertEquals(create(5, 6, 6, 5, 6), create(5, 6, 6, 5) cat create(6))
        assertEquals(create(5, 6, 8, 5, 6, 5), create(5, 6, 8, 5) cat create(6, 5))
    }
    
    @Test
    fun plus_seq() {
        if (allowsEmpty) {
            assertEquals(create(1), create() + create(1))
            assertEquals(create(1), create(1) + create())
            assertEquals(create(), create() + create())
            assertEquals(create(1), create() + mk_Seq(1))
            assertEquals(mk_Seq(1), mk_Seq(1) + create())
        }
        assertEquals(create(2, 1), create(2) + mk_Seq(1))
        assertEquals(mk_Seq(1, 2), mk_Seq(1) + create(2))
        assertEquals(create(5, 5), create(5) + create(5))
        assertEquals(create(5, 6, 6, 5, 6), create(5, 6, 6, 5) + create(6))
        assertEquals(create(5, 6, 8, 5, 6, 5), create(5, 6, 8, 5) + create(6, 5))
    }

    @Test
    fun plus_elem() {
        if (allowsEmpty) {
            assertEquals(create(1), create() + 1)
        }
        assertEquals(create(2, 1), create(2) + 1)
        assertEquals(create(5, 5), create(5) + 5)
        assertEquals(create(5, 6, 6, 5, 6), create(5, 6, 6, 5) + 6)
    }

    @Test
    fun first() {
        if (allowsEmpty) {
            causesPreconditionFailure { create().first() }
        }
        assertEquals(1, create(1).first())
        assertEquals(2, create(2, 5, 6).first())
    }
    
    @Test
    fun last() {
        if (allowsEmpty) {
            causesPreconditionFailure { create().last() }
        }
        assertEquals(1, create(1).last())
        assertEquals(6, create(2, 5, 6).last())
    }

    @Test
    fun head() {
        if (allowsEmpty) {
            causesPreconditionFailure { create().head() }
        }
        assertEquals(1, create(1).head())
        assertEquals(2, create(2, 5, 6).head())
    }

    @Test
    fun tail() {
        if (allowsEmpty) {
            assertEquals(create(), create().tail())
            assertEquals(create(), create(1).tail())
        }
        assertEquals(create(5, 6), create(2, 5, 6).tail())
    }
}
