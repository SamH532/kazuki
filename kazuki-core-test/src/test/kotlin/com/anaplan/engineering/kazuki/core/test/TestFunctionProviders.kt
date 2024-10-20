package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.A_Module.mk_A
import com.anaplan.engineering.kazuki.core.B_Module.mk_B
import com.anaplan.engineering.kazuki.core.C_Module.mk_C
import com.anaplan.engineering.kazuki.core.D_Module.mk_D
import com.anaplan.engineering.kazuki.core.E_Module.mk_E
import com.anaplan.engineering.kazuki.core.GA_Module.mk_GA
import com.anaplan.engineering.kazuki.core.GB1_Module.mk_GB1
import com.anaplan.engineering.kazuki.core.GB2_Module.mk_GB2
import com.anaplan.engineering.kazuki.core.GC1_Module.mk_GC1
import com.anaplan.engineering.kazuki.core.GC2_Module.mk_GC2
import com.anaplan.engineering.kazuki.core.GD1_Module.mk_GD1
import com.anaplan.engineering.kazuki.core.GD2_Module.mk_GD2
import com.anaplan.engineering.kazuki.core.GD3_Module.mk_GD3
import com.anaplan.engineering.kazuki.core.GE1_Module.mk_GE1
import com.anaplan.engineering.kazuki.core.GE2_Module.mk_GE2
import com.anaplan.engineering.kazuki.core.GE3_Module.mk_GE3
import com.anaplan.engineering.kazuki.core.GE4_Module.mk_GE4
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Mapping
import org.junit.Test
import kotlin.test.assertEquals

class TestFunctionProviders {

    // TODO -- TupleTestGeneratorTask to generate tests for construction

    private val p = "P"

    @Test
    fun directDcl() {
        assertEquals(mk_A(4), mk_A(3).functions.increment())

        assertEquals(mk_GA(mk_Mapping(mk_(p, 2))), mk_GA(mk_Mapping(mk_(p, 1))).functions.increment(p))
    }

    @Test
    fun inheritNothingOverridden() {
        assertEquals(mk_B(4), mk_B(3).functions.increment())

        causesInvariantFailure {
            mk_B(4).functions.increment()
        }

        assertEquals(mk_GB1(mk_Mapping(mk_(p, 2))), mk_GB1(mk_Mapping(mk_(p, 1))).functions.increment(p))

        causesInvariantFailure {
            mk_GB1(mk_Mapping(mk_(p, 4))).functions.increment(p)
        }

        assertEquals(mk_GB2(mk_Mapping(mk_(p, 2))), mk_GB2(mk_Mapping(mk_(p, 1))).functions.increment(p))

        causesInvariantFailure {
            mk_GB2(mk_Mapping(mk_(p, 4))).functions.increment(p)
        }
    }

    @Test
    fun overrideFunctionsClassButNotFunction() {
        assertEquals(mk_C(7, 8), mk_C(6, 8).functions.increment())
        assertEquals(mk_C(6, 7), mk_C(6, 8).functions.decrement())

        assertEquals(mk_GC1(mk_Mapping(mk_(p, 2)), 8), mk_GC1(mk_Mapping(mk_(p, 1)), 8).functions.increment(p))
        assertEquals(mk_GC1(mk_Mapping(mk_(p, 1)), 7), mk_GC1(mk_Mapping(mk_(p, 1)), 8).functions.decrement())
        
        assertEquals(mk_GC2(mk_Mapping(mk_(p, 2)), 8), mk_GC2(mk_Mapping(mk_(p, 1)), 8).functions.increment(p))
        assertEquals(mk_GC2(mk_Mapping(mk_(p, 1)), 7), mk_GC2(mk_Mapping(mk_(p, 1)), 8).functions.decrement())
    }

    @Test
    fun inheritOverriddenWithNothingOverridden() {
        assertEquals(mk_D(2, 2, 3), mk_D(1, 2, 3).functions.increment())
        assertEquals(mk_D(1, 1, 3), mk_D(1, 2, 3).functions.decrement())

        assertEquals(mk_GD1(mk_Mapping(mk_(p, 2)), 2, 3), mk_GD1(mk_Mapping(mk_(p, 1)), 2, 3).functions.increment(p))
        assertEquals(mk_GD1(mk_Mapping(mk_(p, 1)), 1, 3), mk_GD1(mk_Mapping(mk_(p, 1)), 2, 3).functions.decrement())
        
        assertEquals(mk_GD2(mk_Mapping(mk_(p, 2)), 2, 3), mk_GD2(mk_Mapping(mk_(p, 1)), 2, 3).functions.increment(p))
        assertEquals(mk_GD2(mk_Mapping(mk_(p, 1)), 1, 3), mk_GD2(mk_Mapping(mk_(p, 1)), 2, 3).functions.decrement())

        assertEquals(mk_GD3(mk_Mapping(mk_(p, 2)), 2, 3), mk_GD3(mk_Mapping(mk_(p, 1)), 2, 3).functions.increment(p))
        assertEquals(mk_GD3(mk_Mapping(mk_(p, 1)), 1, 3), mk_GD3(mk_Mapping(mk_(p, 1)), 2, 3).functions.decrement())
    }

    @Test
    fun inheritOverriddenWithFunctionOverridden() {
        assertEquals(mk_E(2, 2, 3, 10), mk_E(1, 2, 3, 10).functions.increment())
        assertEquals(mk_E(1, 1, 3, 9), mk_E(1, 2, 3, 10).functions.decrement())

        assertEquals(mk_GE1(mk_Mapping(mk_(p, 2)), 2, 3, 6), mk_GE1(mk_Mapping(mk_(p, 1)), 2, 3, 6).functions.increment(p))
        assertEquals(mk_GE1(mk_Mapping(mk_(p, 1)), 1, 3, 5), mk_GE1(mk_Mapping(mk_(p, 1)), 2, 3, 6).functions.decrement())
      
        assertEquals(mk_GE2(mk_Mapping(mk_(p, 2)), 2, 3, 6), mk_GE2(mk_Mapping(mk_(p, 1)), 2, 3, 6).functions.increment(p))
        assertEquals(mk_GE2(mk_Mapping(mk_(p, 1)), 1, 3, 5), mk_GE2(mk_Mapping(mk_(p, 1)), 2, 3, 6).functions.decrement())
      
        assertEquals(mk_GE3(mk_Mapping(mk_(p, 2)), 2, 3, 6), mk_GE3(mk_Mapping(mk_(p, 1)), 2, 3, 6).functions.increment(p))
        assertEquals(mk_GE3(mk_Mapping(mk_(p, 1)), 1, 3, 5), mk_GE3(mk_Mapping(mk_(p, 1)), 2, 3, 6).functions.decrement())
        
        assertEquals(mk_GE4(mk_Mapping(mk_(p, 2)), 2, 3, 6), mk_GE4(mk_Mapping(mk_(p, 1)), 2, 3, 6).functions.increment(p))
        assertEquals(mk_GE4(mk_Mapping(mk_(p, 1)), 1, 3, 5), mk_GE4(mk_Mapping(mk_(p, 1)), 2, 3, 6).functions.decrement())
    }
}