# ADR 0001 — Migración de NetBeans/Ant a Maven

**Estado:** aceptada

## Contexto

El proyecto se construía con el `build.xml` generado por NetBeans, dependiente de
`nbproject/`. No declaraba dependencias ni versión de Java de forma portable, incluía binarios
en el repositorio y no podía construirse fuera del IDE ni integrarse con SonarQube o con CI sin
configuración manual.

## Decisión

Adoptar Maven con la estructura estándar (`src/main/java`, `src/test/java`), Java 17 (LTS) y
todos los plugins con la versión fijada. Eliminar los ficheros de Ant/NetBeans y los binarios.
Renombrar `mainTOPTW` a `MainTOPTW` y usarla como `Main-Class` del JAR.

## Consecuencias

- Construcción reproducible con una orden (`mvn verify`) en cualquier IDE y en CI.
- Integración directa con JaCoCo, SonarQube, Checkstyle, PMD y SpotBugs.
- Se pierde la configuración de ejecución de NetBeans, sustituida por `exec-maven-plugin`.
- Quien ejecute el proyecto necesita JDK 17 o superior (el enforcer lo comprueba).
