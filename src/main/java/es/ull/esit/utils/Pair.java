package es.ull.esit.utils;

/**
 * \brief Par inmutable de valores.
 *
 * \details Se declara como record: Java genera automáticamente el constructor, los
 * accesores first() y second(), equals(), hashCode() y toString().
 *
 * @param <F>    tipo del primer elemento.
 * @param <S>    tipo del segundo elemento.
 * @param first  primer elemento.
 * @param second segundo elemento.
 */
public record Pair<F, S>(F first, S second) {

    /**
     * \brief Método factoría.
     * @param <A> tipo del primer elemento.
     * @param <B> tipo del segundo elemento.
     * @param a   primer elemento.
     * @param b   segundo elemento.
     * @return nuevo par.
     */
    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<>(a, b);
    }
}