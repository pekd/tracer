package org.graalvm.vm.x86.trcview.test.io;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.graalvm.vm.trcview.io.TextSerializer;
import org.junit.Test;

public class TextSerializerTest {
    @Test
    public void tokenize1() throws IOException {
        String[] data = TextSerializer.tokenize("\"the \\\"test\\\"\";\"0x%x\";\"rip\"");
        String[] ref = new String[]{"the \"test\"", "0x%x", "rip"};
        assertArrayEquals(ref, data);
    }
}
