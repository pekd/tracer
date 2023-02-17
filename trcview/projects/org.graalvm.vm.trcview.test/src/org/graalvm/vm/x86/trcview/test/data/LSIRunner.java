package org.graalvm.vm.x86.trcview.test.data;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.graalvm.vm.trcview.analysis.Analysis;
import org.graalvm.vm.trcview.arch.io.TraceFileReader;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.TraceParser;

public class LSIRunner {
    protected void check(long mask, VariableType... types) {
        long check = 0;
        for (VariableType type : types) {
            check |= type.getMask();
        }
        long actual = mask & ~(VariableType.CHAIN_BIT | VariableType.BREAK_BIT | VariableType.SOLVED.getMask());
        assertEquals(check, actual);
    }

    protected Analysis load(String resource) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            return load(in);
        }
    }

    protected Analysis load(byte[] trace) throws IOException {
        try (InputStream in = new ByteArrayInputStream(trace)) {
            return load(in);
        }
    }

    protected Analysis load(InputStream in) throws IOException {
        TraceFileReader reader = new TraceFileReader(in);
        Analysis analysis = new Analysis(reader.getArchitecture(), Collections.emptyList(), true, false);
        analysis.start();

        Map<Integer, BlockNode> threads = TraceParser.parse(reader, analysis, null);

        BlockNode root = null;
        for (BlockNode block : threads.values()) {
            if (root == null) {
                root = block;
            } else if (block.getStep() < root.getStep()) {
                root = block;
            }
        }
        analysis.finish(root);

        if (root == null || root.getFirstStep() == null) {
            throw new IOException("no steps");
        }

        return analysis;
    }
}
