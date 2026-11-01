# ExpositoTOP — RTVRPTW

**Recomendador Turístico con Ventanas de Tiempo.** Dado un conjunto de puntos de interés (POI)
con su interés turístico, su horario y la duración de la visita, el sistema propone un
itinerario de varios días que **maximiza el interés total** sin salirse de los horarios ni
de la duración máxima de cada jornada.

Formalmente es el *Team Orienteering Problem with Time Windows* (**TOPTW**), un problema
NP-difícil que el proyecto resuelve con la metaheurística **GRASP** (*Greedy Randomized
Adaptive Search Procedure*).

<div class="grid cards" markdown>

-   :material-play-circle: **Empezar**

    Compilar, ejecutar e interpretar los resultados.

    [Guía de usuario](guia-usuario.md)

-   :material-sitemap: **Diseño**

    Clases, estructuras de datos y funcionamiento del GRASP.

    [Arquitectura](arquitectura.md) · [Algoritmo](algoritmo.md)

-   :material-language-java: **Construcción con Maven**

    Ciclo de vida, órdenes y plugins configurados en el `pom.xml`.

    [Maven](maven/index.md)

-   :material-shield-check: **Calidad**

    Análisis con SonarQube, incidencias detectadas y correcciones aplicadas.

    [SonarQube](calidad/sonarqube.md) · [Incidencias](calidad/incidencias.md)

-   :material-book-open-variant: **Referencia de la API**

    Generada por Doxygen a partir de los comentarios del código.

    [Referencia API](api-referencia.md)

-   :material-scale-balance: **Decisiones**

    Registro de decisiones de arquitectura (ADR).

    [ADR](adr/index.md)

</div>

## Resumen rápido

```bash
mvn package                      # compila, ejecuta los tests y genera target/ExpositoTOP.jar
java -jar target/ExpositoTOP.jar --iterations 100 --seed 42 c101.txt
```

## Estado del proyecto

| Aspecto | Antes | Después |
|---|---|---|
| Construcción | NetBeans / Ant (`build.xml`) | Maven (`pom.xml`) + GitHub Actions |
| Tests automáticos | Ninguno | 43 tests JUnit (lectura, modelo, GRASP, utilidades, CLI) |
| Documentación | Javadoc vacío en parte de las clases | Comentarios Doxygen (`\brief` + detalle) en todas las clases y métodos; portal MkDocs |
| Fallos funcionales conocidos | Lectura incorrecta de ventanas temporales en instancias `pr*` | Corregido (ver [ADR 0002](adr/0002-ventana-temporal-ultimos-campos.md)) |

!!! info "Sobre esta documentación"
    Las guías, la arquitectura y los ADR de este portal se redactaron con ayuda de un modelo
    de lenguaje (LLM) a partir del código fuente, y después se revisaron contra el propio código
    y los tests (actividad 9.3 de la práctica). La referencia de la API se genera
    automáticamente con Doxygen y MkDoxy en cada construcción.
