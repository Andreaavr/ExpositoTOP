# ADR 0003 — Generador aleatorio único e inyectable

**Estado:** aceptada

## Contexto

`aleatorySelectionRCL` creaba un `new Random()` en cada llamada (regla `java:S2119`). Además
de ser ineficiente, hacía imposible reproducir una ejecución y, por tanto, escribir tests de
regresión que comprobaran que una refactorización no cambia el algoritmo.

## Decisión

`TOPTWGRASP` recibe un `Random` en el constructor y lo reutiliza. El constructor de un
argumento mantiene la compatibilidad usando `new Random()`. `MainTOPTW` acepta `--seed`.

## Consecuencias

- Ejecuciones reproducibles, útiles para depurar, para comparar parámetros y para los tests.
- Con la misma semilla, el código refactorizado produce exactamente las mismas soluciones que
  el original, lo que se ha usado como prueba de equivalencia.
- El uso de `Random` sigue siendo un *Security Hotspot* (`java:S2245`) que se marca como
  seguro: no se usa con fines criptográficos.
