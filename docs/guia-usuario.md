# Guía de usuario

## Requisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| JDK | 17 o superior | Compilar y ejecutar |
| Maven | 3.6.3 o superior | Construir el proyecto |
| Doxygen + Graphviz | 1.9+ / 2.40+ | *(Opcional)* referencia del código y grafos |
| Python + MkDocs | 3.9+ | *(Opcional)* portal de documentación |

Comprobación rápida:

```bash
java -version
mvn -v
```

## Construir

```bash
git clone https://github.com/Andreaavr/ExpositoTOP.git
cd ExpositoTOP
mvn package
```

Se generan en `target/`:

- `ExpositoTOP.jar`: aplicación ejecutable.
- `ExpositoTOP-sources.jar` y `ExpositoTOP-javadoc.jar`: código fuente y documentación.
- `surefire-reports/`: resultados de los tests.

## Ejecutar

=== "JAR"

    ```bash
    java -jar target/ExpositoTOP.jar [--dir DIR] [--iterations N] [--seed S] [instancia ...]
    ```

=== "Maven"

    ```bash
    mvn compile exec:java                                   # argumentos por defecto del pom
    mvn compile exec:java -Dexec.args="--iterations 500 r101.txt"
    ```

| Opción | Por defecto | Descripción |
|---|---|---|
| `--dir DIR` | `Instances/TOPTW` | Directorio donde están las instancias |
| `--iterations N` | `10000` | Construcciones GRASP por cada tamaño de RCL (3, 5 y 7) |
| `--seed S` | aleatoria | Semilla para obtener resultados reproducibles |
| `instancia ...` | las 29 de Solomon (`c1*`, `r1*`, `rc1*`) | Ficheros a resolver |

!!! warning "Duración del experimento completo"
    Sin argumentos se reproduce el experimento original: 29 instancias × 3 tamaños de RCL ×
    10 000 iteraciones. Escribe el informe de cada solución, por lo que tarda bastante y
    genera mucha salida. Para pruebas, use `--iterations 100` y redirija la salida a un
    fichero (`> resultados.txt`).

## Interpretar la salida

Por cada solución construida se imprime un bloque como este:

```text
SOLUTION:
0 - 5 - 3 - 7 - 83 - 82 - 61 - 26 - 51 - 21 - 49 - 47 - 0
101 - 43 - 42 - 41 - 27 - 46 - 45 - 48 - 23 - 66 - 75 - 101
102 - 67 - 87 - 86 - 94 - 92 - 85 - 6 - 89 - 91 - 102
...
109 - 81 - 54 - 53 - 56 - 72 - 77 - 79 - 80 - 109
FEASIBLE SOLUTION: true
SCORE: 1470.0
TIME COST: 10043.516672012478
```

*(Primera solución de `c101.txt` con `--seed 42`.)*

- Cada línea de `SOLUTION` es una ruta (un día). Empieza y termina en su depósito: `0` para la
  primera ruta y `101`, `102`, … (número de POI + 1, + 2, …) para las siguientes. Todos los
  depósitos representan el mismo lugar físico (el hotel).
- `FEASIBLE SOLUTION` indica si se respetan todas las ventanas temporales y la duración máxima.
- `SCORE` es la suma del interés de los POI visitados (valor a maximizar).
- `TIME COST` es la suma de las horas de regreso al depósito de todas las rutas.

A continuación aparece una tabla por ruta con las coordenadas, la ventana `[READY TIME, DUE DATE]`,
la hora de llegada y salida y la duración de la visita de cada nodo.

Tras las iteraciones de cada tamaño de RCL:

```text
 --> MEDIA: 1448.6
 --> MEJOR SOLUCION: 1550.0
```

## Resultados reproducibles

Con `--seed`, dos ejecuciones con los mismos argumentos producen exactamente la misma salida.
Es lo que usan los tests de regresión para comprobar que las refactorizaciones no cambian el
comportamiento del algoritmo.

## Usar la biblioteca desde código

```java
TOPTW problem = TOPTWReader.readProblem("Instances/TOPTW/c101.txt");
TOPTWGRASP grasp = new TOPTWGRASP(new TOPTWSolution(problem), new Random(42), null);
grasp.setStrategy(TOPTWGRASP.SelectionStrategy.FUZZY_ALPHA_CUT);
grasp.setAlpha(0.8);
double best = grasp.grasp(1000, 5);          // mejor puntuación
double mean = grasp.getLastAverageFitness(); // puntuación media
```

## Generar la documentación

```bash
pip install -r requirements-docs.txt
doxygen Doxyfile                     # target/doxygen/html, latex y xml
make -C target/doxygen/latex         # target/doxygen/latex/refman.pdf
mkdocs serve                         # portal en http://127.0.0.1:8000
mkdocs build                         # portal estático en site/
python scripts/build_pdf.py          # docs/pdf/ExpositoTOP-documentacion.pdf
```

O todo a la vez con Maven: `mvn -Pdocs package`.
