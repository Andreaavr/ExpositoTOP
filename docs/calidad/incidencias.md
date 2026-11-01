# Incidencias detectadas y soluciones

Inventario de incidencias del **código original** según las reglas del perfil *Sonar way*
para Java, agrupadas por tipo, y la solución aplicada en cada caso. Cada regla se identifica
con su clave (`java:Sxxxx`) para localizarla en SonarQube (*Rules*). Las líneas se refieren a
los ficheros originales.

!!! note "Cómo se ha elaborado"
    Análisis con SonarQube Community Build (servidor 13.7) mediante `mvn verify sonar:sonar`,
    completado con los informes de SpotBugs, PMD y Checkstyle que genera `mvn verify` y con
    la revisión del código. Las líneas se refieren a los ficheros originales.

## Fiabilidad (*bugs*)

| # | Regla | Dónde | Problema | Solución |
|---|---|---|---|---|
| B1 | `java:S2259` Null pointers should not be dereferenced | `ExpositoUtilities.printFile` (l. 159-160) | Si `new FileReader` falla, `reader` es `null` y el `finally` lanza `NullPointerException`, ocultando la excepción original | `try-with-resources`; el método propaga `IOException` |
| B2 | `java:S2095` Resources should be closed | `ExpositoUtilities.writeTextToFile` (l. 198-201) | Si `write` lanza una excepción, el `BufferedWriter` no se cierra | `try-with-resources` |
| B3 | `java:S2119` Random objects should be reused | `TOPTWGRASP.aleatorySelectionRCL` (l. 71) | Se crea un `Random` en cada llamada (miles por iteración): ineficiente y de peor calidad estadística | Un único `Random` como campo, inyectable por constructor ([ADR 0003](../adr/0003-aleatoriedad-inyectable.md)) |
| B4 | `java:S2272` Iterator.next() should throw NoSuchElementException | `PowerSet.next` | Al agotarse sigue devolviendo conjuntos, incumpliendo el contrato de `Iterator` | Lanza `NoSuchElementException` si `!hasNext()` |
| B4b | `java:S4348` iterator() should not return this | `PowerSet.iterator` | La clase era su propio iterador: solo podía recorrerse una vez | `PowerSet` es solo `Iterable` y cada `iterator()` crea un iterador interno nuevo |
| B5 | `java:S1201` / `java:S1206` equals/hashCode | `TOPTWSolution.equals(TOPTWSolution)` (l. 56) | Sobrecarga en lugar de sobrescribir `equals(Object)`: las colecciones la ignoran; además no hay `hashCode` | `equals(Object)` y `hashCode()` coherentes basados en `predecessors` |
| B6 | `java:S2589` Gratuitous boolean expressions | `ExpositoUtilities.isAcyclic` (l. 350-351) | `acyclic` nunca cambia: condición siempre cierta | Bucle `for` simplificado |

### Defectos funcionales que SonarQube no detecta

El análisis estático no entiende el formato de los ficheros ni la semántica de la salida, pero
la revisión del código y los tests encontraron estos errores, más graves que los anteriores:

| # | Dónde | Problema | Solución |
|---|---|---|---|
| F1 | `TOPTWReader.readProblem` (l. 37-38) | La ventana temporal se leía de las **columnas fijas 8 y 9**, pero en el formato el campo previo es una lista de longitud variable. En las 10 instancias `pr*` (con listas de longitud 2 y 4) se tomaban valores de la lista como horario: por ejemplo, el POI 25 de `pr01` quedaba con ventana `[2, 4]` en lugar de `[360, 505]` | Se usan los **dos últimos** campos de la línea ([ADR 0002](../adr/0002-ventana-temporal-ultimos-campos.md)). Con semilla 42, la mejor puntuación en `pr01` pasa de **302** a **560** |
| F2 | `TOPTWReader` (l. 43-45, 50-52) | Ante un error de lectura escribía el error y llamaba a `System.exit(0)`, es decir, terminaba con **código de éxito** | Lanza `IOException`; `MainTOPTW` registra el error y termina con código 1 |
| F3 | `TOPTWSolution.getInfoSolution` (l. 185) | La columna `SERVICE TIME` mostraba la duración del nodo **anterior** | Muestra la del nodo de la fila |
| F4 | `TOPTWSolution.getInfoSolution` (l. 178-188) | En nodos que incumplen su ventana, las columnas de llegada y salida conservaban los valores de la fila anterior | Se muestran como `-` |

## Seguridad

| # | Regla | Dónde | Valoración |
|---|---|---|---|
| H1 | `java:S2245` (*Security Hotspot*) Pseudorandom number generators | `TOPTWGRASP` (l. 71) | `java.util.Random` no es criptográficamente seguro, pero aquí solo guía una heurística de optimización: se revisa y se marca como **Safe** en SonarQube. No hay vulnerabilidades. |

