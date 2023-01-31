# Conway's Game of Life

## Rectangular World

Black means dead. White means alive. Click on square cells to change their status. When cursor lands on a cell, its coordinate appears at upper right corner.

Most upper left cell is world origin.

Display is adaptive to screen size. No display if screen too small.

## Interface Manual

### A

Number of cells alive.

### G

Generation. Modifying world properties will reset this to 0.

### Thresh

A living cell survives if the number of its living neighbors is between the first two numbers, otherwise it dies.

A dead cell becomes alive (reproduction) if the number of its living neighbors is between the last two numbers, otherwise it stays death.

Clearly these four numbers have values 0-8. Click SET to apply changes.

### RGN O, RGN Size, ALL

Select a region from a coordinate and a size into the bottom right direction. Clicking ALL selects the entire world. Crop applies on selected region.

### UR Shift, Shift

Shift world some distance into the upper right direction in torus sense. Click Shift to apply.

### Delay

Delay between each generation's advance in auto play.

### Rand

Randomizes world.

### Clear

All cell dies.

### ADV

Advance into next generation.

### Torus

Connect upper and bottom world edges, left and right world edges.

### Mirror

Mirror world about center vertical line.

### 90°⭮

World clockwise rotation.