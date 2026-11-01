package es.ull.esit.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class PowerSetTest {
    @Test
    public void generatesAllSubsets() {
        PowerSet<Integer> ps = new PowerSet<>(new TreeSet<>(Arrays.asList(1, 2, 3)));
        Set<Set<Integer>> subsets = new HashSet<>();
        for (Set<Integer> s : ps) {
            subsets.add(s);
        }
        assertEquals(8, subsets.size());
        assertTrue(subsets.contains(new HashSet<>()));
        assertTrue(subsets.contains(new HashSet<>(Arrays.asList(1, 2, 3))));
    }

    @Test
    public void supportsMultipleTraversals() {
        PowerSet<Integer> ps = new PowerSet<>(new TreeSet<>(Arrays.asList(1, 2)));
        int first = 0;
        for (Set<Integer> s : ps) {
            first++;
        }
        int second = 0;
        for (Set<Integer> s : ps) {
            second++;
        }
        assertEquals(4, first);
        assertEquals(4, second);
        assertNotSame(ps.iterator(), ps.iterator());
    }

    @Test
    public void iteratorContract() {
        Iterator<Set<String>> it = new PowerSet<>(new TreeSet<String>()).iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isEmpty());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
        assertThrows(UnsupportedOperationException.class, it::remove);
    }
}
