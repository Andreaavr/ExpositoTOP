# ExpositoTOP — RTVRPTW

**Recomendador Turístico con Ventanas de Tiempo**: resolución del *Team Orienteering Problem
with Time Windows* (TOPTW) con la metaheurística GRASP.

[![CI](https://github.com/Andreaavr/ExpositoTOP/actions/workflows/ci.yml/badge.svg)](https://github.com/Andreaavr/ExpositoTOP/actions/workflows/ci.yml)
[![Documentación](https://github.com/Andreaavr/ExpositoTOP/actions/workflows/docs.yml/badge.svg)](https://andreaavr.github.io/ExpositoTOP/)

Práctica 3 de *Laboratorio de Desarrollo y Herramientas* (Grado en Ingeniería Informática, ULL):
herramientas de calidad del producto software y documentación — **Maven, SonarQube, Doxygen y MkDocs**.

📖 **Portal técnico:** <https://andreaavr.github.io/ExpositoTOP/>

## Inicio rápido

```bash
mvn package
java -jar target/ExpositoTOP.jar --iterations 100 --seed 42 c101.txt
```

Requisitos: JDK 17+ y Maven 3.6.3+. Detalles en la [guía de usuario](docs/guia-usuario.md).

## Estructura

```text
├── pom.xml                       Proyecto Maven (plugins de construcción, calidad y documentación)
├── src/main/java                 Código fuente (paquetes top, es.ull.esit.utilities, es.ull.esit.utils)
├── src/test/java                 Tests JUnit (43)
├── Instances/                    Instancias TOPTW (Solomon y Cordeau)
├── Doxyfile                      Configuración de Doxygen (HAVE_DOT = YES, HTML, LaTeX, XML)
├── doxygen/mainpage.md           Página principal de la referencia Doxygen
├── mkdocs.yml                    Portal MkDocs (tema Material + MkDoxy)
├── docs/                         Páginas del portal (guías, arquitectura, calidad, ADR)
│   └── pdf/                      Documentación técnica en PDF
├── scripts/build_pdf.py          Genera el PDF del portal
├── sonarqube/docker-compose.yml  SonarQube local
└── .github/workflows/            CI (Maven + SonarQube) y publicación en GitHub Pages
```

## Órdenes útiles

| Orden | Resultado |
|---|---|
| `mvn test` | Ejecuta los tests |
| `mvn verify` | Tests, cobertura (JaCoCo), Checkstyle, PMD/CPD, SpotBugs |
| `mvn verify sonar:sonar -Dsonar.token=…` | Análisis en SonarQube |
| `mvn exec:java` | Ejecuta el recomendador desde Maven |
| `mvn -Pdocs package` | Genera Doxygen y el portal MkDocs |
| `mkdocs serve` | Vista previa del portal |

## Historial

La etiqueta `v0-original` contiene el proyecto tal como se recibió (NetBeans/Ant). Los cambios
posteriores se describen en el [registro de cambios](docs/changelog.md) y en las
[incidencias de calidad](docs/calidad/incidencias.md).
