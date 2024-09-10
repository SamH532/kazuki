package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.InjectiveMapping1Extension_Module.mk_InjectiveMapping1Extension
import com.anaplan.engineering.kazuki.core.InjectiveMappingExtension_Module.mk_InjectiveMappingExtension
import com.anaplan.engineering.kazuki.core.Mapping1Extension_Module.mk_Mapping1Extension
import com.anaplan.engineering.kazuki.core.MappingExtension_Module.mk_MappingExtension
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test standard map operations on implementations of all mapping types
 */
@RunWith(Parameterized::class)
class TestMapping(
    private val allowsEmpty: Boolean,
    private val injective: Boolean,
    private val creator: (Collection<Tuple2<Int, Int>>) -> Mapping<Int, Int>
) {

    // TODO confirm invariant vs precondition failures

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun creators(): Collection<Array<Any?>> =
            listOf(
                arrayOf(true, false, { m: Collection<Tuple2<Int, Int>> -> mk_Mapping(*m.toTypedArray()) }),
                arrayOf(false, false, { m: Collection<Tuple2<Int, Int>> -> mk_Mapping1(*m.toTypedArray()) }),
                arrayOf(true, true, { m: Collection<Tuple2<Int, Int>> -> mk_InjectiveMapping(*m.toTypedArray()) }),
                arrayOf(false, true, { m: Collection<Tuple2<Int, Int>> -> mk_InjectiveMapping1(*m.toTypedArray()) }),
                arrayOf(true, false, { m: Collection<Tuple2<Int, Int>> -> mk_MappingExtension(*m.toTypedArray()) }),
                arrayOf(false, false, { m: Collection<Tuple2<Int, Int>> -> mk_Mapping1Extension(*m.toTypedArray()) }),
                arrayOf(
                    true,
                    true,
                    { m: Collection<Tuple2<Int, Int>> -> mk_InjectiveMappingExtension(*m.toTypedArray()) }),
                arrayOf(
                    false,
                    true,
                    { m: Collection<Tuple2<Int, Int>> -> mk_InjectiveMapping1Extension(*m.toTypedArray()) }),
            )
    }

    private fun create(vararg m: Tuple2<Int, Int>) = creator.invoke(m.toList())

    @Test
    fun card() {
        if (allowsEmpty) {
            assertEquals(0, create().card)
        }
        if (!injective) {
            assertEquals(2, create(mk_(1, 1), mk_(2, 1)).card)
        }
        assertEquals(1, create(mk_(1, 1)).card)
        // TODO - should be disallowed?
        assertEquals(1, create(mk_(1, 1), mk_(1, 2)).card)
    }

    @Test
    fun dom() {
        if (allowsEmpty) {
            assertEquals(mk_Set(), create().dom)
        }
        if (!injective) {
            assertEquals(mk_Set(1, 2), create(mk_(1, 1), mk_(2, 1)).dom)
        }
        assertEquals(mk_Set(1), create(mk_(1, 1)).dom)
        assertEquals(mk_Set(1, 2), create(mk_(1, 1), mk_(2, 2)).dom)
    }

    @Test
    fun rng() {
        if (allowsEmpty) {
            assertEquals(mk_Set(), create().rng)
        }
        if (!injective) {
            assertEquals(mk_Set(1), create(mk_(1, 1), mk_(2, 1)).rng)
        }
        assertEquals(mk_Set(1), create(mk_(1, 1)).rng)
        assertEquals(mk_Set(1, 2), create(mk_(1, 1), mk_(2, 2)).rng)
    }

    @Test
    fun domRestrictTo() {
        if (allowsEmpty) {
            assertEquals(create(), create() domRestrictTo mk_Set())
            assertEquals(create(), create() domRestrictTo mk_Set(1))
            assertEquals(create(), create(mk_(1, 2)) domRestrictTo mk_Set())
            assertEquals(create(), create(mk_(1, 2)) domRestrictTo mk_Set(2))
        } else {
            causesPreconditionFailure { create(mk_(1, 2)) domRestrictTo mk_Set() }
            causesPreconditionFailure { create(mk_(1, 2)) domRestrictTo mk_Set(2) }
        }
        assertEquals(create(mk_(1, 2)), create(mk_(1, 2)) domRestrictTo mk_Set(1))
        assertEquals(
            create(mk_(1, 2), mk_(3, 4)),
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domRestrictTo mk_Set(1, 3)
        )
        assertEquals(
            create(mk_(3, 4)),
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domRestrictTo mk_Set(4, 3)
        )
    }

    @Test
    fun domSubtract() {
        if (allowsEmpty) {
            assertEquals(create(), create() domSubtract mk_Set())
            assertEquals(create(), create() domSubtract mk_Set(1))
            assertEquals(create(), create(mk_(1, 2)) domSubtract mk_Set(1))
        } else {
            causesPreconditionFailure { create(mk_(1, 2)) domSubtract mk_Set(1) }
        }
        assertEquals(create(mk_(1, 2)), create(mk_(1, 2)) domSubtract mk_Set())
        assertEquals(create(mk_(1, 2)), create(mk_(1, 2)) domSubtract mk_Set(2))
        assertEquals(
            create(mk_(2, 3)),
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domSubtract mk_Set(1, 3)
        )
        assertEquals(
            create(mk_(1, 2), mk_(2, 3)),
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domSubtract mk_Set(3)
        )
    }

    @Test
    fun rngRestrictTo() {
        if (allowsEmpty) {
            assertEquals(create(), create() rngRestrictTo mk_Set())
            assertEquals(create(), create() rngRestrictTo mk_Set(1))
            assertEquals(create(), create(mk_(1, 2)) rngRestrictTo mk_Set())
            assertEquals(create(), create(mk_(1, 2)) rngRestrictTo mk_Set(1))
        } else {
            causesPreconditionFailure { create(mk_(1, 2)) rngRestrictTo mk_Set() }
            causesPreconditionFailure { create(mk_(1, 2)) rngRestrictTo mk_Set(1) }
        }
        assertEquals(create(mk_(1, 2)), create(mk_(1, 2)) rngRestrictTo mk_Set(2))
        assertEquals(
            create(mk_(1, 2), mk_(3, 4)),
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngRestrictTo mk_Set(2, 4)
        )
        assertEquals(
            create(mk_(3, 4)),
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngRestrictTo mk_Set(4, 6)
        )
    }

    @Test
    fun rngSubtract() {
        if (allowsEmpty) {
            assertEquals(create(), create() rngSubtract mk_Set())
            assertEquals(create(), create() rngSubtract mk_Set(1))
            assertEquals(create(), create(mk_(1, 2)) rngSubtract mk_Set(2))
        }
        assertEquals(create(mk_(1, 2)), create(mk_(1, 2)) rngSubtract mk_Set())
        assertEquals(create(mk_(1, 2)), create(mk_(1, 2)) rngSubtract mk_Set(1))
        assertEquals(
            create(mk_(2, 3)),
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngSubtract mk_Set(2, 4)
        )
        assertEquals(
            create(mk_(1, 2), mk_(2, 3)),
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngSubtract mk_Set(4, 6)
        )
    }

    @Test
    fun in_pos() {
        if (allowsEmpty) {
            assertEquals(false, mk_(1, 1) in create())
        }
        if (!injective) {
            assertEquals(false, mk_(1, 2) in create(mk_(1, 1), mk_(2, 1)))
            assertEquals(true, mk_(2, 1) in create(mk_(1, 1), mk_(2, 1)))
        }
        assertEquals(true, mk_(1, 1) in create(mk_(1, 1)))
    }

    @Test
    fun in_neg() {
        if (allowsEmpty) {
            assertEquals(true, mk_(1, 1) !in create())
        }
        if (!injective) {
            assertEquals(true, mk_(1, 2) !in create(mk_(1, 1), mk_(2, 1)))
            assertEquals(false, mk_(2, 1) !in create(mk_(1, 1), mk_(2, 1)))
        }
        assertEquals(false, mk_(1, 1) !in create(mk_(1, 1)))
    }

    @Test
    fun equals() {
        if (allowsEmpty) {
            assertEquals(create(), create())
        }
        assertEquals(create(mk_(1, 2)), create(mk_(1, 2)))
        assertEquals(create(mk_(1, 2), mk_(2, 3)), create(mk_(2, 3), mk_(1, 2)))
    }

    @Test
    fun plus_singular() {
        if (allowsEmpty) {
            assertEquals(create(mk_(1, 2)), create() + mk_(1, 2))
        }
        if (injective) {
            causesPreconditionFailure { create(mk_(1, 2)) + mk_(3, 2) }
        } else {
            assertEquals(create(mk_(1, 2), mk_(3, 2)), create(mk_(1, 2)) + mk_(3, 2))
        }
        assertEquals(create(mk_(1, 2), mk_(2, 3)), create(mk_(1, 2)) + mk_(2, 3))
        assertEquals(create(mk_(1, 2)), create(mk_(1, 2)) + mk_(1, 2))
        causesPreconditionFailure { create(mk_(1, 2)) + mk_(1, 3) }
    }

    @Test
    fun plus_otherOfSameType() {
        if (allowsEmpty) {
            assertEquals(create(mk_(1, 2)), create() + create(mk_(1, 2)))
        }
        if (injective) {
            causesPreconditionFailure { create(mk_(1, 2), mk_(5, 6)) + create(mk_(3, 4), mk_(4, 6)) }
        } else {
            assertEquals(
                create(mk_(1, 2), mk_(5, 6), mk_(3, 4), mk_(4, 6)),
                create(mk_(1, 2), mk_(5, 6)) + create(mk_(3, 4), mk_(4, 6))
            )
        }
        assertEquals(create(mk_(1, 2), mk_(2, 3)), create(mk_(1, 2)) + create(mk_(2, 3)))
        assertEquals(
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4), mk_(5, 6)),
            create(mk_(1, 2), mk_(3, 4)) + create(mk_(2, 3), mk_(5, 6))
        )
        assertEquals(
            create(mk_(1, 2), mk_(5, 6), mk_(3, 4)),
            create(mk_(1, 2), mk_(5, 6)) + create(mk_(3, 4), mk_(1, 2))
        )
        causesPreconditionFailure { create(mk_(1, 2), mk_(5, 6)) + create(mk_(3, 4), mk_(1, 3)) }
    }

    @Test
    fun plus_set() {
        if (allowsEmpty) {
            assertEquals(create(mk_(1, 2)), create() + mk_Set(mk_(1, 2)))
        }
        if (injective) {
            causesPreconditionFailure { create(mk_(1, 2), mk_(5, 6)) + mk_Set(mk_(3, 4), mk_(4, 6)) }
        } else {
            assertEquals(
                create(mk_(1, 2), mk_(5, 6), mk_(3, 4), mk_(4, 6)),
                create(mk_(1, 2), mk_(5, 6)) + mk_Set(mk_(3, 4), mk_(4, 6))
            )
        }
        assertEquals(create(mk_(1, 2), mk_(2, 3)), create(mk_(1, 2)) + mk_Set(mk_(2, 3)))
        assertEquals(
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4), mk_(5, 6)),
            create(mk_(1, 2), mk_(3, 4)) + mk_Set(mk_(2, 3), mk_(5, 6))
        )
        assertEquals(
            create(mk_(1, 2), mk_(5, 6), mk_(3, 4)),
            create(mk_(1, 2), mk_(5, 6)) + mk_Set(mk_(3, 4), mk_(1, 2))
        )
        causesPreconditionFailure { create(mk_(1, 2), mk_(5, 6)) + mk_Set(mk_(3, 4), mk_(1, 3)) }
    }

    @Test
    fun times_singular_ok() {
        if (allowsEmpty) {
            assertEquals(create(mk_(1, 2)), create() * mk_(1, 2))
        }
        assertEquals(create(mk_(1, 2), mk_(2, 3)), create(mk_(1, 2)) * mk_(2, 3))
    }

    @Test
    fun times_singular_overlap() {
        assertEquals(create(mk_(1, 2)), create(mk_(1, 2)) * mk_(1, 2))
    }

    @Test
    fun times_singular_conflict() {
        assertEquals(create(mk_(1, 3)), create(mk_(1, 2)) * mk_(1, 3))
    }

    @Test
    fun times_plural_ok() {
        if (allowsEmpty) {
            assertEquals(create(mk_(1, 2)), create() * create(mk_(1, 2)))
        }
        assertEquals(create(mk_(1, 2), mk_(2, 3)), create(mk_(1, 2)) * create(mk_(2, 3)))
        assertEquals(
            create(mk_(1, 2), mk_(2, 3), mk_(3, 4), mk_(5, 6)),
            create(mk_(1, 2), mk_(3, 4)) * create(mk_(2, 3), mk_(5, 6))
        )
    }

    @Test
    fun times_plural_intersection() {
        assertEquals(
            create(mk_(1, 2), mk_(5, 6), mk_(3, 4)),
            create(mk_(1, 2), mk_(5, 6)) * create(mk_(3, 4), mk_(1, 2))
        )
    }

    @Test
    fun times_plural_conflict() {
        assertEquals(
            create(mk_(1, 3), mk_(5, 6), mk_(3, 4)),
            create(mk_(1, 2), mk_(5, 6)) * create(mk_(3, 4), mk_(1, 3))
        )
    }
}