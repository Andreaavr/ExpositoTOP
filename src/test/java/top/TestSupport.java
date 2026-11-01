package top;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** Utilidades compartidas por los tests. */
final class TestSupport {
    static final String C101 = "Instances/TOPTW/c101.txt";
    static final String PR01 = "Instances/TOPTW/pr01.txt";

    private TestSupport() {
    }

    static TOPTW mini() throws IOException {
        return TOPTWReader.readProblem("src/test/resources/mini.txt");
    }

    /**
     * Comprueba, de forma independiente al algoritmo, que la solución es factible:
     * cada POI aparece como mucho una vez, se respetan las ventanas temporales y la
     * duración máxima de cada ruta. Devuelve la puntuación total.
     */
    static double assertFeasible(TOPTWSolution s) {
        TOPTW p = s.getProblem();
        Set<Integer> visited = new HashSet<>();
        double total = 0;
        for (int k = 0; k < s.getCreatedRoutes(); k++) {
            int depot = s.getIndexRoute(k);
            int pre = depot;
            double time = 0;
            int steps = 0;
            do {
                int suc = s.getSuccessor(pre);
                assertEquals("enlace predecesor roto", pre, s.getPredecessor(suc));
                time += p.getDistance(pre, suc);
                assertTrue("ventana temporal incumplida en " + suc, time < p.getDueTime(suc));
                time = Math.max(time, p.getReadyTime(suc)) + p.getServiceTime(suc);
                assertTrue("duración máxima superada", time <= p.getMaxTimePerRoute());
                if (suc != depot) {
                    assertTrue("POI repetido " + suc, visited.add(suc));
                    assertFalse(p.isDepot(suc));
                    total += p.getScore(suc);
                }
                pre = suc;
                assertTrue("ruta no cerrada", ++steps <= p.getPOIs() + 1);
            } while (pre != depot);
        }
        return total;
    }
}
