package top;

import java.util.Arrays;
import java.util.List;

import es.ull.esit.utilities.ExpositoUtilities;

/**
 * \brief Modelo de una instancia del problema TOPTW (Team Orienteering Problem with Time Windows).
 *
 * \details Representa el conjunto de puntos de interés (POI) que un recomendador turístico
 * puede incluir en las rutas de los visitantes. Cada POI tiene coordenadas, una puntuación
 * (beneficio o interés turístico), una ventana temporal [readyTime, dueTime] en la que
 * puede comenzar la visita y un tiempo de servicio (duración de la visita).
 *
 * El índice 0 corresponde al depósito (punto de salida y llegada de todas las rutas,
 * típicamente el hotel). Los índices mayores que el número de POI se reservan para los
 * depósitos "virtuales" que la solución crea al abrir rutas adicionales; todos ellos se
 * tratan como el depósito real (índice 0) al consultar datos.
 *
 * La matriz de distancias es euclídea y simétrica; el tiempo de viaje entre dos nodos se
 * considera igual a su distancia.
 */
public class TOPTW {
    /** \brief Número de POI (sin contar el depósito). */
    private int nodes;
    /** \brief Coordenada X de cada nodo (índice 0 = depósito). */
    private final double[] x;
    /** \brief Coordenada Y de cada nodo (índice 0 = depósito). */
    private final double[] y;
    /** \brief Puntuación o beneficio obtenido al visitar cada nodo. */
    private final double[] score;
    /** \brief Instante más temprano en que puede empezar la visita de cada nodo. */
    private final double[] readyTime;
    /** \brief Instante más tardío en que puede empezar la visita de cada nodo. */
    private final double[] dueTime;
    /** \brief Duración de la visita de cada nodo. */
    private final double[] serviceTime;
    /** \brief Número de vehículos (rutas / días de visita) disponibles. */
    private final int vehicles;
    /** \brief Número de depósitos virtuales añadidos por las soluciones. */
    private int depots;
    /** \brief Duración máxima permitida para cada ruta. */
    private double maxTimePerRoute;
    /** \brief Número máximo de rutas permitidas. */
    private double maxRoutes;
    /** \brief Matriz simétrica de distancias euclídeas entre nodos. */
    private final double[][] distanceMatrix;

    /**
     * \brief Crea una instancia vacía con el número de POI y rutas indicados.
     *
     * \details Reserva espacio para nodes + 1 nodos (los POI más el depósito) e inicializa
     * la matriz de distancias a cero. Las coordenadas y demás datos deben cargarse después
     * mediante los métodos set (normalmente desde TOPTWReader).
     *
     * @param nodes  número de puntos de interés, sin contar el depósito.
     * @param routes número de rutas (vehículos) disponibles.
     */
    public TOPTW(int nodes, int routes) {
        this.nodes = nodes;
        this.depots = 0;
        this.x = new double[this.nodes + 1];
        this.y = new double[this.nodes + 1];
        this.score = new double[this.nodes + 1];
        this.readyTime = new double[this.nodes + 1];
        this.dueTime = new double[this.nodes + 1];
        this.serviceTime = new double[this.nodes + 1];
        this.distanceMatrix = new double[this.nodes + 1][this.nodes + 1];
        this.maxRoutes = routes;
        this.vehicles = routes;
    }

    /**
     * \brief Indica si un índice corresponde a un depósito virtual.
     *
     * @param a índice de nodo.
     * @return true si el índice es mayor que el número de POI (depósito virtual).
     */
    public boolean isDepot(int a) {
        return a > this.nodes;
    }

    /**
     * \brief Traduce un índice de nodo al índice real en los vectores de datos.
     *
     * \details Los depósitos virtuales comparten los datos del depósito real (índice 0).
     *
     * @param index índice de nodo, posiblemente de un depósito virtual.
     * @return el propio índice, o 0 si es un depósito virtual.
     */
    private int dataIndex(int index) {
        return this.isDepot(index) ? 0 : index;
    }

    /**
     * \brief Calcula la longitud total de una ruta dada como vector de nodos.
     *
     * @param route secuencia de nodos visitados.
     * @return suma de las distancias entre nodos consecutivos.
     */
    public double getDistance(int[] route) {
        double distance = 0.0;
        for (int i = 0; i < route.length - 1; i++) {
            distance += this.getDistance(route[i], route[i + 1]);
        }
        return distance;
    }

