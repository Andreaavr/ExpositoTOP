package top;

/**
 * \brief Evaluador de la función objetivo del TOPTW.
 *
 * \details La función objetivo del TOPTW es maximizar la suma de las puntuaciones de los
 * puntos de interés visitados por todas las rutas. El evaluador calcula ese valor y lo
 * almacena en la propia solución.
 */
public class TOPTWEvaluator {
    /** \brief Valor que indica que una solución todavía no ha sido evaluada. */
    public static final double NO_EVALUATED = -1.0;

    /**
     * \brief Evalúa una solución y guarda el resultado en ella.
     * @param solution solución a evaluar.
     * @return valor de la función objetivo (puntuación total).
     */
    public double evaluate(TOPTWSolution solution) {
        double value = solution.evaluateFitness();
        solution.setObjectiveFunctionValue(value);
        return value;
    }
}