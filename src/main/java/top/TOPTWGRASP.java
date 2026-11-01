package top;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * \brief Metaheurística GRASP (Greedy Randomized Adaptive Search Procedure) para el TOPTW.
 *
 * \details Cada iteración construye una solución de forma voraz y aleatorizada:
 *
 * 1. Se evalúa, para cada POI pendiente, la mejor posición de inserción factible
 *    (respetando ventanas temporales y la duración máxima de ruta) en las rutas abiertas.
 * 2. Los candidatos se ordenan por coste temporal de inserción y los mejores
 *    (hasta maxSizeRCL) forman la lista restringida de candidatos (RCL).
 * 3. Se elige un candidato de la RCL según la estrategia configurada
 *    (aleatoria, borrosa con el mejor grado o borrosa con corte alfa) y se inserta.
 * 4. Si no queda ningún candidato factible se abre una nueva ruta, mientras haya vehículos.
 *
 * El método grasp() repite la construcción el número de iteraciones indicado y registra la
 * puntuación media y la mejor obtenida.
 *
 * Pseudocódigo de referencia:
 * \code
 * procedure GRASP(MaxIterations, Seed)
 *   for k = 1..MaxIterations do
 *     Solution = GreedyRandomizedConstruction(Seed)
 *     Solution = LocalSearch(Solution)         // pendiente de implementar
 *     UpdateSolution(Solution, BestSolution)
 *   return BestSolution
 * \endcode
 */
public class TOPTWGRASP {

    /** \brief Estrategias de selección de un candidato dentro de la RCL. */
    public enum SelectionStrategy {
        /** \brief Selección uniforme al azar. */
        RANDOM,
        /** \brief Selección del candidato con mejor grado de pertenencia borroso. */
        FUZZY_BEST,
        /** \brief Selección al azar entre los candidatos que superan un corte alfa borroso. */
        FUZZY_ALPHA_CUT
    }

    /** \brief Posición del cliente en el vector de información de un candidato. */
    static final int CUSTOMER = 0;
    /** \brief Posición de la ruta en el vector de información de un candidato. */
    static final int ROUTE = 1;
    /** \brief Posición del predecesor en el vector de información de un candidato. */
    static final int PREDECESSOR = 2;
    /** \brief Posición del coste temporal en el vector de información de un candidato. */
    static final int COST = 3;
    /** \brief Posición de la puntuación en el vector de información de un candidato. */
    static final int SCORE = 4;
    /** \brief Tamaño del vector de información de un candidato. */
    private static final int CANDIDATE_INFO_SIZE = 5;
    /** \brief Valor alfa por defecto para el corte borroso. */
    public static final double DEFAULT_ALPHA = 0.8;

    /** \brief Solución sobre la que se construye. */
    private TOPTWSolution solution;
    /** \brief Tiempo de cómputo de la solución (reservado). */
    private int solutionTime;
    /** \brief Generador de números aleatorios (reutilizado en todas las selecciones). */
    private final Random random;
    /** \brief Estrategia de selección dentro de la RCL. */
    private SelectionStrategy strategy = SelectionStrategy.FUZZY_ALPHA_CUT;
    /** \brief Valor alfa para la estrategia FUZZY_ALPHA_CUT. */
    private double alpha = DEFAULT_ALPHA;
    /** \brief Flujo donde se escriben los informes; null para no escribir nada. */
    private PrintStream out;
    /** \brief Puntuación media de la última ejecución de grasp(). */
    private double lastAverageFitness;
    /** \brief Mejor puntuación de la última ejecución de grasp(). */
    private double lastBestFitness;

    /**
     * \brief Crea el GRASP con un generador aleatorio no determinista y salida silenciosa.
     * @param sol solución de trabajo.
     */
    public TOPTWGRASP(TOPTWSolution sol) {
        this(sol, new Random(), null);
    }

    /**
     * \brief Crea el GRASP con un generador y un flujo de salida concretos.
     *
     * \details Permite ejecuciones reproducibles (pasando un Random con semilla) y
     * desacopla el algoritmo de la consola.
     *
     * @param sol    solución de trabajo.
     * @param random generador de números aleatorios.
     * @param out    flujo para los informes de cada iteración, o null para no escribir.
     */
    public TOPTWGRASP(TOPTWSolution sol, Random random, PrintStream out) {
        this.solution = sol;
        this.solutionTime = 0;
        this.random = random;
        this.out = out;
    }

