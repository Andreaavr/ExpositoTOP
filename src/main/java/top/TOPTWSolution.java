package top;

import java.io.PrintStream;
import java.util.Arrays;

import es.ull.esit.utilities.ExpositoUtilities;

/**
 * \brief Solución del problema TOPTW representada mediante listas doblemente enlazadas.
 *
 * \details Cada ruta comienza y termina en un depósito. El depósito de la primera ruta es el
 * nodo 0; las rutas adicionales usan depósitos virtuales con índices POIs+1, POIs+2, ...
 * Los vectores predecessors y successors almacenan, para cada nodo, su anterior y su
 * siguiente en la ruta, de modo que recorrer una ruta consiste en seguir los sucesores
 * desde su depósito hasta volver a él.
 *
 * La calidad (fitness) de la solución es la suma de las puntuaciones de los POI visitados.
 */
public class TOPTWSolution {
    /** \brief Valor centinela para posiciones aún no asignadas. */
    public static final int NO_INITIALIZED = -1;
    /** \brief Instancia del problema a la que pertenece la solución. */
    private final TOPTW problem;
    /** \brief Predecesor de cada nodo en su ruta. */
    private int[] predecessors;
    /** \brief Sucesor de cada nodo en su ruta. */
    private int[] successors;
    /** \brief Tiempo de espera en cada POI. */
    private final double[] waitingTime;
    /** \brief Posición de cada POI dentro de su ruta. */
    private final int[] positionInRoute;
    /** \brief Índice del depósito de cada ruta creada. */
    private int[] routes;
    /** \brief Vehículos todavía no utilizados. */
    private int availableVehicles;
    /** \brief Último valor de la función objetivo calculado por TOPTWEvaluator. */
    private double objectiveFunctionValue;

    /**
     * \brief Crea una solución vacía asociada a una instancia.
     * @param problem instancia del problema.
     */
    public TOPTWSolution(TOPTW problem) {
        this.problem = problem;
        this.availableVehicles = this.problem.getVehicles();
        this.predecessors = new int[this.problem.getPOIs() + this.problem.getVehicles()];
        this.successors = new int[this.problem.getPOIs() + this.problem.getVehicles()];
        this.waitingTime = new double[this.problem.getPOIs()];
        this.positionInRoute = new int[this.problem.getPOIs()];
        Arrays.fill(this.predecessors, NO_INITIALIZED);
        Arrays.fill(this.successors, NO_INITIALIZED);
        Arrays.fill(this.waitingTime, NO_INITIALIZED);
        Arrays.fill(this.positionInRoute, NO_INITIALIZED);
        this.routes = new int[this.problem.getVehicles()];
        this.objectiveFunctionValue = TOPTWEvaluator.NO_EVALUATED;
    }

    /**
     * \brief Reinicia la solución dejando una única ruta vacía (depósito 0 → depósito 0).
     */
    public void initSolution() {
        this.predecessors = new int[this.problem.getPOIs() + this.problem.getVehicles()];
        this.successors = new int[this.problem.getPOIs() + this.problem.getVehicles()];
        Arrays.fill(this.predecessors, NO_INITIALIZED);
        Arrays.fill(this.successors, NO_INITIALIZED);
        this.routes = new int[this.problem.getVehicles()];
        Arrays.fill(this.routes, NO_INITIALIZED);
        this.routes[0] = 0;
        this.predecessors[0] = 0;
        this.successors[0] = 0;
        this.availableVehicles = this.problem.getVehicles() - 1;
    }

    /**
     * \brief Indica si un nodo es el depósito de alguna ruta de la solución.
     * @param c índice del nodo.
     * @return true si c es el depósito de alguna ruta.
     */
    public boolean isDepot(int c) {
        for (int route : this.routes) {
            if (c == route) {
                return true;
            }
        }
        return false;
    }

    /**
     * \brief Compara dos soluciones por su vector de predecesores.
     * @param o objeto a comparar.
     * @return true si ambas soluciones tienen los mismos predecesores.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TOPTWSolution)) {
            return false;
        }
        TOPTWSolution other = (TOPTWSolution) o;
        return Arrays.equals(this.predecessors, other.predecessors);
    }

    /**
     * \brief Código hash coherente con equals().
     * @return hash del vector de predecesores.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.predecessors);
    }

    /**
     * \brief Vehículos todavía disponibles.
     * @return número de vehículos sin ruta asignada.
     */
    public int getAvailableVehicles() {
        return this.availableVehicles;
    }

    /**
     * \brief Número de rutas creadas en la solución.
     * @return rutas abiertas.
     */
    public int getCreatedRoutes() {
        return this.problem.getVehicles() - this.availableVehicles;
    }

