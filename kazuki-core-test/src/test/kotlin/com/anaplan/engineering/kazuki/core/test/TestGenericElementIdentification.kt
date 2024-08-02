package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.test.DeferredInstantiateWithOthers_Module.mk_DeferredInstantiateWithOthers
import com.anaplan.engineering.kazuki.core.test.DeferredInstantiate_Module.mk_DeferredInstantiate
import com.anaplan.engineering.kazuki.core.test.IntDeferredInstantiateWithOthers_Module.mk_IntDeferredInstantiateWithOthers
import com.anaplan.engineering.kazuki.core.test.IntDeferredInstantiate_Module.mk_IntDeferredInstantiate
import com.anaplan.engineering.kazuki.core.test.IntOrderedSet_Module.mk_IntOrderedSet
import com.anaplan.engineering.kazuki.core.test.OrderedSet_Module.mk_OrderedSet
import com.anaplan.engineering.kazuki.core.test.SomeType_Module.mk_SomeType
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@Module
interface OrderedSet<T> : Sequence<T> {

    @Invariant
    fun noDuplicates() = len == elems.card

}

@Module
interface IntOrderedSet : OrderedSet<Int> {

}

@Module
interface SomeType : IntOrderedSet {

}

@Module
interface DeferredInstantiate<C> : OrderedSet<C> {

}

@Module
interface DeferredInstantiateWithOthers<A, B, C> : OrderedSet<B> {

}

@Module
interface IntDeferredInstantiateWithOthers : DeferredInstantiateWithOthers<String, Int, Double> {

}

@Module
interface IntDeferredInstantiate : DeferredInstantiate<Int> {

}

// This tests that the correct type can be resolved for the sequence generic, the 'test' is essentially successful compilation
// TODO -- unit testing of KSClassDeclaration.resolveTypeNameOfAncestorGenericParameter
@RunWith(Parameterized::class)
class TestGenericElementIdentification(
    private val creator: (Collection<Int>) -> OrderedSet<Int>
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun creators(): Collection<Array<Any?>> =
            listOf(
                arrayOf({ m: Collection<Int> -> mk_OrderedSet(*m.toTypedArray()) }),
                arrayOf({ m: Collection<Int> -> mk_IntOrderedSet(*m.toIntArray()) }),
                arrayOf({ m: Collection<Int> -> mk_SomeType(*m.toIntArray()) }),
                arrayOf({ m: Collection<Int> -> mk_DeferredInstantiate(*m.toTypedArray()) }),
                arrayOf({ m: Collection<Int> -> mk_IntDeferredInstantiate(*m.toIntArray()) }),
                arrayOf({ m: Collection<Int> -> mk_DeferredInstantiateWithOthers<Set<Int>, Int, String>(*m.toTypedArray()) }),
                arrayOf({ m: Collection<Int> -> mk_IntDeferredInstantiateWithOthers(*m.toIntArray()) }),
            )
    }

    private fun create(vararg m: Int) = creator.invoke(m.toList())

    @Test
    fun creation() {
        val orderedSet = create(1, 2, 3)
        assertEquals(1, orderedSet.first())
        assertEquals(3, orderedSet.last())
    }

}