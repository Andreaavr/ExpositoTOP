# ADR 0005 — Estrategia de selección de la RCL como `enum`

**Estado:** aceptada

## Contexto

La estrategia se elegía con `int selection = 3` dentro de `computeGreedySolution`, junto a
`double alpha = 0.8`. Cambiarla requería editar el código, y el `switch` tenía dos ramas
idénticas (`java:S1871`).

## Decisión

Definir `TOPTWGRASP.SelectionStrategy { RANDOM, FUZZY_BEST, FUZZY_ALPHA_CUT }` con *setters*
para la estrategia y para `alpha`. Los valores por defecto (`FUZZY_ALPHA_CUT`, 0,8) son los del
original.

## Consecuencias

- Las estrategias se pueden comparar sin recompilar y se prueban las tres.
- El comportamiento por defecto no cambia.
