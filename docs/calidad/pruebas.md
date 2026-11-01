# Pruebas

El proyecto original no tenía tests. Se han añadido **43 tests JUnit 4** en `src/test/java`,
que Maven ejecuta en la fase `test` y JaCoCo mide para la cobertura.

| Clase de test | Qué verifica |
|---|---|
| `TOPTWReaderTest` | Lectura de `c101` y de una instancia mínima; **regresión de la ventana temporal en `pr01`**; errores con fichero inexistente, truncado o mal formado |
| `TOPTWTest` | Matriz de distancias euclídea y simétrica; traducción de depósitos virtuales; longitudes de rutas; copia defensiva; `toString` |
| `TOPTWSolutionTest` | Solución inicial; `addRoute`; fitness; informe (factible e infactible); `equals`/`hashCode`; copias |
| `TOPTWGRASPTest` | Factibilidad de las soluciones con las tres estrategias; reproducibilidad con semilla; **resultados idénticos al código original**; mejora en `pr01`; evaluación de inserciones; estrategias de selección |
| `MainTOPTWTest` | Interpretación de argumentos y ejecución de extremo a extremo |
| `TOPTWRouteTest`, `PairTest` | Accesores, igualdad, `hashCode` |
| `ExpositoUtilitiesTest` | Normalización de cadenas, formato, detección de números, producto de matrices, ciclos, ficheros |
| `PowerSetTest`, `BellmanFordTest` | 2ⁿ subconjuntos y contrato de `Iterator`; caminos mínimos con pesos negativos y camino máximo |

## Oráculo de factibilidad independiente

`TestSupport.assertFeasible` recorre cada ruta y comprueba, **sin usar el código del GRASP**,
que:

- los enlaces `predecessors`/`successors` son coherentes,
- cada POI se visita como mucho una vez,
- se llega a cada nodo antes del cierre de su ventana,
- ninguna ruta supera la duración máxima.

Así los tests no se limitan a repetir la lógica que verifican.

## Instancia mínima

`src/test/resources/mini.txt` tiene 4 POI y 2 rutas, con valores elegidos para poder calcular
a mano los resultados (por ejemplo, insertar el POI 1 cuesta 10 + 5 + 10 = 25) e incluye un POI
inalcanzable dentro de su ventana y una línea con lista de longitud 2.

## Ejecución

```bash
mvn test                                   # todos los tests
mvn test -Dtest=TOPTWGRASPTest             # una clase
mvn verify                                 # + informe de cobertura en target/site/jacoco/index.html
```

Resultado esperado: `Tests run: 43, Failures: 0, Errors: 0, Skipped: 0`.
