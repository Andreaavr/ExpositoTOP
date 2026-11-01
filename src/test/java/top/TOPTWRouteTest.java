package top;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TOPTWRouteTest {
    @Test
    public void gettersAndSetters() {
        TOPTWRoute r = new TOPTWRoute();
        assertEquals(TOPTWSolution.NO_INITIALIZED, r.getId());
        r.setPredecessor(1);
        r.setSuccessor(2);
        r.setId(3);
        assertEquals(1, r.getPredecessor());
        assertEquals(2, r.getSuccessor());
        assertEquals(3, r.getId());
        TOPTWRoute r2 = new TOPTWRoute(4, 5, 6);
        assertEquals(4, r2.getPredecessor());
        assertEquals(5, r2.getSuccessor());
        assertEquals(6, r2.getId());
    }
}
