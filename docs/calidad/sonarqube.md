# Análisis de calidad con SonarQube

SonarQube realiza **análisis estático** del código: detecta *bugs*, vulnerabilidades,
*security hotspots* y *code smells*, mide duplicaciones, complejidad y cobertura de tests, y
decide si el proyecto supera una **Quality Gate** (umbral de calidad). Con Maven, el análisis
lo ejecuta el `sonar-maven-plugin`, que aprovecha la información del POM (fuentes, binarios,
dependencias y versión de Java).

## 1. Levantar SonarQube

=== "Docker (recomendado)"

    ```bash
    docker compose -f sonarqube/docker-compose.yml up -d
    # o bien, sin compose:
    docker run -d --name sonarqube -p 9000:9000 sonarqube:community
    ```

    Abrir <http://localhost:9000>, entrar con `admin` / `admin` y cambiar la contraseña.

=== "Distribución ZIP"

    1. Descargar *SonarQube Community Build* de <https://www.sonarsource.com/products/sonarqube/downloads/>.
    2. Descomprimir y ejecutar `bin/linux-x86-64/sonar.sh start` (o `bin\windows-x86-64\StartSonar.bat`).
    3. Requiere Java 17 o 21 y, en Linux, `vm.max_map_count ≥ 524288`.

=== "SonarQube Cloud"

    Para el repositorio de GitHub se puede usar SonarQube Cloud (gratuito para proyectos
    públicos): importar el repositorio en <https://sonarcloud.io>, crear el secreto
    `SONAR_TOKEN` en GitHub y fijar las variables `SONAR_ORGANIZATION` y `SONAR_PROJECT_KEY`.
    El *workflow* `.github/workflows/ci.yml` ya ejecuta el análisis cuando el secreto existe.

## 2. Crear el proyecto y el token

1. **Projects → Create project → Local project**. Clave: `ExpositoTOP`.
2. **Analysis method → Locally → Generate a token** (o *My Account → Security → Generate
   Tokens*, tipo *Project Analysis Token*). Guardar el token.

## 3. Configuración en el `pom.xml`

```xml
<sonar.projectKey>ExpositoTOP</sonar.projectKey>
<sonar.projectName>ExpositoTOP - RTVRPTW</sonar.projectName>
<sonar.host.url>http://localhost:9000</sonar.host.url>
<sonar.sourceEncoding>UTF-8</sonar.sourceEncoding>
<sonar.java.source>17</sonar.java.source>
<sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
<sonar.exclusions>Instances/**,docs/**,site/**</sonar.exclusions>
```

Y el plugin, con versión fijada en `pluginManagement`:

```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>${sonar-maven-plugin.version}</version>
</plugin>
```

!!! tip "No guardar el token en el POM"
    El token es una credencial: se pasa por la línea de órdenes o por la variable de entorno
    `SONAR_TOKEN`, nunca en el repositorio.

## 4. Ejecutar el análisis

```bash
mvn clean verify sonar:sonar -Dsonar.token=TOKEN
```

`verify` es necesario antes: genera los `.class` (SonarQube Java los necesita para el análisis
semántico) y el informe XML de JaCoCo con la cobertura. Al terminar, el log muestra
`ANALYSIS SUCCESSFUL, you can find the results at: http://localhost:9000/dashboard?id=ExpositoTOP`.

Desde el IDE: crear una configuración de ejecución Maven con
`clean verify sonar:sonar -Dsonar.token=TOKEN`. Con **SonarQube for IDE** (antes SonarLint) y
el modo *Connected Mode* se ven las mismas incidencias mientras se edita.

## 5. Análisis del código original

Para comparar el antes y el después, se analiza también la versión original (etiqueta
`v0-original` del repositorio). Como no tenía `pom.xml`, se compila con `javac` y se usa el
escáner independiente (*SonarScanner CLI*) en Docker:

```bash
git worktree add ../ExpositoTOP-original v0-original
cd ../ExpositoTOP-original
mkdir -p out && javac -d out $(find src -name "*.java")
docker run --rm --network host -v "$PWD:/usr/src" sonarsource/sonar-scanner-cli \
  -Dsonar.host.url=http://localhost:9000 -Dsonar.token=TOKEN \
  -Dsonar.projectKey=ExpositoTOP-original -Dsonar.sources=src \
  -Dsonar.java.binaries=out -Dsonar.exclusions="**/*~"
```

Después se analiza la rama `main` con Maven (paso 4) y se comparan los dos proyectos.

## 6. Qué mirar en el panel

| Sección | Qué indica |
|---|---|
| **Quality Gate** | *Passed/Failed* según las condiciones de *Sonar way* sobre el código nuevo (cobertura ≥ 80 %, duplicación ≤ 3 %, sin incidencias nuevas, *hotspots* revisados) |
| **Reliability / Security / Maintainability** | Incidencias por cualidad del software y su calificación A–E |
| **Security Hotspots** | Código sensible que debe revisar una persona (aquí, el uso de `Random`) |
| **Coverage** | Cobertura de líneas y condiciones a partir del XML de JaCoCo |
| **Duplications** | Bloques y líneas duplicadas |
| **Measures → Complexity** | Complejidad ciclomática y cognitiva por fichero y método |
| **Technical Debt** | Tiempo estimado para corregir los *code smells* |

Las incidencias encontradas y cómo se han resuelto se detallan en
[Incidencias y soluciones](incidencias.md).
