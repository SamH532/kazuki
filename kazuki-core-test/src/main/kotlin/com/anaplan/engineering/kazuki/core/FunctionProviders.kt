package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.A_Module.set
import com.anaplan.engineering.kazuki.core.C_Module.set
import com.anaplan.engineering.kazuki.core.E_Module.as_E
import com.anaplan.engineering.kazuki.core.E_Module.is_E
import com.anaplan.engineering.kazuki.core.E_Module.set


@Module
interface A {
    val a: Int

    @FunctionProvider(AFunctions::class)
    val functions: AFunctions
}

open class AFunctions(val a: A) {

    open val increment = function(
        command = { a.set(a = a.a + 1) },
        post = { result -> result.a - 1 == a.a }
    )

}

@Module
interface B : A {

    @Invariant
    fun lessThan5() = a < 5
}

@Module
interface C : A {
    val c: Int

    @FunctionProvider(CFunctions::class)
    override val functions: CFunctions
}

open class CFunctions(val c: C) : AFunctions(c) {

    open val decrement = function(
        command = { c.set(c = c.c - 1) },
        post = { result -> result.a == c.a && result.c + 1 == c.c }
    )

}

@Module
interface D : C {
    val d: Int

    @Invariant
    fun lessThan5() = a < 5 && c < 5 && d < 5

    @Invariant
    fun greaterThanEqOne() = a >= 1 && c >= 1 && d >= 1
}

@Module
interface E : D {
    val e: Int

    @FunctionProvider(EFunctions::class)
    override val functions: EFunctions
}

open class EFunctions(val e: E) : CFunctions(e) {

    override val decrement = function<C>(
        command = { e.set(c = e.c - 1, e = e.e - 1) },
        post = { result ->
            is_E(result)
                    && as_E(result).a == e.a
                    && as_E(result).c + 1 == e.c
                    && as_E(result).d == e.d
                    && as_E(result).e + 1 == e.e
        }
    )

}


// TODO -- generic pattern