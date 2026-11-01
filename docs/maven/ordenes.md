# Órdenes de Maven y uso desde el IDE

## Instalación y verificación

=== "Linux / macOS"

    ```bash
    # Opción 1: gestor de paquetes
    sudo apt install maven          # Debian/Ubuntu
    brew install maven              # macOS
    # Opción 2: binario oficial
    wget https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
    tar xzf apache-maven-3.9.11-bin.tar.gz -C /opt
    export PATH=/opt/apache-maven-3.9.11/bin:$PATH
    ```

=== "Windows"

    1. Descargar el `.zip` binario de <https://maven.apache.org/download.cgi>.
    2. Descomprimir en `C:\Program Files\Apache\maven`.
    3. Definir `JAVA_HOME` y añadir `C:\Program Files\Apache\maven\bin` al `PATH`.

Verificación:

```console
$ mvn -v
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: /opt/maven
Java version: 21.0.12.1, vendor: Ubuntu, runtime: /usr/lib/jvm/java-21-openjdk-amd64
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "6.18.44", arch: "amd64", family: "unix"
```

Una prueba básica es generar un proyecto de ejemplo con un arquetipo y construirlo:

```bash
mvn archetype:generate -DgroupId=es.ull.prueba -DartifactId=hola \
    -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5 -DinteractiveMode=false
cd hola && mvn package && java -cp target/hola-1.0-SNAPSHOT.jar es.ull.prueba.App
```

## Órdenes principales (línea de órdenes)

| Orden | Qué hace |
|---|---|
| `mvn validate` | Valida el POM y ejecuta las reglas del enforcer |
| `mvn compile` | Compila el código de producción |
| `mvn test` | Compila y ejecuta los tests |
| `mvn test -Dtest=TOPTWReaderTest` | Ejecuta una sola clase de test (`#método` para un método) |
| `mvn package` | Genera los JAR en `target/` |
| `mvn package -DskipTests` | Empaqueta sin ejecutar los tests |
| `mvn verify` | Además, cobertura y análisis estático |
| `mvn install` | Instala el artefacto en el repositorio local |
| `mvn deploy` | Publica el artefacto en el repositorio remoto configurado |
| `mvn clean` | Borra `target/` (se combina: `mvn clean install`) |
| `mvn site` | Genera el sitio de informes en `target/site/` |
| `mvn exec:java` | Ejecuta `MainTOPTW` con los argumentos de `exec.args` |
| `mvn verify sonar:sonar` | Análisis de calidad en SonarQube |
| `mvn -Pdocs package` | Construcción más documentación (Doxygen + MkDocs) |
| `mvn dependency:tree` | Árbol de dependencias |
| `mvn help:effective-pom` | POM efectivo (con lo heredado del Super POM) |
| `mvn versions:display-plugin-updates` | Plugins con versiones más recientes |

Opciones globales útiles: `-q` (silencioso), `-X` (depuración), `-o` (sin conexión),
`-B` (no interactivo, para CI), `-U` (forzar actualización de *snapshots*), `-T 1C` (un hilo
por núcleo), `-D propiedad=valor` y `-P perfil`.

## Uso desde el IDE

=== "IntelliJ IDEA"

    1. **File → Open** y seleccionar el `pom.xml` → *Open as Project*. IntelliJ importa el
       proyecto, descarga dependencias y marca `src/main/java` y `src/test/java`.
    2. Panel **Maven** (lateral derecho): *Lifecycle* contiene las fases
       (`validate` … `deploy`); doble clic para ejecutarlas. *Plugins* lista los goals
       (`exec:java`, `sonar:sonar`, `jacoco:report`…).
    3. **Run → Edit Configurations → + → Maven** para guardar órdenes con argumentos
       (por ejemplo `verify sonar:sonar -Dsonar.token=...`).
    4. Botón *Reload All Maven Projects* tras modificar el `pom.xml`.

=== "Eclipse"

    1. **File → Import → Maven → Existing Maven Projects** y seleccionar la carpeta.
    2. Clic derecho en el proyecto → **Run As**: `Maven build…` (permite escribir los goals,
       p. ej. `clean verify`), `Maven install`, `Maven test`, `Maven clean`.
    3. Clic derecho → **Maven → Update Project** tras cambiar el `pom.xml`.

=== "NetBeans"

    1. **File → Open Project** y elegir la carpeta (NetBeans reconoce el `pom.xml`).
    2. Botones *Build* (`install`), *Clean and Build* (`clean install`), *Test* y *Run*.
    3. Clic derecho → **Run Maven → Goals…** para cualquier fase o goal.

=== "VS Code"

    Con el *Extension Pack for Java*: vista **Maven** en el explorador, clic derecho sobre el
    proyecto o sobre una fase del *Lifecycle* para ejecutarla.

## Pruebas del ciclo de vida

La siguiente tabla resume lo que produce cada fase de este proyecto. Las capturas desde la
consola y desde el IDE deben incluirse en el informe.

| Fase | Comprobación |
|---|---|
| `mvn validate` | `BUILD SUCCESS` sin compilar nada; falla si se usa un JDK < 17 |
| `mvn compile` | `target/classes/top/*.class`; sin avisos de `-Xlint` |
| `mvn test` | `Tests run: 43, Failures: 0, Errors: 0, Skipped: 0` |
| `mvn package` | `target/ExpositoTOP.jar`; `java -jar target/ExpositoTOP.jar --iterations 10 c101.txt` funciona |
| `mvn verify` | `target/site/jacoco/index.html`, `target/checkstyle-result.xml`, `target/pmd.xml`, `target/spotbugsXml.xml` |
| `mvn install` | `~/.m2/repository/es/ull/esit/expositotop/1.0.0-SNAPSHOT/` |
| `mvn deploy` | `target/deploy-repo/snapshots/es/ull/esit/expositotop/1.0.0-SNAPSHOT/` con `maven-metadata.xml` |
