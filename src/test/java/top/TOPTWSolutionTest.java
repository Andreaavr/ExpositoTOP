package top;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

public class TOPTWSolutionTest {
    private static final double EPS = 1e-9;
    private TOPTWSolution s;

    @Before
    public void setUp() throws IOException {
        s = new TOPTWSolution(TestSupport.mini());
        s.initSolution();
    }

    /** Inserta el POI c justo después de pre. */
    private void insertAfter(int pre, int c) {
        int next = s.getSuccessor(pre);
        s.setPredecessor(c, pre);
        s.setSuccessor(c, next);
        s.setSuccessor(pre, c);
        s.setPredecessor(next, c);
    }

    @Test
    public void initialSolutionHasOneEmptyRoute() {
        assertEquals(1, s.getCreatedRoutes());
        assertEquals(1, s.getAvailableVehicles());
        assertEquals(0, s.getSuccessor(0));
        assertTrue(s.isDepot(0));
        assertEquals(0.0, s.evaluateFitness(), EPS);
        assertEquals(TOPTWEvaluator.NO_EVALUATED, s.getObjectiveFunctionValue(), EPS);
    }

    @Test
    public void addRouteCreatesVirtualDepot() {
        int depot = s.addRoute();
        assertEquals(5, depot);
        assertEquals(2, s.getCreatedRoutes());
        assertEquals(0, s.getAvailableVehicles());
        assertTrue(s.isDepot(5));
        assertEquals(5, s.getSuccessor(5));
        assertEquals(5, s.getIndexRoute(1));
    }

    @Test
    public void fitnessSumsVisitedScores() {
        insertAfter(0, 1);
        insertAfter(1, 2);
        int depot = s.addRoute();
        insertAfter(depot, 3);
        assertEquals(60.0, s.evaluateFitness(), EPS);
        assertEquals(60.0, new TOPTWEvaluator().evaluate(s), EPS);
        assertEquals(60.0, s.getObjectiveFunctionValue(), EPS);
        assertEquals(60.0, TestSupport.assertFeasible(s), EPS);

        String info = s.getInfoSolution();
        assertTrue(info.contains("0 - 1 - 2 - 0"));
        assertTrue(info.contains("5 - 3 - 5"));
        assertTrue(info.contains("FEASIBLE SOLUTION: true"));
        assertTrue(info.contains("SCORE: 60.0"));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertEquals(60.0, s.printSolution(new PrintStream(bytes, true)), EPS);
        assertTrue(new String(bytes.toByteArray(), StandardCharsets.UTF_8).contains("SC=60.0"));
    }

    @Test
    public void infeasibleRouteIsReported() {
        insertAfter(0, 4); // POI 4 está a 70.7 y su ventana cierra en 20
        String info = s.getInfoSolution();
        assertTrue(info.contains("FEASIBLE SOLUTION: false"));
    }

    @Test
    public void equalsAndHashCodeUsePredecessors() {
        TOPTWSolution other = new TOPTWSolution(s.getProblem());
        other.initSolution();
        assertEquals(s, other);
        assertEquals(s.hashCode(), other.hashCode());
        insertAfter(0, 1);
        assertNotEquals(s, other);
        assertNotEquals(s, null);
        assertNotEquals(s, "texto");
    }

    @Test
    public void accessorsReturnCopies() {
        int[] pred = s.getPredecessors();
        pred[0] = 42;
        assertEquals(0, s.getPredecessor(0));
        int[] succ = s.getSuccessors();
        succ[0] = 42;
        assertEquals(0, s.getSuccessor(0));
        s.setWaitingTime(1, 2.5);
        assertEquals(2.5, s.getWaitingTime(1), EPS);
        s.setPositionInRoute(1, 3);
        assertEquals(3, s.getPositionInRoute(1));
        s.setAvailableVehicles(0);
        assertEquals(2, s.getCreatedRoutes());
        assertArrayEquals(new int[]{0, -1, -1, -1, -1, -1}, s.getPredecessors());
    }
}