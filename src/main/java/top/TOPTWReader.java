package top;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import es.ull.esit.utilities.ExpositoUtilities;

/**
 * \brief Lector de instancias TOPTW en el formato de Cordeau / Righini.
 *
 * \details Formato del fichero (ver Instances/TOPTW/formatInstances.txt):
 * - Línea 1: <tt>k v N t</tt>, donde v es el número de rutas y N el número de POI.
 * - Línea 2: <tt>D Q</tt> (no se usa).
 * - Una línea por nodo (empezando por el depósito, índice 0):
 *   <tt>i x y d S f a lista O C</tt>, donde d es la duración de la visita, S la
 *   puntuación, la lista tiene longitud variable (depende de a) y O/C son la apertura y
 *   el cierre de la ventana temporal.
 *
 * Como la lista tiene longitud variable, la ventana temporal se toma siempre de los dos
 * últimos campos de la línea.
 */
public final class TOPTWReader {

    /** \brief Posición del campo x. */
    private static final int X_FIELD = 1;
    /** \brief Posición del campo y. */
    private static final int Y_FIELD = 2;
    /** \brief Posición del campo de duración de la visita. */
    private static final int SERVICE_FIELD = 3;
    /** \brief Posición del campo de puntuación. */
    private static final int SCORE_FIELD = 4;

    /**
     * \brief Clase de utilidad: no instanciable.
     */
    private TOPTWReader() {
    }

    /**
     * \brief Lee una instancia desde un fichero.
     *
     * \details Carga coordenadas, tiempos de servicio, puntuaciones y ventanas temporales,
     * calcula la matriz de distancias y fija la duración máxima de ruta al cierre de la
     * ventana del depósito.
     *
     * @param filePath ruta del fichero de la instancia.
     * @return instancia cargada.
     * @throws IOException si el fichero no existe, no se puede leer o está incompleto.
     */
    public static TOPTW readProblem(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        TOPTW problem;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String[] parts = nextFields(reader, path);
            problem = new TOPTW(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]));
            nextFields(reader, path); // línea "D Q": no se utiliza
            for (int i = 0; i < problem.getPOIs() + 1; i++) {
                parts = nextFields(reader, path);
                problem.setX(i, Double.parseDouble(parts[X_FIELD]));
                problem.setY(i, Double.parseDouble(parts[Y_FIELD]));
                problem.setServiceTime(i, Double.parseDouble(parts[SERVICE_FIELD]));
                problem.setScore(i, Double.parseDouble(parts[SCORE_FIELD]));
                problem.setReadyTime(i, Double.parseDouble(parts[parts.length - 2]));
                problem.setDueTime(i, Double.parseDouble(parts[parts.length - 1]));
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IOException("Formato de instancia no válido en " + path, e);
        }
        problem.calculateDistanceMatrix();
        problem.setMaxTimePerRoute(problem.getDueTime(0));
        return problem;
    }

    /**
     * \brief Lee la siguiente línea y la divide en campos.
     * @param reader lector abierto.
     * @param path   fichero (para los mensajes de error).
     * @return campos de la línea normalizada.
     * @throws IOException si se alcanza el final del fichero.
     */
    private static String[] nextFields(BufferedReader reader, Path path) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Fin de fichero inesperado en " + path);
        }
        return ExpositoUtilities.simplifyString(line).split(" ");
    }
}