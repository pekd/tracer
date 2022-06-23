package org.graalvm.vm.x86.trcview.test.data;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.graalvm.vm.trcview.analysis.memory.MemorySegment;
import org.graalvm.vm.trcview.analysis.memory.Protection;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.DataViewModel;
import org.graalvm.vm.x86.trcview.test.mock.MockArchitecture;
import org.graalvm.vm.x86.trcview.test.mock.MockTraceAnalyzer;
import org.junit.Before;
import org.junit.Test;

public class DataModelTest {
    private Architecture arch;
    private TypedMemory mem;
    private MemorySegment seg;
    private TraceAnalyzer trc;

    @Before
    public void setup() {
        mem = new TypedMemory();
        seg = new MemorySegment(0x1000, 0x1fff, new Protection(true, true, true), "mem");
        arch = new MockArchitecture(true, true);
        trc = new MockTraceAnalyzer() {
            @Override
            public Architecture getArchitecture() {
                return arch;
            }

            @Override
            public List<MemorySegment> getMemorySegments(long step) {
                return Arrays.asList(seg);
            }

            @Override
            public TypedMemory getTypedMemory() {
                return mem;
            }
        };
    }

    @Test
    public void testEmpty() {
        DataViewModel model = new DataViewModel();
        model.setTraceAnalyzer(trc);
        model.setStep(0);

        assertEquals(0x1000, model.getAddressByLine(0));
        assertEquals(0x1000, model.getAddressByLine(1));
        assertEquals(0x1000, model.getAddressByLine(2));
        assertEquals(0x1000, model.getAddressByLine(3));
        assertEquals(0x1001, model.getAddressByLine(4));
        assertEquals(0x1002, model.getAddressByLine(5));
        assertEquals(0x1003, model.getAddressByLine(6));
        assertEquals(0x1004, model.getAddressByLine(7));

        assertEquals(3, model.getLineByAddress(0x1000));
        assertEquals(4, model.getLineByAddress(0x1001));
        assertEquals(5, model.getLineByAddress(0x1002));
        assertEquals(6, model.getLineByAddress(0x1003));
        assertEquals(7, model.getLineByAddress(0x1004));

        assertEquals(0x1000 + 5, model.getLineCount());
    }

    @Test
    public void testWord() {
        mem.set(0x1001, new Type(DataType.U16));
        mem.set(0x1004, new Type(DataType.U16));
        DataViewModel model = new DataViewModel();
        model.setTraceAnalyzer(trc);
        model.setStep(0);

        assertEquals(0x1000, model.getAddressByLine(0));
        assertEquals(0x1000, model.getAddressByLine(1));
        assertEquals(0x1000, model.getAddressByLine(2));
        assertEquals(0x1000, model.getAddressByLine(3));
        assertEquals(0x1001, model.getAddressByLine(4));
        assertEquals(0x1003, model.getAddressByLine(5));
        assertEquals(0x1004, model.getAddressByLine(6));
        assertEquals(0x1006, model.getAddressByLine(7));

        assertEquals(3, model.getLineByAddress(0x1000));
        assertEquals(4, model.getLineByAddress(0x1001));
        assertEquals(4, model.getLineByAddress(0x1002));
        assertEquals(5, model.getLineByAddress(0x1003));
        assertEquals(6, model.getLineByAddress(0x1004));
        assertEquals(6, model.getLineByAddress(0x1005));
        assertEquals(7, model.getLineByAddress(0x1006));

        assertEquals(0x1000 + 5 - 2, model.getLineCount());
    }
}
