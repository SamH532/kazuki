# Game of Life

An example specification of Conway's Game of Life in Kazuki, based on
a [VDM version](https://github.com/overturetool/overturetool.github.io/blob/master/download/examples/VDMSL/ConwayGameLifeSL/index.md)
by Nick Battle, Peter Gorm Larsen and Claus Ballegaard Nielsen.

Conway's Game of Life is a theoretical system embedded into a two-dimensional square grid.
Each square is either alive or dead (sometimes called "empty").
Each new generation is determined based only on the current generation's state.
Each square on the grid interacts with its eight neighbours (horizontal, vertical and diagonal).
The rules for this are as follows:

- Any live square with less than two live neighbours dies
- Any live square with more than three live neighbours dies
- Any live square with two or three live neighbours survives into the next generation
- Any empty square with three live neighbours will become a live square

The system is given an initial state which it uses to calculate the subsequent generations.
This calculation is repeated, with some systems eventually forming cycles and others eventually dying off.