package es.ull.esit.utilities;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * \brief Conjunto potencia (todos los subconjuntos) de un conjunto, recorrible con for-each.
 *
 * \details Cada llamada a iterator() devuelve un iterador nuevo e independiente, por lo
 * que el conjunto potencia puede recorrerse tantas veces como se quiera. El iterador usa
 * un BitSet como contador binario: el bit i indica si el elemento i pertenece al
 * subconjunto actual. Genera 2^n subconjuntos empezando por el vacío. Los elementos
 * deben ser comparables, ya que cada subconjunto se devuelve como TreeSet.
 *
 * @param <E> tipo de los elementos.
 */
public class PowerSet<E> implements Iterable<Set<E>> {

    /** \brief Elementos del conjunto original. */
    private final Object[] arr;

    /**
     * \brief Crea el conjunto potencia de un conjunto.
     * @param set conjunto original.
     */
    public PowerSet(Set<E> set) {
        this.arr = set.toArray();
    }

    /**
     * \brief Devuelve un iterador nuevo sobre los 2^n subconjuntos.
     * @return iterador independiente de los anteriores.
     */
    @Override
    public Iterator<Set<E>> iterator() {
        return new PowerSetIterator();
    }

    /**
     * \brief Iterador sobre los subconjuntos, basado en un contador binario.
     */
    private final class PowerSetIterator implements Iterator<Set<E>> {
        /** \brief Contador binario del subconjunto actual; el bit n marca el final. */
        private final BitSet bset = new BitSet(arr.length + 1);

        /**
         * \brief Indica si quedan subconjuntos.
         * @return true mientras no se hayan generado los 2^n subconjuntos.
         */
        @Override
        public boolean hasNext() {
            return !this.bset.get(arr.length);
        }

        /**
         * \brief Devuelve el siguiente subconjunto.
         * @return subconjunto actual.
         * @throws NoSuchElementException si ya se han generado todos.
         */
        @Override
        @SuppressWarnings("unchecked")
        public Set<E> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            Set<E> returnSet = new TreeSet<>();
            for (int i = 0; i < arr.length; i++) {
                if (this.bset.get(i)) {
                    returnSet.add((E) arr[i]);
                }
            }
            for (int i = 0; i < this.bset.size(); i++) {
                if (!this.bset.get(i)) {
                    this.bset.set(i);
                    break;
                } else {
                    this.bset.clear(i);
                }
            }
            return returnSet;
        }
    }
}