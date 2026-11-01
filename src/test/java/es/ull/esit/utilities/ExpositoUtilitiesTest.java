package es.ull.esit.utilities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ExpositoUtilitiesTest {
    private static final int X = Integer.MAX_VALUE;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void simplifyStringCollapsesWhitespace() {
        assertEquals("1 2 3", ExpositoUtilities.simplifyString("  1\t\t2      3  "));
        assertEquals("", ExpositoUtilities.simplifyString("   "));
    }

    @Test
    public void numberDetection() {
        assertTrue(ExpositoUtilities.isInteger("12"));
        assertFalse(ExpositoUtilities.isInteger("1.5"));
        assertFalse(ExpositoUtilities.isInteger(null));
        assertTrue(ExpositoUtilities.isDouble("1.5"));
        assertFalse(ExpositoUtilities.isDouble("abc"));
        assertFalse(ExpositoUtilities.isDouble(null));
    }

    @Test
    public void formatting() {
        assertEquals("3.142", ExpositoUtilities.getFormat(Math.PI));
        assertEquals("3.14", ExpositoUtilities.getFormat(Math.PI, 2));
        assertEquals("7", ExpositoUtilities.getFormat("7"));
        assertEquals("2.500", ExpositoUtilities.getFormat("2.5"));
        assertEquals("hola", ExpositoUtilities.getFormat("hola"));
        assertEquals("   ab", ExpositoUtilities.getFormat("ab", 5));
        assertEquals("ab   ", ExpositoUtilities.getFormat("ab", 5, ExpositoUtilities.ALIGNMENT_LEFT));
        assertEquals("    1  2.000", ExpositoUtilities.getFormat(new String[]{"1", "2.0"}, new int[]{5, 7}));
        assertEquals("    a    b", ExpositoUtilities.getFormat(Arrays.asList("a", "b"), 5));
        assertEquals("         1         2", ExpositoUtilities.getFormatIntegers(Arrays.asList(1, 2)));
        assertEquals("         x", ExpositoUtilities.getFormat(new String[]{"x"}));
        assertEquals("  a\n  b", ExpositoUtilities.getFormat(new String[][]{{"a"}, {"b"}}, 3));
        assertEquals("a  b",
                ExpositoUtilities.getFormat(new String[]{"a", "b"}, new int[]{3, 1},
                        new int[]{ExpositoUtilities.ALIGNMENT_LEFT, ExpositoUtilities.ALIGNMENT_RIGHT}));
    }

    @Test
    public void multiplyMatrices() {
        double[][] a = {{1, 2}, {3, 4}};
        double[][] b = {{5}, {6}};
        double[][] r = ExpositoUtilities.multiplyMatrices(a, b);
        assertArrayEquals(new double[]{17}, r[0], 1e-9);
        assertArrayEquals(new double[]{39}, r[1], 1e-9);
        assertEquals(0, ExpositoUtilities.multiplyMatrices(new double[0][0], b).length);
        assertThrows(IllegalArgumentException.class,
                () -> ExpositoUtilities.multiplyMatrices(a, new double[][]{{1}}));
    }

    @Test
    public void cycleDetection() {
        int[][] dag = {{X, 1, X, X}, {X, X, 1, X}, {X, X, X, 1}, {X, X, X, X}};
        assertTrue(ExpositoUtilities.isAcyclic(dag));
        int[][] cyclic = {{X, 1, X, X}, {X, X, 1, X}, {X, 1, X, 1}, {X, X, X, X}};
        assertFalse(ExpositoUtilities.isAcyclic(cyclic));
        assertTrue(ExpositoUtilities.thereIsPath(cyclic, 1));
        assertFalse(ExpositoUtilities.thereIsPath(dag, 1));
    }

    @Test
    public void writeAndPrintFile() throws IOException {
        File f = new File(tmp.getRoot(), "out.txt");
        ExpositoUtilities.writeTextToFile(f.getPath(), "línea 1\nlínea 2");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ExpositoUtilities.printFile(f.getPath(), new PrintStream(bytes, true, "UTF-8"));
        String text = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        assertEquals("línea 1" + System.lineSeparator() + "línea 2" + System.lineSeparator(), text);
        assertThrows(IOException.class,
                () -> ExpositoUtilities.printFile("no/existe", new PrintStream(new ByteArrayOutputStream())));
    }
}