    /**
     * \brief Calcula la longitud total de una ruta dada como lista de nodos.
     *
     * @param route secuencia de nodos visitados.
     * @return suma de las distancias entre nodos consecutivos.
     */
    public double getDistance(List<Integer> route) {
        double distance = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            distance += this.getDistance(route.get(i), route.get(i + 1));
        }
        return distance;
    }

    /**
     * \brief Calcula la longitud acumulada de un conjunto de rutas.
     *
     * @param routes rutas a evaluar.
     * @return suma de las longitudes de todas las rutas.
     */
    public double getTotalDistance(List<List<Integer>> routes) {
        double distance = 0.0;
        for (List<Integer> route : routes) {
            distance += this.getDistance(route);
        }
        return distance;
    }

    /**
     * \brief Rellena la matriz de distancias euclídeas a partir de las coordenadas.
     *
     * \details Debe invocarse una vez cargadas las coordenadas de todos los nodos.
     */
    public void calculateDistanceMatrix() {
        for (int i = 0; i < this.nodes + 1; i++) {
            for (int j = 0; j < this.nodes + 1; j++) {
                if (i != j) {
                    double diffXs = this.x[i] - this.x[j];
                    double diffYs = this.y[i] - this.y[j];
                    this.distanceMatrix[i][j] = Math.sqrt(diffXs * diffXs + diffYs * diffYs);
                    this.distanceMatrix[j][i] = this.distanceMatrix[i][j];
                } else {
                    this.distanceMatrix[i][j] = 0.0;
                }
            }
        }
    }

    /**
     * \brief Devuelve la duración máxima de cada ruta.
     * @return tiempo máximo por ruta.
     */
    public double getMaxTimePerRoute() {
        return maxTimePerRoute;
    }

    /**
     * \brief Establece la duración máxima de cada ruta.
     * @param maxTimePerRoute tiempo máximo por ruta.
     */
    public void setMaxTimePerRoute(double maxTimePerRoute) {
        this.maxTimePerRoute = maxTimePerRoute;
    }

    /**
     * \brief Devuelve el número máximo de rutas.
     * @return número máximo de rutas.
     */
    public double getMaxRoutes() {
        return maxRoutes;
    }

    /**
     * \brief Establece el número máximo de rutas.
     * @param maxRoutes número máximo de rutas.
     */
    public void setMaxRoutes(double maxRoutes) {
        this.maxRoutes = maxRoutes;
    }

    /**
     * \brief Devuelve el número de puntos de interés (sin el depósito).
     * @return número de POI.
     */
    public int getPOIs() {
        return this.nodes;
    }

    /**
     * \brief Distancia entre dos nodos.
     *
     * @param i nodo origen (los depósitos virtuales se tratan como el 0).
     * @param j nodo destino (los depósitos virtuales se tratan como el 0).
     * @return distancia euclídea entre ambos.
     */
    public double getDistance(int i, int j) {
        return this.distanceMatrix[this.dataIndex(i)][this.dataIndex(j)];
    }

    /**
     * \brief Tiempo de viaje entre dos nodos.
     *
     * \details En este modelo el tiempo de viaje coincide con la distancia.
     *
     * @param i nodo origen.
     * @param j nodo destino.
     * @return tiempo de viaje entre ambos.
     */
    public double getTime(int i, int j) {
        return this.getDistance(i, j);
    }

    /**
     * \brief Devuelve el número de POI.
     * @return número de POI.
     */
    public int getNodes() {
        return this.nodes;
    }

    /**
     * \brief Modifica el número de POI.
     * @param nodes nuevo número de POI.
     */
    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    /**
     * \brief Coordenada X de un nodo.
     * @param index índice del nodo.
     * @return coordenada X.
     */
    public double getX(int index) {
        return this.x[this.dataIndex(index)];
    }

    /**
     * \brief Establece la coordenada X de un nodo.
     * @param index índice del nodo.
     * @param x     coordenada X.
     */
    public void setX(int index, double x) {
        this.x[index] = x;
    }

    /**
     * \brief Coordenada Y de un nodo.
     * @param index índice del nodo.
     * @return coordenada Y.
     */
    public double getY(int index) {
        return this.y[this.dataIndex(index)];
    }

    /**
     * \brief Establece la coordenada Y de un nodo.
     * @param index índice del nodo.
     * @param y     coordenada Y.
     */
    public void setY(int index, double y) {
        this.y[index] = y;
    }

    /**
     * \brief Puntuación de un nodo.
     * @param index índice del nodo.
     * @return puntuación (beneficio) del nodo.
     */
    public double getScore(int index) {
        return this.score[this.dataIndex(index)];
    }

    /**
     * \brief Copia del vector de puntuaciones.
     *
     * \details Se devuelve una copia para no exponer la representación interna.
     *
     * @return vector de puntuaciones indexado por nodo.
     */
    public double[] getScore() {
        return this.score.clone();
    }

    /**
     * \brief Establece la puntuación de un nodo.
     * @param index índice del nodo.
     * @param score puntuación.
     */
    public void setScore(int index, double score) {
        this.score[index] = score;
    }

    /**
     * \brief Inicio de la ventana temporal de un nodo.
     * @param index índice del nodo.
     * @return instante más temprano de comienzo de la visita.
     */
    public double getReadyTime(int index) {
        return this.readyTime[this.dataIndex(index)];
    }

    /**
     * \brief Establece el inicio de la ventana temporal de un nodo.
     * @param index     índice del nodo.
     * @param readyTime instante más temprano de comienzo de la visita.
     */
    public void setReadyTime(int index, double readyTime) {
        this.readyTime[index] = readyTime;
    }

    /**
     * \brief Fin de la ventana temporal de un nodo.
     * @param index índice del nodo.
     * @return instante más tardío de comienzo de la visita.
     */
    public double getDueTime(int index) {
        return this.dueTime[this.dataIndex(index)];
    }

    /**
     * \brief Establece el fin de la ventana temporal de un nodo.
     * @param index   índice del nodo.
     * @param dueTime instante más tardío de comienzo de la visita.
     */
    public void setDueTime(int index, double dueTime) {
        this.dueTime[index] = dueTime;
    }

    /**
     * \brief Tiempo de servicio (duración de la visita) de un nodo.
     * @param index índice del nodo.
     * @return duración de la visita.
     */
    public double getServiceTime(int index) {
        return this.serviceTime[this.dataIndex(index)];
    }

    /**
     * \brief Establece el tiempo de servicio de un nodo.
     * @param index       índice del nodo.
     * @param serviceTime duración de la visita.
     */
    public void setServiceTime(int index, double serviceTime) {
        this.serviceTime[index] = serviceTime;
    }

    /**
     * \brief Número de vehículos (rutas) disponibles.
     * @return número de vehículos.
     */
    public int getVehicles() {
        return this.vehicles;
    }

    /**
     * \brief Número de depósitos virtuales registrados.
     * @return número de depósitos añadidos con addNodeDepot().
     */
    public int getDepots() {
        return this.depots;
    }

    /**
     * \brief Representación tabular de la instancia.
     * @return texto con los datos de cada POI y el número de vehículos.
     */
    @Override
    public String toString() {
        final int columnWidth = 15;
        StringBuilder text = new StringBuilder("Nodes: ").append(this.nodes).append('\n');
        String[] strings = {"CUST NO.", "XCOORD.", "YCOORD.", "SCORE", "READY TIME", "DUE DATE", "SERVICE TIME"};
        int[] width = new int[strings.length];
        Arrays.fill(width, columnWidth);
        text.append(ExpositoUtilities.getFormat(strings, width)).append('\n');
        for (int i = 0; i < this.nodes; i++) {
            String[] row = {
                    Integer.toString(i),
                    String.valueOf(this.x[i]),
                    String.valueOf(this.y[i]),
                    String.valueOf(this.score[i]),
                    String.valueOf(this.readyTime[i]),
                    String.valueOf(this.dueTime[i]),
                    String.valueOf(this.serviceTime[i])
            };
            text.append(ExpositoUtilities.getFormat(row, width)).append('\n');
        }
        text.append("Vehicles: ").append(this.vehicles).append('\n');
        String[] header = {"VEHICLE", "CAPACITY"};
        int[] headerWidth = new int[header.length];
        Arrays.fill(headerWidth, columnWidth);
        text.append(ExpositoUtilities.getFormat(header, headerWidth)).append('\n');
        return text.toString();
    }

    /**
     * \brief Añade un nuevo POI incrementando el contador de nodos.
     * @return nuevo número de POI.
     */
    public int addNode() {
        this.nodes++;
        return this.nodes;
    }

    /**
     * \brief Registra un nuevo depósito virtual.
     * @return número de depósitos virtuales tras el registro.
     */
    public int addNodeDepot() {
        this.depots++;
        return this.depots;
    }
}