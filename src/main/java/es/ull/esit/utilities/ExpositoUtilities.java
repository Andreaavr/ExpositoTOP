package es.ull.esit.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * \brief Funciones de utilidad: formato de texto tabular, ficheros, matrices y grafos.
 *
 * \details Clase estática usada principalmente para imprimir tablas alineadas en columnas
 * (los números reales se formatean con tres decimales y punto decimal).
 */
public final class ExpositoUtilities {

    /** \brief Ancho de columna por defecto. */
    public static final int DEFAULT_COLUMN_WIDTH = 10;
    /** \brief Alineación a la izquierda. */
    public static final int ALIGNMENT_LEFT = 1;
    /** \brief Alineación a la derecha. */
    public static final int ALIGNMENT_RIGHT = 2;

    /**
     * \brief Clase de utilidad: no instanciable.
     */
    private ExpositoUtilities() {
    }

    /**
     * \brief Copia el contenido de un fichero de texto en un flujo de salida.
     * @param file ruta del fichero.
     * @param out  flujo de salida.
     * @throws IOException si el fichero no se puede leer.
     */
    public static void printFile(String file, PrintStream out) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
        }
    }

    /**
     * \brief Normaliza los espacios de una cadena.
     * \details Sustituye tabuladores por espacios, colapsa espacios repetidos y recorta.
     * @param string cadena original.
     * @return cadena con los campos separados por un único espacio.
     */
    public static String simplifyString(String string) {
        return string.replace('\t', ' ').replaceAll(" {2,}", " ").trim();
    }

    /**
     * \brief Producto de matrices.
     * @param a matriz m x n.
     * @param b matriz n x p.
     * @return matriz producto m x p (vacía si a está vacía).
     * @throws IllegalArgumentException si las dimensiones no son compatibles.
     */
    public static double[][] multiplyMatrices(double[][] a, double[][] b) {
        if (a.length == 0) {
            return new double[0][0];
        }
        if (a[0].length != b.length) {
            throw new IllegalArgumentException(
                    "Dimensiones incompatibles: " + a[0].length + " columnas frente a " + b.length + " filas");
        }
        int n = a[0].length;
        int m = a.length;
        int p = b[0].length;
        double[][] ans = new double[m][p];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                for (int k = 0; k < n; k++) {
                    ans[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return ans;
    }

    /**
     * \brief Escribe un texto en un fichero, sobrescribiéndolo.
     * @param file ruta del fichero.
     * @param text texto a escribir.
     * @throws IOException si no se puede escribir.
     */
    public static void writeTextToFile(String file, String text) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8)) {
            writer.write(text);
        }
    }

    /**
     * \brief Formatea una cadena numérica: los reales no enteros pasan a tres decimales.
     * @param string cadena a formatear.
     * @return cadena formateada (sin cambios si es entera o no numérica).
     */
    public static String getFormat(String string) {
        if (!ExpositoUtilities.isInteger(string) && ExpositoUtilities.isDouble(string)) {
            return ExpositoUtilities.getFormat(Double.parseDouble(string));
        }
        return string;
    }

    /**
     * \brief Formatea un real con tres decimales y punto decimal.
     * @param value valor.
     * @return texto formateado.
     */
    public static String getFormat(double value) {
        return getFormat(value, 3);
    }

    /**
     * \brief Formatea un real con el número de decimales indicado y punto decimal.
     * @param value valor.
     * @param zeros número de decimales.
     * @return texto formateado.
     */
    public static String getFormat(double value, int zeros) {
        StringBuilder format = new StringBuilder("0.");
        for (int i = 0; i < zeros; i++) {
            format.append('0');
        }
        DecimalFormat decimalFormatter = new DecimalFormat(format.toString());
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimalFormatter.setDecimalFormatSymbols(symbols);
        return decimalFormatter.format(value);
    }

    /**
     * \brief Alinea una cadena a la derecha en una columna.
     * @param string cadena.
     * @param width  ancho de la columna.
     * @return cadena alineada.
     */
    public static String getFormat(String string, int width) {
        return ExpositoUtilities.getFormat(string, width, ExpositoUtilities.ALIGNMENT_RIGHT);
    }

    /**
     * \brief Alinea una cadena en una columna.
     * @param string    cadena.
     * @param width     ancho de la columna.
     * @param alignment ALIGNMENT_LEFT o ALIGNMENT_RIGHT.
     * @return cadena alineada.
     */
    public static String getFormat(String string, int width, int alignment) {
        String format = alignment == ExpositoUtilities.ALIGNMENT_LEFT ? "%-" + width + "s" : "%" + width + "s";
        return String.format(format, string);
    }

    /**
     * \brief Formatea una lista de cadenas en columnas del mismo ancho.
     * @param strings cadenas.
     * @param width   ancho de cada columna.
     * @return fila formateada.
     */
    public static String getFormat(List<String> strings, int width) {
        return getFormat(strings.toArray(new String[0]), width);
    }

    /**
     * \brief Formatea una lista de enteros en columnas de ancho DEFAULT_COLUMN_WIDTH.
     * @param values enteros.
     * @return fila formateada.
     */
    public static String getFormatIntegers(List<Integer> values) {
        StringBuilder result = new StringBuilder();
        for (Integer value : values) {
            result.append(String.format("%" + DEFAULT_COLUMN_WIDTH + "s", value));
        }
        return result.toString();
    }

    /**
     * \brief Formatea un vector de cadenas en columnas del mismo ancho, alineadas a la derecha.
     * @param strings cadenas.
     * @param width   ancho de cada columna.
     * @return fila formateada.
     */
    public static String getFormat(String[] strings, int width) {
        int[] widths = new int[strings.length];
        Arrays.fill(widths, width);
        return ExpositoUtilities.getFormat(strings, widths);
    }

    /**
     * \brief Formatea una matriz de cadenas: una fila por línea.
     * @param matrixStrings matriz de cadenas.
     * @param width         ancho de cada columna.
     * @return texto formateado.
     */
    public static String getFormat(String[][] matrixStrings, int width) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < matrixStrings.length; i++) {
            result.append(ExpositoUtilities.getFormat(matrixStrings[i], width));
            if (i < matrixStrings.length - 1) {
                result.append('\n');
            }
        }
        return result.toString();
    }

    /**
     * \brief Formatea un vector de cadenas con el ancho por defecto.
     * @param strings cadenas.
     * @return fila formateada.
     */
    public static String getFormat(String[] strings) {
        return ExpositoUtilities.getFormat(strings, DEFAULT_COLUMN_WIDTH);
    }

    /**
     * \brief Formatea un vector de cadenas con un ancho por columna, alineadas a la derecha.
     * @param strings cadenas.
     * @param width   ancho de cada columna.
     * @return fila formateada.
     */
    public static String getFormat(String[] strings, int[] width) {
        int[] alignment = new int[strings.length];
        Arrays.fill(alignment, ExpositoUtilities.ALIGNMENT_RIGHT);
        return ExpositoUtilities.getFormat(strings, width, alignment);
    }

    /**
     * \brief Formatea un vector de cadenas con ancho y alineación por columna.
     * \details Cada valor numérico real se escribe con tres decimales.
     * @param strings   cadenas.
     * @param width     ancho de cada columna.
     * @param alignment alineación de cada columna.
     * @return fila formateada.
     */
    public static String getFormat(String[] strings, int[] width, int[] alignment) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            result.append(getFormat(getFormat(strings[i]), width[i], alignment[i]));
        }
        return result.toString();
    }

    /**
     * \brief Indica si una cadena representa un entero.
     * @param str cadena.
     * @return true si Integer.parseInt la acepta.
     */
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * \brief Indica si una cadena representa un real.
     * @param str cadena.
     * @return true si Double.parseDouble la acepta.
     */
    public static boolean isDouble(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * \brief Comprueba si un grafo de tareas es acíclico.
     *
     * \details El grafo se da como matriz de distancias en la que Integer.MAX_VALUE indica
     * ausencia de arco. Los nodos 0 y n-1 son el inicio y el fin ficticios, por lo que solo
     * se comprueban los nodos 1..n-2.
     *
     * @param distanceMatrix matriz de adyacencia ponderada.
     * @return true si ninguna tarea real pertenece a un ciclo.
     */
    public static boolean isAcyclic(int[][] distanceMatrix) {
        int numRealTasks = distanceMatrix.length - 2;
        for (int node = 1; node <= numRealTasks; node++) {
            if (ExpositoUtilities.thereIsPath(distanceMatrix, node)) {
                return false;
            }
        }
        return true;
    }

    /**
     * \brief Comprueba si existe un camino de un nodo a sí mismo (ciclo).
     * @param distanceMatrix matriz de adyacencia ponderada (Integer.MAX_VALUE = sin arco).
     * @param node           nodo de partida.
     * @return true si el nodo es alcanzable desde sí mismo.
     */
    public static boolean thereIsPath(int[][] distanceMatrix, int node) {
        Set<Integer> visits = new HashSet<>();
        Set<Integer> noVisits = new HashSet<>();
        for (int i = 0; i < distanceMatrix.length; i++) {
            if (i != node) {
                noVisits.add(i);
            }
        }
        visits.add(node);
        while (!visits.isEmpty()) {
            Iterator<Integer> it = visits.iterator();
            int toCheck = it.next();
            visits.remove(toCheck);
            for (int i = 0; i < distanceMatrix.length; i++) {
                if (!hasArc(distanceMatrix, toCheck, i)) {
                    continue;
                }
                if (i == node) {
                    return true;
                }
                if (noVisits.remove(i)) {
                    visits.add(i);
                }
            }
        }
        return false;
    }

    /**
     * \brief Indica si existe un arco entre dos nodos distintos.
     * @param distanceMatrix matriz de adyacencia ponderada (Integer.MAX_VALUE = sin arco).
     * @param from           nodo origen.
     * @param to             nodo destino.
     * @return true si from y to son distintos y hay arco entre ellos.
     */
    private static boolean hasArc(int[][] distanceMatrix, int from, int to) {
        return from != to && distanceMatrix[from][to] != Integer.MAX_VALUE;
    }
}