package org.graalvm.vm.x86.trcview.test.type;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEventI32;
import org.graalvm.vm.trcview.arch.io.MemoryEventI8;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.Autocomment;
import org.graalvm.vm.x86.trcview.test.mock.MockTraceAnalyzer;
import org.junit.Test;

public class AutocommentTest {
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 8, 8, 1, true);

    private static Struct getStruct() {
        Struct substruct = new Struct("substruct");
        substruct.add(0, "string", new Type(DataType.S8, false, 8, Representation.CHAR));
        Struct struct = new Struct("teststruct");
        struct.add(0, "int32", new Type(DataType.U32, Representation.HEX));
        struct.add(4, "int32array", new Type(DataType.U32, false, 4));
        struct.add(20, "substruct", new Type(substruct));
        return struct;
    }

    private static StepEvent getStep(MemoryEvent... events) {
        return new StepEvent(0) {
            @Override
            public byte[] getMachinecode() {
                return null;
            }

            @Override
            public String[] getDisassemblyComponents() {
                return null;
            }

            @Override
            public String getMnemonic() {
                return null;
            }

            @Override
            public long getPC() {
                return 0;
            }

            @Override
            public InstructionType getType() {
                return null;
            }

            @Override
            public long getStep() {
                return 0;
            }

            @Override
            public CpuState getState() {
                return null;
            }

            @Override
            public StepFormat getFormat() {
                return FORMAT;
            }

            @Override
            public List<MemoryEvent> getDataReads() {
                List<MemoryEvent> reads = new ArrayList<>();
                for (MemoryEvent e : events) {
                    if (!e.isWrite()) {
                        reads.add(e);
                    }
                }
                return reads;
            }

            @Override
            public List<MemoryEvent> getDataWrites() {
                List<MemoryEvent> reads = new ArrayList<>();
                for (MemoryEvent e : events) {
                    if (!e.isWrite()) {
                        reads.add(e);
                    }
                }
                return reads;
            }
        };
    }

    private static TraceAnalyzer trc(Variable var) {
        TypedMemory mem = new TypedMemory();
        mem.set(var.getAddress(), var.getType(), var.getName());
        return new MockTraceAnalyzer() {
            @Override
            public TypedMemory getTypedMemory() {
                return mem;
            }
        };
    }

    @Test
    public void test001() {
        Variable var = new Variable(64, new Type(getStruct()), "variable");
        String result = Autocomment.get(trc(var), getStep(new MemoryEventI32(true, 0, 64, false, 0xDEADBEEF)));
        assertEquals("variable.int32 = 0xdeadbeef", result);
    }

    @Test
    public void test002() {
        Variable var = new Variable(64, new Type(getStruct()), "variable");
        String result = Autocomment.get(trc(var), getStep(new MemoryEventI32(true, 0, 72, false, 12345)));
        assertEquals("variable.int32array[1] = 12345", result);
    }

    @Test
    public void test003() {
        Variable var = new Variable(64, new Type(getStruct()), "variable");
        String result = Autocomment.get(trc(var), getStep(new MemoryEventI32(true, 0, 80, false, 12345)));
        assertEquals("variable.int32array[3] = 12345", result);
    }

    @Test
    public void test004() {
        Variable var = new Variable(64, new Type(getStruct()), "variable");
        String result = Autocomment.get(trc(var), getStep(new MemoryEventI8(true, 0, 83, false, (byte) 100)));
        assertEquals("variable.int32array[3] (+3) = 100", result);
    }

    @Test
    public void test005() {
        Variable var = new Variable(64, new Type(getStruct()), "variable");
        String result = Autocomment.get(trc(var), getStep(new MemoryEventI8(true, 0, 84, false, (byte) 65)));
        assertEquals("variable.substruct.string[0] = 'A'", result);
    }

    @Test
    public void test006() {
        Variable var = new Variable(64, new Type(getStruct()), "variable");
        String result = Autocomment.get(trc(var), getStep(new MemoryEventI8(true, 0, 85, false, (byte) 66)));
        assertEquals("variable.substruct.string[1] = 'B'", result);
    }
}
