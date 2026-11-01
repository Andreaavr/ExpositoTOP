package es.ull.esit.utilities;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

// Calcula todos los subconjuntos de un conjunto dado.
// Cada llamada a iterator() devuelve un iterador nuevo, así que puede recorrerse varias veces.
public class PowerSet<E> implements Iterable<Set<E>> {

    private final Object[] arr;

    public PowerSet(Set<E> set) {
        this.arr = set.toArray();
    }

    @Override
    public Iterator<Set<E>> iterator() {
        return new PowerSetIterator();
    }

    private final class PowerSetIterator implements Iterator<Set<E>> {
        private final BitSet bset = new BitSet(arr.length + 1);

        @Override
        public boolean hasNext() {
            return !this.bset.get(arr.length);
        }

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