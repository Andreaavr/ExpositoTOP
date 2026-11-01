package top;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class MainTOPTWTest {

    @Test
    public void defaultOptionsReproduceOriginalExperiment() {
        MainTOPTW.Options o = MainTOPTW.parseArgs(new String[0]);
        assertEquals(MainTOPTW.DEFAULT_DIR, o.dir);
        assertEquals(10000, o.iterations);
        assertNull(o.seed);
        assertEquals(29, o.instances.size());
    }

    @Test
    public void parsesOptions() {
        MainTOPTW.Options o = MainTOPTW.parseArgs(
                new String[]{"--dir", "src/test/resources", "--iterations", "2", "--seed", "9", "mini.txt"});
        assertEquals("src/test/resources", o.dir);
        assertEquals(2, o.iterations);
        assertEquals(Long.valueOf(9), o.seed);
        assertEquals(1, o.instances.size());
    }

    @Test
    public void missingValueIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MainTOPTW.parseArgs(new String[]{"--seed"}));
        assertThrows(NumberFormatException.class,
                () -> MainTOPTW.parseArgs(new String[]{"--iterations", "muchas"}));
    }

    @Test
    public void runsInstances() throws IOException {
        MainTOPTW.Options o = MainTOPTW.parseArgs(
                new String[]{"--dir", "src/test/resources", "--iterations", "2", "--seed", "1", "mini.txt"});
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MainTOPTW.run(o, new PrintStream(bytes, true, "UTF-8"));
        String text = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(text.contains(" --> Instance: mini.txt"));
        assertEquals(3, text.split("MEJOR SOLUCION").length - 1);
    }

    @Test
    public void runFailsOnMissingInstance() {
        MainTOPTW.Options o = MainTOPTW.parseArgs(new String[]{"--dir", "nope", "x.txt"});
        assertThrows(IOException.class, () -> MainTOPTW.run(o, new PrintStream(new ByteArrayOutputStream())));
    }
}
