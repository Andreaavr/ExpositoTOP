# ADR 0002 — Leer la ventana temporal de los dos últimos campos

**Estado:** aceptada

## Contexto

Cada línea de nodo tiene el formato `i x y d S f a lista O C`, donde `lista` contiene `a`
valores. `TOPTWReader` leía `O` y `C` de las posiciones fijas 8 y 9 (7 y 8 para el depósito),
lo que solo es correcto si `a = 1`. En las 29 instancias de Solomon `a = 1`, pero en las 10
instancias `pr*` `a` toma los valores 1, 2 y 4. Por ejemplo, en `pr01`:

```text
25 -54.755 14.368 4 14 1 4 1 2 4 8 360 505
```

se leía la ventana `[2, 4]` en lugar de `[360, 505]`, lo que hacía inalcanzables muchos POI.

## Decisión

Tomar `O` y `C` de los **dos últimos** campos de la línea, válido para cualquier `a`, y
convertir los errores de formato en `IOException` con un mensaje claro.

## Consecuencias

- Los resultados de las instancias `pr*` cambian (y mejoran: en `pr01`, con semilla 42, la
  mejor puntuación pasa de 302 a 560). Los de Solomon no cambian.
- Un test de regresión (`readsTimeWindowFromLastFieldsWithVariableLengthList`) protege el
  comportamiento.
