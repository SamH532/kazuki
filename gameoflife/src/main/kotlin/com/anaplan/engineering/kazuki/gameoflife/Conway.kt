package com.anaplan.engineering.kazuki.gameoflife

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.as_Population
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Point

@Module
object Conway {

    private const val Generate = 3 // Number of neighbours to cause generation
    private val Survive = mk_Set(2, 3) // Numbers of neighbours to ensure survival, else death
    private const val maxNeighbours = 8

    interface Point {
        val x: int
        val y: int
    }

    interface Population : Set<Point>

    val around: (Point) -> Population = function(
        command = { p: Point ->
            val A: Set<int> = mk_Set(-1, 0, 1) // Adjacencies
            as_Population(
                dunion(
                set(A) { x: int -> set(A) { y: int -> mk_Point(p.x + x, p.y + y) } }
            ).minus(p))
        },
        post = { _, result -> result.card <= maxNeighbours }
    )

    val neighbourCount: (Population, Point) -> int = function(
        command = { pop: Population, p: Point ->
            (around(p) inter pop).card
        },
        post = { _, _, result -> result <= maxNeighbours },
    )

    val newCells: (Population) -> Population = function(
        command = { pop: Population ->
            as_Population(
                dunion(
                set(pop) { p: Point ->
                    set(around(p) minus pop, filter = {
                        neighbourCount(pop, it) == Generate
                    }) { q -> q }
                }
            ))
        },
        post = { pop, result -> (result inter pop).isEmpty() }
    )

    val deadCells: (Population) -> Population = function(
        command = { pop: Population ->
            as_Population(
                set(pop, filter = { neighbourCount(pop, it) !in Survive }) { it }
            )
        },
        post = { pop, result -> (result inter pop) == result }
    )

    internal val generation: (Population) -> Population = function(
        command = { pop: Population ->
            (pop - deadCells(pop)) + newCells(pop)
        }
    )

    val generations: (nat1, Population) -> Population by lazy {
        function(
            command = { n: nat1, pop: Population ->
                val newP = generation(pop)
                if (n == 1) {
                    newP
                } else {
                    generations(n - 1, newP)
                }
            },
            measure = { n, _ -> n }
        )
    }
}
