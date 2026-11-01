package top;

/**
 * \brief Descriptor ligero de una ruta: identificador y nodos extremos.
 *
 * \details Estructura de datos auxiliar que agrupa el identificador de una ruta junto con
 * un predecesor y un sucesor de referencia.
 */
public class TOPTWRoute {
    /** \brief Nodo predecesor de referencia. */
    private int predecessor;
    /** \brief Nodo sucesor de referencia. */
    private int successor;
    /** \brief Identificador de la ruta. */
    private int id;

    /**
     * \brief Crea una ruta sin datos.
     */
    TOPTWRoute() {
        this(TOPTWSolution.NO_INITIALIZED, TOPTWSolution.NO_INITIALIZED, TOPTWSolution.NO_INITIALIZED);
    }

    /**
     * \brief Crea una ruta con sus datos.
     * @param pre  predecesor.
     * @param succ sucesor.
     * @param id   identificador.
     */
    TOPTWRoute(int pre, int succ, int id) {
        this.predecessor = pre;
        this.successor = succ;
        this.id = id;
    }

    /**
     * \brief Predecesor de referencia.
     * @return predecesor.
     */
    public int getPredecessor() {
        return this.predecessor;
    }

    /**
     * \brief Sucesor de referencia.
     * @return sucesor.
     */
    public int getSuccessor() {
        return this.successor;
    }

    /**
     * \brief Identificador de la ruta.
     * @return identificador.
     */
    public int getId() {
        return this.id;
    }

    /**
     * \brief Establece el predecesor.
     * @param pre predecesor.
     */
    public void setPredecessor(int pre) {
        this.predecessor = pre;
    }

    /**
     * \brief Establece el sucesor.
     * @param suc sucesor.
     */
    public void setSuccessor(int suc) {
        this.successor = suc;
    }

    /**
     * \brief Establece el identificador.
     * @param id identificador.
     */
    public void setId(int id) {
        this.id = id;
    }
}
