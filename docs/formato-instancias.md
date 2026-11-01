# Formato de las instancias

Las instancias están en `Instances/TOPTW/` y siguen el formato de Cordeau (instancias `pr*`)
adaptado por Righini y Salani al TOPTW, también usado para las de Solomon (`c*`, `r*`, `rc*`).

```text
4 10 100 1                                   ← k v N t
0 200                                        ← D Q
0 40.00 50.00 0.00 0.00 0 0 0 1236           ← depósito (nodo 0)
1 45.00 68.00 90.00 10.00 1 1 1 912 967      ← POI 1
...
```

## Primera línea: `k v N t`

| Campo | Significado | Uso |
|---|---|---|
| `k` | — | No se usa |
| `v` | Número de rutas (vehículos / días) | `TOPTW.getVehicles()` |
| `N` | Número de POI | `TOPTW.getPOIs()` |
| `t` | — | No se usa |

## Segunda línea: `D Q`

No se usa.

## Líneas de nodo: `i x y d S f a lista O C`

| Campo | Significado | Uso |
|---|---|---|
| `i` | Número del nodo (0 = depósito) | — |
| `x`, `y` | Coordenadas | Matriz de distancias euclídeas |
| `d` | Duración de la visita | `getServiceTime(i)` |
| `S` | Puntuación (interés) | `getScore(i)` |
| `f`, `a` | — | `a` es la longitud de `lista` |
| `lista` | `a` valores | No se usa |
| `O` | Apertura de la ventana temporal | `getReadyTime(i)` |
| `C` | Cierre de la ventana temporal | `getDueTime(i)` |

!!! danger "La lista tiene longitud variable"
    Como `lista` ocupa `a` campos, `O` y `C` **no están en una columna fija**: son siempre
    los **dos últimos** campos de la línea. La versión original leía las columnas 8 y 9, lo
    que solo es correcto cuando `a = 1` (como en las instancias de Solomon). En las instancias
    `pr*`, donde `a` vale 1, 2 o 4, se leían como ventana temporal valores de la lista. Ver
    [ADR 0002](adr/0002-ventana-temporal-ultimos-campos.md).

## Observaciones

- El nodo 0 es a la vez el punto de salida y de llegada de todas las rutas.
- La duración máxima de cada ruta es el cierre de la ventana del depósito (`C` del nodo 0).
- El tiempo de viaje entre dos nodos es su distancia euclídea.
- El fichero `Instances/TOPTW/formatInstances.txt` contiene la descripción original del formato.
