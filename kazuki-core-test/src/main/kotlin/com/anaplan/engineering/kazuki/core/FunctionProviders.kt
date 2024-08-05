package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.A_Module.set
import com.anaplan.engineering.kazuki.core.C_Module.set
import com.anaplan.engineering.kazuki.core.E_Module.as_E
import com.anaplan.engineering.kazuki.core.E_Module.is_E
import com.anaplan.engineering.kazuki.core.E_Module.set
import com.anaplan.engineering.kazuki.core.GA_Module.set
import com.anaplan.engineering.kazuki.core.GC1_Module.set
import com.anaplan.engineering.kazuki.core.GC2_Module.set
import com.anaplan.engineering.kazuki.core.GE1_Module.as_GE1
import com.anaplan.engineering.kazuki.core.GE1_Module.is_GE1
import com.anaplan.engineering.kazuki.core.GE1_Module.set
import com.anaplan.engineering.kazuki.core.GE2_Module.as_GE2
import com.anaplan.engineering.kazuki.core.GE2_Module.is_GE2
import com.anaplan.engineering.kazuki.core.GE2_Module.set
import com.anaplan.engineering.kazuki.core.GE3_Module.as_GE3
import com.anaplan.engineering.kazuki.core.GE3_Module.is_GE3
import com.anaplan.engineering.kazuki.core.GE3_Module.set
import com.anaplan.engineering.kazuki.core.GE4_Module.as_GE4
import com.anaplan.engineering.kazuki.core.GE4_Module.is_GE4
import com.anaplan.engineering.kazuki.core.GE4_Module.set


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


@Module
interface GA<P> {
    val map: Mapping<P, Int>

    @FunctionProvider(GAFunctions::class)
    val functions: GAFunctions<P>
}

open class GAFunctions<P>(val ga: GA<P>) {
    open val increment = function(
        command = { p: P -> ga.set(map = ga.map * mk_(p, ga.map[p] + 1)) },
        pre = { p -> p in ga.map.dom },
        post = { p, result -> result.map[p] == ga.map[p] + 1 }
    )
}

@Module
interface GB1<P> : GA<P> {

    @Invariant
    fun lessThan5() = forall(map.rng) { it < 5 }

}

@Module
interface GB2 : GA<String> {

    @Invariant
    fun lessThan5() = forall(map.rng) { it < 5 }

}

@Module
interface GC1<P> : GA<P> {
    val c: Int

    @FunctionProvider(GC1Functions::class)
    override val functions: GC1Functions<P>
}

open class GC1Functions<P>(val c: GC1<P>) : GAFunctions<P>(c) {

    open val decrement = function(
        command = { c.set(c = c.c - 1) },
        post = { result -> result.map == c.map && result.c + 1 == c.c }
    )

}

@Module
interface GC2 : GA<String> {
    val c: Int

    @FunctionProvider(GC2Functions::class)
    override val functions: GC2Functions
}

open class GC2Functions(val c: GC2) : GAFunctions<String>(c) {

    open val decrement = function(
        command = { c.set(c = c.c - 1) },
        post = { result -> result.map == c.map && result.c + 1 == c.c }
    )

}

@Module
interface GD1<P> : GC1<P> {
    val d: Int

    @Invariant
    fun lessThan5() = forall(map.rng) { it < 5 } && c < 5 && d < 5

    @Invariant
    fun greaterThanEqOne() = forall(map.rng) { it < 5 } && c >= 1 && d >= 1
}

@Module
interface GD2 : GC1<String> {
    val d: Int

    @Invariant
    fun lessThan5() = forall(map.rng) { it < 5 } && c < 5 && d < 5

    @Invariant
    fun greaterThanEqOne() = forall(map.rng) { it < 5 } && c >= 1 && d >= 1
}

@Module
interface GD3 : GC2 {
    val d: Int

    @Invariant
    fun lessThan5() = forall(map.rng) { it < 5 } && c < 5 && d < 5

    @Invariant
    fun greaterThanEqOne() = forall(map.rng) { it < 5 } && c >= 1 && d >= 1
}

@Module
interface GE1<P> : GD1<P> {
    val e: Int

    @FunctionProvider(GE1Functions::class)
    override val functions: GE1Functions<P>
}

open class GE1Functions<P>(val e: GE1<P>) : GC1Functions<P>(e) {

    override val decrement = function<GC1<P>>(
        command = { e.set(c = e.c - 1, e = e.e - 1) },
        post = { result ->
            is_GE1<P>(result)
                    && as_GE1<P>(result).map == e.map
                    && as_GE1<P>(result).c + 1 == e.c
                    && as_GE1<P>(result).d == e.d
                    && as_GE1<P>(result).e + 1 == e.e
        }
    )

}

@Module
interface GE2 : GD1<String> {
    val e: Int

    @FunctionProvider(GE2Functions::class)
    override val functions: GE2Functions
}

open class GE2Functions(val e: GE2) : GC1Functions<String>(e) {

    override val decrement = function<GC1<String>>(
        command = { e.set(c = e.c - 1, e = e.e - 1) },
        post = { result ->
            is_GE2(result)
                    && as_GE2(result).map == e.map
                    && as_GE2(result).c + 1 == e.c
                    && as_GE2(result).d == e.d
                    && as_GE2(result).e + 1 == e.e
        }
    )

}

@Module
interface GE3 : GD2 {
    val e: Int

    @FunctionProvider(GE3Functions::class)
    override val functions: GE3Functions
}

open class GE3Functions(val e: GE3) : GC1Functions<String>(e) {

    override val decrement = function<GC1<String>>(
        command = { e.set(c = e.c - 1, e = e.e - 1) },
        post = { result ->
            is_GE3(result)
                    && as_GE3(result).map == e.map
                    && as_GE3(result).c + 1 == e.c
                    && as_GE3(result).d == e.d
                    && as_GE3(result).e + 1 == e.e
        }
    )

}

@Module
interface GE4 : GD3 {
    val e: Int

    @FunctionProvider(GE4Functions::class)
    override val functions: GE4Functions
}

open class GE4Functions(val e: GE4) : GC2Functions(e) {

    override val decrement = function<GC2>(
        command = { e.set(c = e.c - 1, e = e.e - 1) },
        post = { result ->
            is_GE4(result)
                    && as_GE4(result).map == e.map
                    && as_GE4(result).c + 1 == e.c
                    && as_GE4(result).d == e.d
                    && as_GE4(result).e + 1 == e.e
        }
    )

}