    /**
     * \brief Ejecuta el GRASP.
     *
     * \details Construye maxIterations soluciones voraces aleatorizadas, escribe el
     * informe de cada una (si hay flujo de salida) y al final la media y la mejor
     * puntuación. Ambos valores quedan disponibles en getLastAverageFitness() y
     * getLastBestFitness().
     *
     * @param maxIterations número de construcciones.
     * @param maxSizeRCL    tamaño máximo de la lista restringida de candidatos.
     * @return mejor puntuación obtenida.
     */
    public double grasp(int maxIterations, int maxSizeRCL) {
        double averageFitness = 0.0;
        double bestSolution = 0.0;
        for (int i = 0; i < maxIterations; i++) {
            this.computeGreedySolution(maxSizeRCL);
            double fitness = this.solution.evaluateFitness();
            this.print(this.solution.getInfoSolution());
            averageFitness += fitness;
            if (bestSolution < fitness) {
                bestSolution = fitness;
            }
            // Punto de extensión: búsqueda local sobre this.solution.
        }
        averageFitness = averageFitness / maxIterations;
        this.print(" --> MEDIA: " + averageFitness);
        this.print(" --> MEJOR SOLUCION: " + bestSolution);
        this.lastAverageFitness = averageFitness;
        this.lastBestFitness = bestSolution;
        return bestSolution;
    }

    /**
     * \brief Escribe una línea en el flujo de salida, si existe.
     * @param text texto a escribir.
     */
    private void print(String text) {
        if (this.out != null) {
            this.out.println(text);
        }
    }

    /**
     * \brief Selecciona uniformemente una posición de la RCL.
     * @param maxTRCL tamaño de la RCL.
     * @return posición seleccionada en [0, maxTRCL).
     */
    public int aleatorySelectionRCL(int maxTRCL) {
        return this.random.nextInt(maxTRCL);
    }

    /**
     * \brief Grado de pertenencia borroso de un candidato.
     * \details Vale 1 - puntuación/puntuación máxima: cuanto menor, más interesante el POI.
     * @param candidate vector de información del candidato.
     * @param maxScore  puntuación máxima de la instancia.
     * @return grado de pertenencia.
     */
    private static double membership(double[] candidate, double maxScore) {
        return 1 - (candidate[SCORE] / maxScore);
    }

    /**
     * \brief Selección borrosa: candidato con menor grado de pertenencia (mayor puntuación).
     * @param rcl lista restringida de candidatos.
     * @return posición seleccionada, o -1 si la lista está vacía.
     */
    public int fuzzySelectionBestFDRCL(List<double[]> rcl) {
        double maxSc = this.getMaxScore();
        double minMemFunc = Double.MAX_VALUE;
        int posSelected = -1;
        for (int i = 0; i < rcl.size(); i++) {
            double value = membership(rcl.get(i), maxSc);
            if (minMemFunc > value) {
                minMemFunc = value;
                posSelected = i;
            }
        }
        return posSelected;
    }

    /**
     * \brief Selección borrosa con corte alfa.
     *
     * \details Elige al azar entre los candidatos cuyo grado de pertenencia es menor o
     * igual que alpha; si no hay ninguno, elige al azar entre toda la RCL.
     *
     * @param rcl   lista restringida de candidatos.
     * @param alpha umbral del corte.
     * @return posición seleccionada.
     */
    public int fuzzySelectionAlphaCutRCL(List<double[]> rcl, double alpha) {
        List<Integer> rclPos = new ArrayList<>();
        double maxSc = this.getMaxScore();
        for (int j = 0; j < rcl.size(); j++) {
            if (membership(rcl.get(j), maxSc) <= alpha) {
                rclPos.add(j);
            }
        }
        if (!rclPos.isEmpty()) {
            return rclPos.get(this.aleatorySelectionRCL(rclPos.size()));
        }
        return this.aleatorySelectionRCL(rcl.size());
    }

    /**
     * \brief Aplica la estrategia de selección configurada.
     * @param rcl lista restringida de candidatos.
     * @return posición seleccionada.
     */
    private int select(List<double[]> rcl) {
        switch (this.strategy) {
            case FUZZY_BEST:
                return this.fuzzySelectionBestFDRCL(rcl);
            case FUZZY_ALPHA_CUT:
                return this.fuzzySelectionAlphaCutRCL(rcl, this.alpha);
            case RANDOM:
            default:
                return this.aleatorySelectionRCL(rcl.size());
        }
    }

    /**
     * \brief Crea un vector de tiempos de salida inicializado a cero.
     * @return lista con un tiempo por nodo (POI y depósitos).
     */
    private List<Double> newDepartureTimes() {
        int size = this.solution.getProblem().getPOIs() + this.solution.getProblem().getVehicles();
        List<Double> times = new ArrayList<>(size);
        for (int z = 0; z < size; z++) {
            times.add(0.0);
        }
        return times;
    }

