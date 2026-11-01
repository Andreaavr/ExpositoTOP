# ADR 0004 — Salida desacoplada de la consola

**Estado:** aceptada

## Contexto

El algoritmo y el lector escribían directamente en `System.out`/`System.err` y el lector
terminaba el proceso con `System.exit(0)` ante cualquier error (`java:S106`, `java:S1147`).
Esto impedía usar las clases como biblioteca, probarlas sin capturar la consola y distinguir
una ejecución correcta de una fallida (código de salida 0 en ambos casos).

## Decisión

- `TOPTWGRASP` escribe en un `PrintStream` inyectado (o en ninguno, si es `null`) y expone
  `getLastAverageFitness()` y `getLastBestFitness()`.
- `TOPTWSolution.printSolution` y `ExpositoUtilities.printFile` reciben el `PrintStream`.
- `TOPTWReader` lanza `IOException`.
- Solo `MainTOPTW.main` usa `System.out` y `System.exit(1)`, marcados como excepción
  justificada (`NOSONAR`).

## Consecuencias

- Las clases se prueban capturando la salida en memoria.
- La aplicación de consola mantiene exactamente el mismo formato de salida.
