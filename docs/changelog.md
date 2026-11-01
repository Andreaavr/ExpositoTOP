# Registro de cambios

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).

## [1.0.0] — Práctica 3

### Añadido
- Proyecto Maven (`pom.xml`) con enforcer, compiler, surefire, JaCoCo, jar, source, javadoc,
  Checkstyle, PMD, SpotBugs, exec y SonarQube; perfil `docs`.
- 43 tests JUnit y una instancia de prueba mínima.
- Opciones de línea de órdenes `--dir`, `--iterations` y `--seed`.
- `TOPTWGRASP.SelectionStrategy` y *setters* de estrategia, `alpha` y salida.
- `TOPTWEvaluator.evaluate` implementado.
- Documentación Doxygen en todas las clases, `Doxyfile` con Graphviz, portal MkDocs Material
  con MkDoxy, ADR y PDF.
- GitHub Actions: construcción, tests, análisis SonarQube y publicación en GitHub Pages.

### Corregido
- Lectura de la ventana temporal en instancias con listas de longitud variable (`pr*`).
- `TOPTWReader` terminaba el proceso con código 0 ante errores.
- `NullPointerException` en `ExpositoUtilities.printFile` si el fichero no existe.
- Recurso sin cerrar en `ExpositoUtilities.writeTextToFile`.
- `PowerSet.next()` no lanzaba `NoSuchElementException`.
- `TOPTWSolution.equals` no sobrescribía `Object.equals`; faltaba `hashCode`.
- Columna `SERVICE TIME` y celdas obsoletas en el informe de solución.

### Cambiado
- `mainTOPTW` → `MainTOPTW`; `GRASP()` → `grasp()` (devuelve la mejor puntuación).
- `TOPTWRoute.getPredeccesor/getSuccesor` → `getPredecessor/getSuccessor`.
- `ExpositoUtilities.getFormat(ArrayList<Integer>)` → `getFormatIntegers(List<Integer>)`.
- `TOPTW.getDistance(ArrayList<Integer>[])` → `getTotalDistance(List<List<Integer>>)`.
- Reducción de la complejidad cognitiva de la construcción voraz sin cambiar los resultados.

### Eliminado
- `build/`, `dist/`, `nbproject/`, `build.xml`, ficheros de IDE y `ExpositoUtilities.java~`.
- Código comentado y miembros sin uso.