    /**
     * \brief Evalúa los candidatos y los ordena por coste de inserción creciente.
     * @param customers      POI pendientes.
     * @param departureTimes tiempos de salida por ruta y nodo.
     * @return candidatos ordenados.
     */
    private List<double[]> sortedCandidates(List<Integer> customers, List<List<Double>> departureTimes) {
        List<double[]> candidates = this.comprehensiveEvaluation(customers, departureTimes);
        candidates.sort(Comparator.comparingDouble(a -> a[COST]));
        return candidates;
    }

    /**
     * \brief Construye una solución voraz aleatorizada (fase constructiva del GRASP).
     * @param maxSizeRCL tamaño máximo de la lista restringida de candidatos.
     */
    public void computeGreedySolution(int maxSizeRCL) {
        this.solution.initSolution();

        List<List<Double>> departureTimesPerClient = new ArrayList<>();
        departureTimesPerClient.add(this.newDepartureTimes());

        List<Integer> customers = new ArrayList<>();
        for (int j = 1; j <= this.solution.getProblem().getPOIs(); j++) {
            customers.add(j);
        }

        List<double[]> candidates = this.sortedCandidates(customers, departureTimesPerClient);
        boolean existCandidates = true;

        while (!customers.isEmpty() && existCandidates) {
            if (!candidates.isEmpty()) {
                int maxTRCL = Math.min(maxSizeRCL, candidates.size());
                List<double[]> rcl = new ArrayList<>(candidates.subList(0, maxTRCL));
                double[] candidateSelected = rcl.get(this.select(rcl));
                customers.remove(Integer.valueOf((int) candidateSelected[CUSTOMER]));
                this.updateSolution(candidateSelected, departureTimesPerClient);
            } else if (this.solution.getCreatedRoutes() < this.solution.getProblem().getVehicles()) {
                // No hay candidatos factibles: se abre otra ruta.
                this.solution.addRoute();
                departureTimesPerClient.add(this.newDepartureTimes());
            } else {
                existCandidates = false;
            }
            candidates = this.sortedCandidates(customers, departureTimesPerClient);
        }
    }

    /**
     * \brief Inserta un candidato en la solución y propaga los tiempos de salida.
     *
     * \details Enlaza el cliente tras su predecesor y recalcula los tiempos de salida de
     * todos los nodos posteriores de la ruta hasta volver al depósito.
     *
     * @param candidateSelected vector {cliente, ruta, predecesor, coste, puntuación}.
     * @param departureTimes    tiempos de salida por ruta y nodo (se actualizan).
     */
    public void updateSolution(double[] candidateSelected, List<List<Double>> departureTimes) {
        int customer = (int) candidateSelected[CUSTOMER];
        int routeIndex = (int) candidateSelected[ROUTE];
        int predecessor = (int) candidateSelected[PREDECESSOR];
        TOPTW problem = this.solution.getProblem();

        this.solution.setPredecessor(customer, predecessor);
        this.solution.setSuccessor(customer, this.solution.getSuccessor(predecessor));
        this.solution.setSuccessor(predecessor, customer);
        this.solution.setPredecessor(this.solution.getSuccessor(customer), customer);

        List<Double> route = departureTimes.get(routeIndex);
        double time = route.get(predecessor);
        int pre = predecessor;
        int suc;
        int depot = this.solution.getIndexRoute(routeIndex);
        do {
            suc = this.solution.getSuccessor(pre);
            time += this.solution.getDistance(pre, suc);
            time = Math.max(time, problem.getReadyTime(suc));
            time += problem.getServiceTime(suc);
            if (!this.solution.isDepot(suc)) {
                route.set(suc, time);
            }
            pre = suc;
        } while (suc != depot);
    }

    /**
     * \brief Tiempo de salida de un nodo dado su instante de llegada.
     *
     * \details Si se llega antes de la apertura de la ventana se espera hasta ella; después
     * se suma la duración de la visita. Si la llegada no es estrictamente anterior al
     * cierre de la ventana, la visita no es factible.
     *
     * @param arrival instante de llegada.
     * @param node    nodo visitado.
     * @return instante de salida, o NaN si se incumple la ventana temporal.
     */
    private double departureTime(double arrival, int node) {
        TOPTW problem = this.solution.getProblem();
        if (arrival >= problem.getDueTime(node)) {
            return Double.NaN;
        }
        return Math.max(arrival, problem.getReadyTime(node)) + problem.getServiceTime(node);
    }

