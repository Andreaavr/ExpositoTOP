package es.ull.esit.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * \brief Algoritmo de Bellman-Ford sobre una matriz de distancias.
 *
 * \details Calcula los caminos mínimos desde el nodo 0 a todos los demás. Los arcos se
 * obtienen de la matriz: Integer.MAX_VALUE significa que no hay arco. Admite pesos
 * negativos, por lo que con los pesos cambiados de signo sirve para obtener el camino
 * máximo en un grafo acíclico (de ahí que getValue() devuelva la distancia cambiada de signo).
 * Tras solve(), la lista path contiene el camino desde el último nodo hasta el 0 en
 * orden inverso.
 */
public class BellmanFord {

    /** \brief Valor usado como distancia infinita. */
    private static final int INFINITY = 999999;
    /** \brief Matriz de distancias (Integer.MAX_VALUE = sin arco). */
    private final int[][] distanceMatrix;
    /** \brief Nodos origen de cada arco. */
    private List<Integer> edges1 = null;
    /** \brief Nodos destino de cada arco. */
    private List<Integer> edges2 = null;
    /** \brief Número de nodos. */
    private final int nodes;
    /** \brief Camino encontrado hasta el último nodo (en orden inverso). */
    private final List<Integer> path;
    /** \brief Distancias mínimas desde el nodo 0. */
    private int[] distances = null;
    /** \brief Valor del camino hasta el último nodo, cambiado de signo. */
    private int value;

    /**
     * \brief Prepara el algoritmo.
     * @param distanceMatrix matriz de distancias (Integer.MAX_VALUE = sin arco).
     * @param nodes          número de nodos.
     * @param path           lista donde se escribirá el camino resultante.
     */
    public BellmanFord(int[][] distanceMatrix, int nodes, List<Integer> path) {
        this.distanceMatrix = distanceMatrix;
        this.nodes = nodes;
        this.path = path;
        this.calculateEdges();
        this.value = INFINITY;
    }

    /**
     * \brief Construye la lista de arcos a partir de la matriz.
     */
    private void calculateEdges() {
        this.edges1 = new ArrayList<>();
        this.edges2 = new ArrayList<>();
        for (int i = 0; i < this.nodes; i++) {
            for (int j = 0; j < this.nodes; j++) {
                if (this.distanceMatrix[i][j] != Integer.MAX_VALUE) {
                    this.edges1.add(i);
                    this.edges2.add(j);
                }
            }
        }
    }

    /**
     * \brief Distancias mínimas desde el nodo 0.
     * @return copia del vector de distancias, o null si no se ha ejecutado solve().
     */
    public int[] getDistances() {
        return this.distances == null ? null : this.distances.clone();
    }

    /**
     * \brief Valor del camino hasta el último nodo, cambiado de signo.
     * @return -distancia(0, n-1).
     */
    public int getValue() {
        return this.value;
    }

    /**
     * \brief Ejecuta Bellman-Ford y reconstruye el camino hasta el último nodo.
     */
    public void solve() {
        int numEdges = this.edges1.size();
        int[] predecessor = new int[this.nodes];
        this.distances = new int[this.nodes];
        for (int i = 0; i < this.nodes; i++) {
            this.distances[i] = INFINITY;
            predecessor[i] = -1;
        }
        this.distances[0] = 0;
        for (int i = 0; i < (this.nodes - 1); i++) {
            for (int j = 0; j < numEdges; j++) {
                int u = this.edges1.get(j);
                int v = this.edges2.get(j);
                if (this.distances[v] > this.distances[u] + this.distanceMatrix[u][v]) {
                    this.distances[v] = this.distances[u] + this.distanceMatrix[u][v];
                    predecessor[v] = u;
                }
            }
        }
        this.path.add(this.nodes - 1);
        int pred = predecessor[this.nodes - 1];
        while (pred != -1) {
            this.path.add(pred);
            pred = predecessor[pred];
        }
        this.value = -this.distances[this.nodes - 1];
    }
}