## Mantenibilidad (*code smells*)

### Diseño y complejidad

| # | Regla | Dónde | Solución |
|---|---|---|---|
| M1 | `java:S3776` Cognitive Complexity > 15 | `TOPTWGRASP.comprehensiveEvaluation` (l. 228-311, ~6 niveles de anidamiento), `computeGreedySolution`, `TOPTWSolution.getInfoSolution` | Extracción de `departureTime()`, `insertionCost()`, `sortedCandidates()`, `newDepartureTimes()`, `select()`; uso de `Math.max`. Comportamiento verificado con test de regresión |
| M1b | `java:S6206` Records should be used | `Pair` | `public record Pair<F, S>(F first, S second)` (Java 16) |
| M2 | `java:S1871` Duplicated branches | `switch` de `computeGreedySolution` (l. 155 y 161) | `case 1` y `default` eran iguales; la estrategia pasa a ser un `enum` ([ADR 0005](../adr/0005-estrategia-seleccion-enum.md)) |
| M3 | Selección fijada en el código (`int selection = 3`) | `computeGreedySolution` (l. 152-153) | `SelectionStrategy` y `alpha` configurables con *setters* |
| M4 | `java:S4144` Methods should not have identical implementations | `TOPTW.getDistance(int,int)` y `getTime` (l. 112-122) | `getTime` delega en `getDistance` |
| M5 | Duplicación de bloques | Comparador de candidatos (l. 133 y 188), inicialización de tiempos (l. 123 y 178) | Métodos `sortedCandidates()` y `newDepartureTimes()` |
| M6 | `java:S1186` / `java:S125` Empty method / commented-out code | `TOPTWEvaluator.evaluate` (cuerpo entero comentado, l. 7-32) | Implementado: calcula y guarda la función objetivo |
| M7 | `java:S1118` Utility classes should not have public constructors | `ExpositoUtilities`, `TOPTWReader` | Clases `final` con constructor privado |

### Robustez y buenas prácticas

| # | Regla | Dónde | Solución |
|---|---|---|---|
| M8 | `java:S1147` Exit methods should not be called | `TOPTWReader` (l. 45, 52) | Excepciones (ver F2) |
| M9 | `java:S106` Standard outputs should not be used directly | `TOPTWGRASP` (l. 50, 66, 67), `TOPTWReader` (l. 44, 51), `TOPTWSolution.printSolution`, `ExpositoUtilities.printFile` | Se escribe en un `PrintStream` inyectado; solo `main` usa `System.out` ([ADR 0004](../adr/0004-salida-desacoplada.md)) |
| M10 | `java:S108` / `java:S2221` Empty catch / catching `Exception` | `isInteger`, `isDouble` (l. 333, 342) | Se captura `NumberFormatException` y se devuelve `false` explícitamente; comprobación de `null` |
| M11 | `java:S1444` `public static` fields should be `final` | `TOPTWEvaluator.NO_EVALUATED`, `TOPTWGRASP.NO_EVALUATED` | `public static final` en el evaluador; el de GRASP no se usaba y se elimina |
| M12 | `java:S1168` Return empty array instead of null | `multiplyMatrices` (l. 181) | `IllegalArgumentException` con mensaje si las dimensiones no son compatibles |
| M13 | `java:S2384` Mutable members returned directly | `TOPTW.getScore()`, `TOPTWSolution.getPredecessors()/getSuccessors()`, `BellmanFord.getDistances()` | Se devuelven copias |
| M14 | `java:S1144` Unused private method | `ExpositoUtilities.getFirstAppearance` | Eliminado |
| M15 | `java:S1481` / `java:S1854` Unused variables / dead stores | `newDepot` (l. 176), `symbols` (l. 245), `line = null; parts = null` (l. 23, 40), inicializaciones `= -1` y `= 0` sobrescritas | Eliminados |
| M16 | `java:S1226` Parameters should not be reassigned | `TOPTW.getDistance(i, j)`, `getTime`, `getX`… (`i = 0`) | Método privado `dataIndex()` |
| M17 | `java:S1643` String concatenation in loops | `TOPTW.toString`, `getInfoSolution`, `ExpositoUtilities.getFormat` | `StringBuilder` |

### Convenciones y estilo

