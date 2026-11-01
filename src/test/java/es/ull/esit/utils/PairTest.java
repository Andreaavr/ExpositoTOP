package es.ull.esit.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class PairTest {
    @Test
    public void valueSemantics() {
        Pair<String, Integer> a = Pair.create("x", 1);
        Pair<String, Integer> b = new Pair<>("x", 1);
        assertEquals("x", a.first());
        assertEquals(Integer.valueOf(1), a.second());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, Pair.create("x", 2));
        assertEquals(Pair.create(null, null), new Pair<>(null, null));
        assertEquals("Pair[first=x, second=1]", a.toString());
    }
}
