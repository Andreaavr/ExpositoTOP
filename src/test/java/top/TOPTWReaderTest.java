package top;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TOPTWReaderTest {
    private static final double EPS = 1e-9;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void readsSolomonInstance() throws IOException {
        TOPTW p = TOPTWReader.readProblem(TestSupport.C101);
        assertEquals(100, p.getPOIs());
        assertEquals(10, p.getVehicles());
        assertEquals(1236.0, p.getMaxTimePerRoute(), EPS);
        assertEquals(45.0, p.getX(1), EPS);
        assertEquals(68.0, p.getY(1), EPS);
        assertEquals(90.0, p.getServiceTime(1), EPS);
        assertEquals(10.0, p.getScore(1), EPS);
        assertEquals(912.0, p.getReadyTime(1), EPS);
        assertEquals(967.0, p.getDueTime(1), EPS);
    }

    /** Regresión: con listas de longitud variable la ventana está en los dos últimos campos. */
    @Test
    public void readsTimeWindowFromLastFieldsWithVariableLengthList() throws IOException {
        TOPTW p = TOPTWReader.readProblem(TestSupport.PR01);
        assertEquals(48, p.getPOIs());
        assertEquals(360.0, p.getReadyTime(25), EPS);
        assertEquals(505.0, p.getDueTime(25), EPS);
        assertEquals(1000.0, p.getMaxTimePerRoute(), EPS);
    }

    @Test
    public void readsMiniInstance() throws IOException {
        TOPTW p = TestSupport.mini();
        assertEquals(4, p.getPOIs());
        assertEquals(2, p.getVehicles());
        assertEquals(0.0, p.getReadyTime(2), EPS);
        assertEquals(60.0, p.getDueTime(2), EPS);
        assertEquals(10.0, p.getDistance(0, 1), EPS);
    }

    @Test
    public void missingFileThrows() {
        assertThrows(IOException.class, () -> TOPTWReader.readProblem("no/existe.txt"));
    }

    @Test
    public void truncatedFileThrows() throws IOException {
        File f = tmp.newFile("trunc.txt");
        Files.write(f.toPath(), "4 2 4 1\n0 200\n0 0 0 0 0 0 0 0 100\n".getBytes(StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> TOPTWReader.readProblem(f.getPath()));
    }

    @Test
    public void malformedNumberThrows() throws IOException {
        File f = tmp.newFile("bad.txt");
        Files.write(f.toPath(), "4 dos 4 1\n".getBytes(StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> TOPTWReader.readProblem(f.getPath()));
    }
}
