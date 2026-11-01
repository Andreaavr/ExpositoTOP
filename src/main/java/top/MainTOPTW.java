package top;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * \brief Punto de entrada del recomendador turístico RTVRPTW / TOPTW.
 *
 * \details Ejecuta el GRASP sobre un conjunto de instancias con tres tamaños de lista
 * restringida de candidatos (3, 5 y 7) y escribe en la salida estándar el informe de cada
 * solución construida, la media y la mejor puntuación.
 *
 * Uso:
 * \code
 * java -jar ExpositoTOP.jar [--dir DIR] [--iterations N] [--seed S] [instancia ...]
 * \endcode
 * Sin argumentos se reproduce el experimento original: las 29 instancias de Solomon
 * (c1, r1, rc1) del directorio Instances/TOPTW con 10000 iteraciones.
 */
public final class MainTOPTW {

    /** \brief Directorio de instancias por defecto. */
    public static final String DEFAULT_DIR = "Instances/TOPTW";
    /** \brief Número de iteraciones por defecto del experimento original. */
    public static final int DEFAULT_ITERATIONS = 10000;
    /** \brief Tamaños de RCL evaluados. */
    private static final int[] RCL_SIZES = {3, 5, 7};
    /** \brief Instancias del experimento original. */
    static final List<String> DEFAULT_INSTANCES = List.of(
            "c101.txt", "c102.txt", "c103.txt", "c104.txt", "c105.txt", "c106.txt", "c107.txt", "c108.txt",
            "c109.txt",
            "r101.txt", "r102.txt", "r103.txt", "r104.txt", "r105.txt", "r106.txt", "r107.txt", "r108.txt",
            "r109.txt", "r110.txt", "r111.txt", "r112.txt",
            "rc101.txt", "rc102.txt", "rc103.txt", "rc104.txt", "rc105.txt", "rc106.txt", "rc107.txt",
            "rc108.txt");
    /** \brief Registro de errores. */
    private static final Logger LOGGER = Logger.getLogger(MainTOPTW.class.getName());

    /**
     * \brief Clase de arranque: no instanciable.
     */
    private MainTOPTW() {
    }

    /**
     * \brief Opciones de ejecución obtenidas de la línea de órdenes.
     */
    static final class Options {
        /** \brief Directorio de instancias. */
        String dir = DEFAULT_DIR;
        /** \brief Iteraciones del GRASP por tamaño de RCL. */
        int iterations = DEFAULT_ITERATIONS;
        /** \brief Semilla aleatoria, o null para no fijarla. */
        Long seed;
        /** \brief Instancias a resolver. */
        List<String> instances = new ArrayList<>();
    }

    /**
     * \brief Interpreta los argumentos de la línea de órdenes.
     * @param args argumentos.
     * @return opciones de ejecución.
     * @throws IllegalArgumentException si falta el valor de una opción o no es numérico.
     */
    static Options parseArgs(String[] args) {
        Options options = new Options();
        Iterator<String> it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            String arg = it.next();
            switch (arg) {
                case "--dir":
                    options.dir = optionValue(it, arg);
                    break;
                case "--iterations":
                    options.iterations = Integer.parseInt(optionValue(it, arg));
                    break;
                case "--seed":
                    options.seed = Long.parseLong(optionValue(it, arg));
                    break;
                default:
                    options.instances.add(arg);
            }
        }
        if (options.instances.isEmpty()) {
            options.instances.addAll(DEFAULT_INSTANCES);
        }
        return options;
    }

    /**
     * \brief Obtiene el valor que sigue a una opción.
     * @param it     iterador sobre los argumentos, situado tras la opción.
     * @param option nombre de la opción (para el mensaje de error).
     * @return valor de la opción.
     * @throws IllegalArgumentException si la opción no tiene valor.
     */
    private static String optionValue(Iterator<String> it, String option) {
        if (!it.hasNext()) {
            throw new IllegalArgumentException("Falta el valor de " + option);
        }
        return it.next();
    }

    /**
     * \brief Ejecuta el experimento para las opciones dadas.
     * @param options opciones de ejecución.
     * @param out     flujo de salida de los resultados.
     * @throws IOException si alguna instancia no se puede leer.
     */
    static void run(Options options, PrintStream out) throws IOException {
        for (String instance : options.instances) {
            TOPTW problem = TOPTWReader.readProblem(Paths.get(options.dir, instance).toString());
            Random random = options.seed == null ? new Random() : new Random(options.seed);
            TOPTWGRASP grasp = new TOPTWGRASP(new TOPTWSolution(problem), random, out);
            out.println(" --> Instance: " + instance);
            for (int size : RCL_SIZES) {
                grasp.grasp(options.iterations, size);
            }
            out.println("");
        }
    }

    /**
     * \brief Programa principal.
     * @param args [--dir DIR] [--iterations N] [--seed S] [instancia ...]
     */
    public static void main(String[] args) {
        try {
            run(parseArgs(args), System.out); // NOSONAR: la salida estándar es la interfaz de la aplicación CLI
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Error ejecutando el recomendador", e);
            System.exit(1); // NOSONAR: código de salida de una aplicación de consola
        }
    }
}