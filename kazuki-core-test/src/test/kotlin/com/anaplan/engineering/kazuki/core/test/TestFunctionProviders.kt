package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.A_Module.mk_A
import com.anaplan.engineering.kazuki.core.B_Module.mk_B
import com.anaplan.engineering.kazuki.core.C_Module.mk_C
import com.anaplan.engineering.kazuki.core.D_Module.mk_D
import org.junit.Test
import kotlin.test.assertEquals

class TestFunctionProviders {

    // TODO -- TupleTestGeneratorTask to generate tests for construction

    @Test
    fun directDcl() {
        assertEquals(mk_A(4), mk_A(3).functions.increment())
    }

    @Test
    fun inheritNothingOverridden() {
        assertEquals(mk_B(4), mk_B(3).functions.increment())

        causesInvariantFailure {
            mk_B(4).functions.increment()
        }
    }

    @Test
    fun overrideFunctionsClassButNotFunction() {
        assertEquals(mk_C(7, 8), mk_C(6, 8).functions.increment())
        assertEquals(mk_C(6, 7), mk_C(6, 8).functions.decrement())
    }

    @Test
    fun inheritOverridenWithNothingOverridden() {
        assertEquals(mk_D(2, 2, 3), mk_D(1, 2, 3).functions.increment())
        assertEquals(mk_D(2, 1, 3), mk_D(1, 2, 3).functions.decrement())

//        causesInvariantFailure {
//            mk_B(4).functions.increment()
//        }
    }
}