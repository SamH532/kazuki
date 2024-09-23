package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.Set1Extension_Module.mk_Set1Extension
import com.anaplan.engineering.kazuki.core.SetExtension_Module.mk_SetExtension
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class TestSet(
    private val allowsEmpty: Boolean,
    private val creator: (Collection<*>) -> Set<*>
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun creators() =
            listOf(
                arrayOf(true, { m: Collection<*> -> mk_Set(*m.toTypedArray()) }),
                arrayOf(false, { m: Collection<*> -> mk_Set1(*m.toTypedArray()) }),
                arrayOf(true, { m: Collection<*> -> as_Set(m) }),
                arrayOf(false, { m: Collection<*> -> as_Set1(m) }),
                arrayOf(true, { m: Collection<*> -> mk_SetExtension(*m.toTypedArray()) }),
                arrayOf(false, { m: Collection<*> -> mk_Set1Extension(*m.toTypedArray()) }),
            )
    }

    private fun create(vararg m: Any) = creator.invoke(m.toList())

    @Test
    fun inter() {
        if (allowsEmpty) {
            assertEquals(create(), create() inter create())
            assertEquals(create(), create(1, 2, 3) inter create())
            assertEquals(create(), create(1) inter create(2))
        } else {
            // inter creates a set of the same type as the first input
            causesPreconditionFailure { create(1) inter create(2) }
        }
        assertEquals(create(2), create(1, 2, 3) inter create(2))
        assertEquals(create(2), create(1, 2, 3) inter create(2, 4))
        assertEquals(create(1, 2, 3), create(1, 2, 3) inter create(2, 4, 3, 1))
    }

    @Test
    fun union() {
        if (allowsEmpty) {
            assertEquals(create(), create() union create())
        }
        assertEquals(create(1, 2, 3), create(1, 2, 3) union create(2))
        assertEquals(create(1, 2, 3, 4), create(1, 2, 3) union create(2, 4))
        assertEquals(create(1, 2, 3, 4), create(1, 2, 3) union create(2, 4, 3, 1))
        assertEquals(create(1, 2, 3), create(1, 2, 3) union mk_Set())

    }

    @Test
    fun subset() {
        if (allowsEmpty) {
            assertEquals(true, create() subset create())
        }
        assertEquals(false, create(2) subset create(create(1, 2, 3), create(2), mk_Set<Int>()))
        assertEquals(true, create(create(3)) subset create(create(1, 2, 3), create(3), mk_Set<Int>()))
        assertEquals(true, mk_Set<Int>() subset create(1, 2, 3))
        assertEquals(false, create(1) subset mk_Set())
        assertEquals(true, create(1) subset create(1, 2, 3))
        assertEquals(false, create(1, 2, 3) subset create(1))
    }

    @Test
    fun cartesianProduct() {
        if (allowsEmpty) {
            assertEquals(create(), create(1, 2, 3) x create())
        }
        assertEquals(create(mk_(1, 2), mk_(1, 3)), create(1) x create(2, 3))
        assertEquals(create(mk_(3, 1), mk_(4, 1), mk_(3, 2), mk_(4, 2)), create(3, 4) x create(2, 1))
        assertEquals(create(mk_(1, 3), mk_(1, 4), mk_(2, 3), mk_(2, 4)), create(1, 2) x create(3, 4))
    }

    @Test
    fun arbitrary() {
        if (allowsEmpty) {
            causesPreconditionFailure { create().arbitrary() }
        }
        assertEquals(true, create(1, 2, 3).arbitrary() in create(1, 2, 3))
        assertEquals(false, create(1, 2, 3).arbitrary() in create(4, 5, 6))
    }

    @Test
    fun card() {
        if (allowsEmpty) {
            assertEquals(0, create().card)
        }
        assertEquals(3, create(1, 2, 3).card)
        assertEquals(3, create(create(1, 2), 2, 3).card)
    }

    @Test
    fun dunion() {
        if (allowsEmpty) {
            assertEquals(create(), dunion(create()))
            assertEquals(create(1), dunion(create(), create(1)))
        }
        assertEquals(create(1, 2, 3), dunion(mk_Set1(create(1, 2), create(3))))
        assertEquals(create(1, 2, 3), dunion(create(1, 2, 3)))
        assertEquals(create(create(1), 2, 3), dunion(mk_Set1(create(1), 2), create(3)))
        assertEquals(create(create(1, 2), 3), dunion(create(create(1, 2), 3)))
    }

    @Test
    fun plusItem() {
        if (allowsEmpty) {
            assertEquals(create(1), create() + 1)
        }
        assertEquals(create(1, 2), create(1) + 2)
        assertEquals(create(1, 2), create(1, 2) + 2)
    }

    @Test
    fun plusSet() {
        if (allowsEmpty) {
            assertEquals(create(1), create(1) + create())
        }
        assertEquals(create(1, 2), create(1) + create(1, 2))
    }

    @Test
    fun minusItem() {
        if (allowsEmpty) {
            assertEquals(create(), create() - 1)
            assertEquals(create(), create(1) - 1)
        } else {
            causesPreconditionFailure { create(1) - 1 }
        }
        assertEquals(create(2), create(1, 2) - 1)
        assertEquals(create(1, 2), create(1, 2) - 3)
    }

    @Test
    fun minusSet() {
        if (allowsEmpty) {
            assertEquals(create(), create() - create())
            assertEquals(create(), create(1, 2) - create(1, 2, 3))
        } else {
            causesPreconditionFailure { create(1) - create(1, 2) }
        }
        assertEquals(create(1), create(1) - mk_Set())
        assertEquals(create(1), create(1, 2, 3) - create(2, 3))
        assertEquals(create(1, 2), create(1, 2, 3) - create(3, 4))
    }
}