    /**
     * \brief Coste de insertar un candidato justo después de un nodo de una ruta.
     *
     * \details Comprueba la ventana temporal del candidato, la del sucesor y la de todos
     * los nodos siguientes hasta volver al depósito, así como la duración máxima de ruta.
     *
     * @param routeIndex     ruta donde se inserta.
     * @param pre            nodo tras el que se inserta.
     * @param candidate      POI candidato.
     * @param departureTimes tiempos de salida por ruta y nodo.
     * @return tiempo de llegada al depósito tras la inserción, o NaN si no es factible.
     */
    private double insertionCost(int routeIndex, int pre, int candidate, List<List<Double>> departureTimes) {
        double maxTime = this.solution.getProblem().getMaxTimePerRoute();
        int depot = this.solution.getIndexRoute(routeIndex);
        int suc = this.solution.getSuccessor(pre);

        double costCand = this.departureTime(
                departureTimes.get(routeIndex).get(pre) + this.solution.getDistance(pre, candidate), candidate);
        if (Double.isNaN(costCand) || costCand > maxTime) {
            return Double.NaN;
        }
        double cost = this.departureTime(costCand + this.solution.getDistance(candidate, suc), suc);
        int current = suc;
        while (!Double.isNaN(cost) && cost <= maxTime && current != depot) {
            int next = this.solution.getSuccessor(current);
            cost = this.departureTime(cost + this.solution.getDistance(current, next), next);
            current = next;
        }
        return (Double.isNaN(cost) || cost > maxTime) ? Double.NaN : cost;
    }

    /**
     * \brief Evalúa la mejor inserción factible de cada POI pendiente.
     *
     * \details Para cada cliente recorre todas las posiciones de todas las rutas creadas y
     * se queda con la de menor coste temporal.
     *
     * @param customers      POI pendientes de insertar.
     * @param departureTimes tiempos de salida por ruta y nodo.
     * @return lista de vectores {cliente, ruta, predecesor, coste, puntuación}, uno por
     *         cada cliente con al menos una inserción factible.
     */
    public List<double[]> comprehensiveEvaluation(List<Integer> customers, List<List<Double>> departureTimes) {
        List<double[]> candidatesList = new ArrayList<>();
        for (int candidate : customers) {
            double[] best = null;
            for (int k = 0; k < this.solution.getCreatedRoutes(); k++) {
                int depot = this.solution.getIndexRoute(k);
                int pre = depot;
                do {
                    double cost = this.insertionCost(k, pre, candidate, departureTimes);
                    if (!Double.isNaN(cost) && (best == null || cost < best[COST])) {
                        best = new double[CANDIDATE_INFO_SIZE];
                        best[CUSTOMER] = candidate;
                        best[ROUTE] = k;
                        best[PREDECESSOR] = pre;
                        best[COST] = cost;
                        best[SCORE] = this.solution.getProblem().getScore(candidate);
                    }
                    pre = this.solution.getSuccessor(pre);
                } while (pre != depot);
            }
            if (best != null) {
                candidatesList.add(best);
            }
        }
        return candidatesList;
    }

    /**
     * \brief Solución de trabajo.
     * @return solución actual.
     */
    public TOPTWSolution getSolution() {
        return solution;
    }

    /**
     * \brief Sustituye la solución de trabajo.
     * @param solution nueva solución.
     */
    public void setSolution(TOPTWSolution solution) {
        this.solution = solution;
    }

    /**
     * \brief Tiempo de cómputo registrado.
     * @return tiempo de solución.
     */
    public int getSolutionTime() {
        return solutionTime;
    }

    /**
     * \brief Registra el tiempo de cómputo.
     * @param solutionTime tiempo de solución.
     */
    public void setSolutionTime(int solutionTime) {
        this.solutionTime = solutionTime;
    }

    /**
     * \brief Estrategia de selección actual.
     * @return estrategia.
     */
    public SelectionStrategy getStrategy() {
        return strategy;
    }

    /**
     * \brief Cambia la estrategia de selección dentro de la RCL.
     * @param strategy nueva estrategia.
     */
    public void setStrategy(SelectionStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * \brief Valor alfa del corte borroso.
     * @return alfa.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * \brief Cambia el valor alfa del corte borroso.
     * @param alpha nuevo valor en [0, 1].
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * \brief Cambia el flujo de salida de los informes.
     * @param out flujo, o null para no escribir.
     */
    public void setOutput(PrintStream out) {
        this.out = out;
    }

    /**
     * \brief Puntuación media de la última ejecución.
     * @return media de las puntuaciones.
     */
    public double getLastAverageFitness() {
        return lastAverageFitness;
    }

    /**
     * \brief Mejor puntuación de la última ejecución.
     * @return mejor puntuación.
     */
    public double getLastBestFitness() {
        return lastBestFitness;
    }

    /**
     * \brief Puntuación máxima entre todos los nodos de la instancia.
     * @return puntuación máxima.
     */
    public double getMaxScore() {
        double maxSc = -1.0;
        for (double value : this.solution.getProblem().getScore()) {
            maxSc = Math.max(maxSc, value);
        }
        return maxSc;
    }
}
