package es.ull.esit.utilities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class BellmanFordTest {
    private static final int X = Integer.MAX_VALUE;

    @Test
    public void shortestPathWithNegativeWeights() {
        // 0 -> 1 (4), 0 -> 2 (1), 2 -> 1 (-2), 1 -> 3 (1)
        int[][] m = {{X, 4, 1, X}, {X, X, X, 1}, {X, -2, X, X}, {X, X, X, X}};
        List<Integer> path = new ArrayList<>();
        BellmanFord bf = new BellmanFord(m, 4, path);
        assertNull(bf.getDistances());
        bf.solve();
        assertArrayEquals(new int[]{0, -1, 1, 0}, bf.getDistances());
        assertEquals(0, bf.getValue());
        assertEquals(Arrays.asList(3, 1, 2, 0), path);
    }

    @Test
    public void longestPathByNegatingWeights() {
        // Camino máximo 0 -> 1 -> 2 (3 + 4 = 7) frente a 0 -> 2 (5).
        int[][] m = {{X, -3, -5}, {X, X, -4}, {X, X, X}};
        BellmanFord bf = new BellmanFord(m, 3, new ArrayList<>());
        bf.solve();
        assertEquals(7, bf.getValue());
    }
}
