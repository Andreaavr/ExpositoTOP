package top;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TOPTWTest {
    private static final double EPS = 1e-9;
    private TOPTW p;

    @Before
    public void setUp() throws IOException {
        p = TestSupport.mini();
    }

    @Test
    public void distanceMatrixIsEuclideanAndSymmetric() {
        assertEquals(10.0, p.getDistance(0, 1), EPS);
        assertEquals(Math.sqrt(200), p.getDistance(1, 3), EPS);
        assertEquals(p.getDistance(2, 3), p.getDistance(3, 2), EPS);
        assertEquals(0.0, p.getDistance(2, 2), EPS);
        assertEquals(p.getDistance(1, 2), p.getTime(1, 2), EPS);
    }

    @Test
    public void virtualDepotsMapToRealDepot() {
        assertFalse(p.isDepot(4));
        assertTrue(p.isDepot(5));
        assertEquals(p.getDistance(0, 1), p.getDistance(5, 1), EPS);
        assertEquals(p.getX(0), p.getX(6), EPS);
        assertEquals(p.getDueTime(0), p.getDueTime(5), EPS);
        assertEquals(0.0, p.getScore(5), EPS);
    }

    @Test
    public void routeDistances() {
        assertEquals(20.0, p.getDistance(new int[]{0, 1, 0}), EPS);
        List<Integer> r1 = Arrays.asList(0, 2, 0);
        List<Integer> r2 = Arrays.asList(0, 1, 0);
        assertEquals(40.0, p.getDistance(r1), EPS);
        assertEquals(60.0, p.getTotalDistance(Arrays.asList(r1, r2)), EPS);
        assertEquals(0.0, p.getDistance(Collections.singletonList(3)), EPS);
    }

    @Test
    public void getScoreReturnsDefensiveCopy() {
        double[] scores = p.getScore();
        scores[1] = 1000;
        assertEquals(10.0, p.getScore(1), EPS);
    }

    @Test
    public void countersAndToString() {
        assertEquals(1, p.addNodeDepot());
        assertEquals(1, p.getDepots());
        String text = p.toString();
        assertTrue(text.startsWith("Nodes: 4"));
        assertTrue(text.contains("Vehicles: 2"));
        assertEquals(5, p.addNode());
        p.setNodes(4);
        assertEquals(4, p.getNodes());
        p.setMaxRoutes(3);
        assertEquals(3.0, p.getMaxRoutes(), EPS);
    }
}