| # | Regla | Dónde | Solución |
|---|---|---|---|
| C1 | `java:S101` Class names | `mainTOPTW` | `MainTOPTW` |
| C2 | `java:S100` Method names | `TOPTWGRASP.GRASP` | `grasp` |
| C3 | `java:S117` / `java:S3008` Local / static field names | `COLUMN_WIDTH`, `INSTANCE` (locales), `NO_EVALUATED` (estático no final) | `columnWidth`, eliminado, `final` |
| C4 | `java:S1126` Return of boolean literal | `TOPTW.isDepot` (l. 42-45) | `return a > this.nodes;` |
| C5 | `java:S1125` Boolean literals | `validFinalInsertion==true` (l. 286) | Eliminado al reestructurar |
| C6 | `java:S1066` Mergeable if | `ExpositoUtilities.getFormat(String)` (l. 205-206) | Condición unida con `&&` |
| C7 | `java:S1155` Use isEmpty() | `rclAlphaCut.size() > 0` (l. 108) | `!rclPos.isEmpty()` |
| C8 | `java:S1488` Local variable returned immediately | `aleatorySelectionRCL` (l. 74-75) | `return random.nextInt(maxTRCL);` |
| C9 | `java:S1604` Anonymous class → lambda | Comparadores (l. 133, 188) | `Comparator.comparingDouble(a -> a[COST])` |
| C10 | `java:S2293` Diamond operator | `new ArrayList< double[] >()`, `new Pair<A, B>(…)` | `new ArrayList<>()` |
| C11 | `java:S1319` Use interfaces in declarations | Parámetros y variables `ArrayList<…>` | `List<…>` |
| C12 | `java:S1197` Array designators on the type | `double a[][]` (l. 176, 186) | `double[][] a` |
| C13 | `java:S1659` One declaration per line | `int pre=-1, suc=-1;` y similares | Una declaración por línea |
| C14 | `java:S125` Commented-out code | `TOPTWGRASP` (l. 51-52, 57), `TOPTW` (l. 205) | Eliminado |
| C15 | Javadoc vacío | `BellmanFord` (comentarios `/** */` sin texto) | Documentación Doxygen completa |

### Higiene del repositorio

| Problema | Solución |
|---|---|
| Binarios y salidas de compilación versionados (`build/`, `dist/*.jar`, `dist/javadoc/`) | Eliminados; `.gitignore` |
| Copia de seguridad del editor `ExpositoUtilities.java~` con un paquete antiguo (`exposito.utilities`) | Eliminada |
| Configuración privada de IDE (`nbproject/private`, `.settings`) | Eliminada |
| Sin tests: cobertura 0 % (la *Quality Gate* exige ≥ 80 % en código nuevo) | 43 tests JUnit ([Pruebas](pruebas.md)) |

## Resumen antes / después (resultados reales)

| Métrica | Original | Final |
|---|---|---|
| **Quality Gate** | Passed *(no evaluaba nada: primer análisis, sin código nuevo)* | **Passed** *(evaluando el código nuevo)* |
| Security (SonarQube) | 0 · A | 0 · A |
| **Reliability** (SonarQube) | **4 · D** | **0 · A** |
| **Maintainability** (SonarQube) | **73** · A | **0** · A |
| **Coverage** | **0,0 %** (793 líneas) | **97,6 %** (658 líneas) |
| Duplications | 0,0 % | 0,0 % |
| Tests JUnit | 0 | 43 |
| SpotBugs | 22 | 0 *(10 `EI_EXPOSE_REP` excluidos y justificados en `spotbugs-exclude.xml`)* |
| PMD | 44 | 0 |
| Checkstyle (Google, avisos) | 1625 | informativo: estilo de 4 espacios frente a 2 |
| Complejidad cognitiva máxima | 95 (`comprehensiveEvaluation`) | < 15 |

### Hallazgos durante el proceso

- **La Quality Gate aprueba sola el primer análisis**: solo evalúa el *código nuevo*, que
  no existe en la primera ejecución. Hay que mirar *Overall Code*.
- **Con menos de 20 líneas nuevas no se aplican las condiciones de cobertura ni duplicación**:
  tras corregir solo la fiabilidad (16 líneas) seguía en *Passed* con un 0 % de cobertura; al
  refactorizar el resto pasó a *Failed* hasta añadir los tests.
- **Falso positivo por idioma**: la regla de comentarios `TODO` detectó la palabra española
  «sobre *todo*».
- **SonarQube también analiza los tests**: marcó `assertFalse(a.equals(b))` (mejor
  `assertNotEquals`) y un `throws IOException` innecesario.
- **Perfil de calidad**: el servidor tenía por defecto el perfil *Sonar agentic AI*.
- **Las herramientas se solapan**: el `Random` creado en cada llamada lo detectaron SonarQube
  (`java:S2119`), SpotBugs (`DMI_RANDOM_USED_ONLY_ONCE`) y las inspecciones de IntelliJ.

!!! success "Comportamiento conservado"
    El test `refactoringPreservesOriginalResults` comprueba que, con la misma semilla, el GRASP
    refactorizado obtiene las mismas puntuaciones que el original. Además se compararon
    450 soluciones completas (rutas, puntuación y coste) de `c101`, `r101` y `rc101` con
    50 iteraciones por tamaño de RCL: todas son idénticas.
