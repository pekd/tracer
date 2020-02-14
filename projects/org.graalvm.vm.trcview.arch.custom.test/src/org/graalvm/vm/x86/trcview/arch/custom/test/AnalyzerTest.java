package org.graalvm.vm.x86.trcview.arch.custom.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.arch.custom.CustomArchitecture;
import org.graalvm.vm.trcview.arch.custom.analysis.CustomAnalyzer;
import org.graalvm.vm.trcview.arch.custom.io.CustomStepEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.none.None;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.x86.trcview.arch.custom.test.impl.MockStepEvent;
import org.junit.Before;
import org.junit.Test;

public class AnalyzerTest {
    public static final String SCRIPT;
    private MemoryTrace mem;

    static {
        try {
            SCRIPT = load("test.c");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String load(String name) throws IOException {
        try (InputStream in = AnalyzerTest.class.getResourceAsStream(name)) {
            return readAll(in);
        }
    }

    private static String readAll(InputStream in) throws IOException {
        Reader read = new InputStreamReader(in);
        StringWriter out = new StringWriter();
        char[] buf = new char[4096];
        int n;
        while ((n = read.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toString();
    }

    @Before
    public void setup() {
        mem = new MemoryTrace();
    }

    @Test
    public void analyzer1() {
        CustomAnalyzer analyzer = new CustomAnalyzer(SCRIPT);
        CustomArchitecture arch = analyzer.getArchitecture();
        assertEquals("c_arch", arch.getName());
        assertEquals("Custom Test Architecture", arch.getDescription());
        assertEquals((short) 0xFF00, arch.getId());
    }

    @Test
    public void analyzer2() {
        CustomAnalyzer analyzer = new CustomAnalyzer(SCRIPT);
        analyzer.start(mem);
        Event evt0 = new MockStepEvent(None.ID, 0, 0, 0xBEEE, new String[]{"salad"}, new byte[]{0x41});
        Event evt1 = new MockStepEvent(None.ID, 0, 1, 0xBEEF, new String[]{"noodle"}, new byte[]{0x42}, "temp", 42L);
        Event evt2 = new MockStepEvent(None.ID, 0, 2, 0xBEF0, new String[]{"tomatoe"}, new byte[]{0x43});
        Node node0 = evt0;
        Node node1 = evt1;
        Node node2 = evt2;
        analyzer.process(evt0, node0);
        analyzer.process(evt1, node1);
        analyzer.process(evt2, node2);
        analyzer.finish();
        assertEquals(1, analyzer.getEvents().size());

        CustomStepEvent evt = (CustomStepEvent) analyzer.getEvents().get(0);
        assertNotNull(evt);
        assertEquals(0xBEEF, evt.getPC());
        assertEquals(42, evt.getState().get("a"));
        assertEquals("nop", evt.getDisassembly());

        byte[] machinecoderef = new byte[]{21};
        assertArrayEquals(machinecoderef, evt.getMachinecode());
    }
}