    /**
     * \brief Distancia entre dos nodos de la instancia.
     * @param x nodo origen.
     * @param y nodo destino.
     * @return distancia entre ambos.
     */
    public double getDistance(int x, int y) {
        return this.problem.getDistance(x, y);
    }

    /**
     * \brief Establece el número de vehículos disponibles.
     * @param availableVehicles vehículos disponibles.
     */
    public void setAvailableVehicles(int availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    /**
     * \brief Predecesor de un nodo.
     * @param customer nodo.
     * @return predecesor en su ruta.
     */
    public int getPredecessor(int customer) {
        return this.predecessors[customer];
    }

    /**
     * \brief Copia del vector de predecesores.
     * @return predecesores indexados por nodo.
     */
    public int[] getPredecessors() {
        return this.predecessors.clone();
    }

    /**
     * \brief Instancia del problema.
     * @return problema asociado.
     */
    public TOPTW getProblem() {
        return this.problem;
    }

    /**
     * \brief Valor de la función objetivo almacenado.
     * @return valor objetivo, o TOPTWEvaluator::NO_EVALUATED si no se ha evaluado.
     */
    public double getObjectiveFunctionValue() {
        return this.objectiveFunctionValue;
    }

    /**
     * \brief Posición de un POI en su ruta.
     * @param customer POI.
     * @return posición dentro de la ruta.
     */
    public int getPositionInRoute(int customer) {
        return this.positionInRoute[customer];
    }

    /**
     * \brief Sucesor de un nodo.
     * @param customer nodo.
     * @return sucesor en su ruta.
     */
    public int getSuccessor(int customer) {
        return this.successors[customer];
    }

    /**
     * \brief Copia del vector de sucesores.
     * @return sucesores indexados por nodo.
     */
    public int[] getSuccessors() {
        return this.successors.clone();
    }

    /**
     * \brief Depósito de la ruta k-ésima.
     * @param index número de ruta.
     * @return índice del depósito que abre y cierra esa ruta.
     */
    public int getIndexRoute(int index) {
        return this.routes[index];
    }

    /**
     * \brief Tiempo de espera en un POI.
     * @param customer POI.
     * @return tiempo de espera.
     */
    public double getWaitingTime(int customer) {
        return this.waitingTime[customer];
    }

    /**
     * \brief Almacena el valor de la función objetivo.
     * @param objectiveFunctionValue valor calculado.
     */
    public void setObjectiveFunctionValue(double objectiveFunctionValue) {
        this.objectiveFunctionValue = objectiveFunctionValue;
    }

    /**
     * \brief Establece la posición de un POI en su ruta.
     * @param customer POI.
     * @param position posición.
     */
    public void setPositionInRoute(int customer, int position) {
        this.positionInRoute[customer] = position;
    }

    /**
     * \brief Establece el predecesor de un nodo.
     * @param customer    nodo.
     * @param predecessor predecesor.
     */
    public void setPredecessor(int customer, int predecessor) {
        this.predecessors[customer] = predecessor;
    }

    /**
     * \brief Establece el sucesor de un nodo.
     * @param customer  nodo.
     * @param successor sucesor.
     */
    public void setSuccessor(int customer, int successor) {
        this.successors[customer] = successor;
    }

    /**
     * \brief Establece el tiempo de espera de un POI.
     * @param customer    POI.
     * @param waitingTime tiempo de espera.
     */
    public void setWaitingTime(int customer, double waitingTime) {
        this.waitingTime[customer] = waitingTime;
    }

    /**
     * \brief Informe detallado de la solución.
     *
     * \details Incluye la secuencia de nodos de cada ruta, si la solución es factible,
     * la puntuación total, el coste temporal y una tabla por ruta con los tiempos de
     * llegada y salida de cada nodo.
     *
     * @return texto con el informe.
     */
    public String getInfoSolution() {
        final int columnWidth = 15;
        StringBuilder text = new StringBuilder()
                .append("\nNODES: ").append(this.problem.getPOIs())
                .append("\nMAX TIME PER ROUTE: ").append(this.problem.getMaxTimePerRoute())
                .append("\nMAX NUMBER OF ROUTES: ").append(this.problem.getMaxRoutes()).append('\n');
        StringBuilder textSolution = new StringBuilder("\nSOLUTION: \n");
        double costTimeSolution = 0.0;
        double fitnessScore = 0.0;
        boolean validSolution = true;
        String[] header = {"CUST NO.", "X COORD.", "Y. COORD.", "READY TIME", "DUE DATE",
                "ARRIVE TIME", " LEAVE TIME", "SERVICE TIME"};
        int[] width = new int[header.length];
        Arrays.fill(width, columnWidth);
        for (int k = 0; k < this.getCreatedRoutes(); k++) {
            String[] title = {"\nROUTE " + k};
            text.append(ExpositoUtilities.getFormat(title, new int[]{columnWidth})).append('\n');
            text.append(ExpositoUtilities.getFormat(header, width)).append('\n');
            String[] row = new String[header.length];
            int depot = this.getIndexRoute(k);
            int pre = depot;
            double costTimeRoute = 0.0;
            double fitnessScoreRoute = 0.0;
            row[0] = String.valueOf(pre);
            row[1] = String.valueOf(this.problem.getX(pre));
            row[2] = String.valueOf(this.problem.getY(pre));
            row[3] = String.valueOf(this.problem.getReadyTime(pre));
            row[4] = String.valueOf(this.problem.getDueTime(pre));
            row[5] = "0";
            row[6] = "0";
            row[7] = String.valueOf(this.problem.getServiceTime(pre));
            text.append(ExpositoUtilities.getFormat(row, width)).append('\n');
            int suc;
            do {
                suc = this.getSuccessor(pre);
                textSolution.append(pre).append(" - ");
                row[0] = String.valueOf(suc);
                row[1] = String.valueOf(this.problem.getX(suc));
                row[2] = String.valueOf(this.problem.getY(suc));
                row[3] = String.valueOf(this.problem.getReadyTime(suc));
                row[4] = String.valueOf(this.problem.getDueTime(suc));
                costTimeRoute += this.getDistance(pre, suc);
                if (costTimeRoute < this.problem.getDueTime(suc)) {
                    costTimeRoute = Math.max(costTimeRoute, this.problem.getReadyTime(suc));
                    row[5] = String.valueOf(costTimeRoute);
                    costTimeRoute += this.problem.getServiceTime(suc);
                    row[6] = String.valueOf(costTimeRoute);
                    row[7] = String.valueOf(this.problem.getServiceTime(suc));
                    if (costTimeRoute > this.problem.getMaxTimePerRoute()) {
                        validSolution = false;
                    }
                    fitnessScoreRoute += this.problem.getScore(suc);
                } else {
                    validSolution = false;
                    row[5] = "-";
                    row[6] = "-";
                    row[7] = String.valueOf(this.problem.getServiceTime(suc));
                }
                pre = suc;
                text.append(ExpositoUtilities.getFormat(row, width)).append('\n');
            } while (suc != depot);
            textSolution.append(suc).append('\n');
            costTimeSolution += costTimeRoute;
            fitnessScore += fitnessScoreRoute;
        }
        textSolution.append("FEASIBLE SOLUTION: ").append(validSolution)
                .append("\nSCORE: ").append(fitnessScore)
                .append("\nTIME COST: ").append(costTimeSolution).append('\n');
        return textSolution.append(text).toString();
    }

    /**
     * \brief Calcula la puntuación total de la solución.
     *
     * \details Recorre todas las rutas creadas sumando la puntuación de cada nodo visitado
     * (los depósitos tienen puntuación 0).
     *
     * @return suma de puntuaciones de los POI visitados.
     */
    public double evaluateFitness() {
        double objectiveFunction = 0.0;
        for (int k = 0; k < this.getCreatedRoutes(); k++) {
            int depot = this.getIndexRoute(k);
            int pre = depot;
            int suc;
            do {
                suc = this.getSuccessor(pre);
                objectiveFunction += this.problem.getScore(suc);
                pre = suc;
            } while (suc != depot);
        }
        return objectiveFunction;
    }

    /**
     * \brief Abre una nueva ruta vacía con un depósito virtual propio.
     *
     * \details El nuevo depósito recibe el índice siguiente al último depósito virtual
     * creado (o POIs + 1 si es el primero) y se enlaza consigo mismo.
     *
     * @return índice del depósito de la nueva ruta.
     */
    public int addRoute() {
        int depot = this.problem.getPOIs() + 1;
        int routePos = 1;
        for (int i = 0; i < this.routes.length; i++) {
            if (this.routes[i] != NO_INITIALIZED && this.routes[i] != 0) {
                depot = this.routes[i] + 1;
                routePos = i + 1;
            }
        }
        this.routes[routePos] = depot;
        this.availableVehicles--;
        this.predecessors[depot] = depot;
        this.successors[depot] = depot;
        this.problem.addNodeDepot();
        return depot;
    }

    /**
     * \brief Escribe las rutas de la solución y su puntuación.
     * @param out flujo de salida.
     * @return puntuación de la solución.
     */
    public double printSolution(PrintStream out) {
        for (int k = 0; k < this.getCreatedRoutes(); k++) {
            int depot = this.getIndexRoute(k);
            int pre = depot;
            int suc;
            StringBuilder line = new StringBuilder();
            do {
                suc = this.getSuccessor(pre);
                line.append(pre).append(" - ");
                pre = suc;
            } while (suc != depot);
            out.println(line.append(suc).append("  "));
        }
        double fitness = this.evaluateFitness();
        out.println("SC=" + fitness);
        return fitness;
    }
}