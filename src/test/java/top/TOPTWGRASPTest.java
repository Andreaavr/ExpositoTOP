package top;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class TOPTWGRASPTest {
    private static final double EPS = 1e-9;

    private static TOPTWGRASP grasp(String instance, long seed) throws IOException {
        TOPTW p = TOPTWReader.readProblem(instance);
        return new TOPTWGRASP(new TOPTWSolution(p), new Random(seed), null);
    }

    @Test
    public void greedySolutionIsFeasibleForEveryStrategy() throws IOException {
        for (TOPTWGRASP.SelectionStrategy strategy : TOPTWGRASP.SelectionStrategy.values()) {
            TOPTWGRASP g = grasp(TestSupport.C101, 7);
            g.setStrategy(strategy);
            assertEquals(strategy, g.getStrategy());
            for (int i = 0; i < 5; i++) {
                g.computeGreedySolution(3);
                double score = TestSupport.assertFeasible(g.getSolution());
                assertEquals(score, g.getSolution().evaluateFitness(), EPS);
                assertTrue(score > 0);
            }
        }
    }

    @Test
    public void graspIsReproducibleWithSeed() throws IOException {
        TOPTWGRASP a = grasp(TestSupport.C101, 42);
        TOPTWGRASP b = grasp(TestSupport.C101, 42);
        assertEquals(a.grasp(20, 5), b.grasp(20, 5), EPS);
        assertEquals(a.getLastAverageFitness(), b.getLastAverageFitness(), EPS);
        assertTrue(a.getLastBestFitness() >= a.getLastAverageFitness());
    }

    /** Valores obtenidos con el código original (semilla 42): la refactorización los conserva. */
    @Test
    public void refactoringPreservesOriginalResults() throws IOException {
        TOPTWGRASP g = grasp(TestSupport.C101, 42);
        assertEquals(1550.0, g.grasp(50, 3), EPS);
        assertEquals(1448.6, g.getLastAverageFitness(), 1e-6);
        assertEquals(1480.0, g.grasp(50, 5), EPS);
        assertEquals(1389.8, g.getLastAverageFitness(), 1e-6);
    }

    @Test
    public void readerFixImprovesPr01() throws IOException {
        TOPTWGRASP g = grasp(TestSupport.PR01, 42);
        g.grasp(20, 3);
        // Con la lectura errónea de ventanas el mejor valor era 302; ahora es muy superior.
        assertTrue(g.getLastBestFitness() > 400);
        TestSupport.assertFeasible(g.getSolution());
    }

    @Test
    public void graspWritesReport() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        TOPTWGRASP g = grasp("src/test/resources/mini.txt", 1);
        g.setOutput(new PrintStream(bytes, true, "UTF-8"));
        g.grasp(3, 2);
        String text = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(text.contains("--> MEDIA:"));
        assertTrue(text.contains("--> MEJOR SOLUCION: 60.0"));
    }

    @Test
    public void miniInstanceSkipsUnreachablePoi() throws IOException {
        TOPTWGRASP g = grasp("src/test/resources/mini.txt", 3);
        g.computeGreedySolution(3);
        // El POI 4 es inalcanzable dentro de su ventana; los demás se visitan.
        assertEquals(60.0, TestSupport.assertFeasible(g.getSolution()), EPS);
    }

    @Test
    public void comprehensiveEvaluationFindsCheapestInsertion() throws IOException {
        TOPTWGRASP g = grasp("src/test/resources/mini.txt", 1);
        g.getSolution().initSolution();
        List<List<Double>> times = new ArrayList<>();
        times.add(new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));
        List<double[]> c = g.comprehensiveEvaluation(Arrays.asList(1, 2, 3, 4), times);
        assertEquals(3, c.size());
        double[] first = c.get(0);
        assertEquals(1, (int) first[TOPTWGRASP.CUSTOMER]);
        assertEquals(0, (int) first[TOPTWGRASP.ROUTE]);
        assertEquals(0, (int) first[TOPTWGRASP.PREDECESSOR]);
        assertEquals(25.0, first[TOPTWGRASP.COST], EPS); // 10 ida + 5 visita + 10 vuelta
        assertEquals(10.0, first[TOPTWGRASP.SCORE], EPS);
    }

    @Test
    public void selectionStrategies() throws IOException {
        TOPTWGRASP g = grasp("src/test/resources/mini.txt", 5);
        List<double[]> rcl = Arrays.asList(
                new double[]{1, 0, 0, 25, 10}, new double[]{2, 0, 0, 45, 20}, new double[]{3, 0, 0, 25, 30});
        assertEquals(99.0, g.getMaxScore(), EPS);
        assertEquals(2, g.fuzzySelectionBestFDRCL(rcl));
        assertEquals(-1, g.fuzzySelectionBestFDRCL(new ArrayList<>()));
        // Con alfa 0.7 solo el candidato 3 (1 - 30/99 = 0.697) supera el corte.
        assertEquals(2, g.fuzzySelectionAlphaCutRCL(rcl, 0.7));
        int pos = g.fuzzySelectionAlphaCutRCL(rcl, 0.1);
        assertTrue(pos >= 0 && pos < 3);
        int r = g.aleatorySelectionRCL(3);
        assertTrue(r >= 0 && r < 3);
        g.setAlpha(0.5);
        assertEquals(0.5, g.getAlpha(), EPS);
        g.setSolutionTime(10);
        assertEquals(10, g.getSolutionTime());
    }
}
