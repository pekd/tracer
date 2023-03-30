package org.graalvm.vm.x86.trcview.test.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.SymbolTable;
import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEventI16;
import org.graalvm.vm.trcview.arch.io.MemoryEventI32;
import org.graalvm.vm.trcview.arch.io.MemoryEventI64;
import org.graalvm.vm.trcview.arch.io.MemoryEventI8;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.DynamicTypePropagation;
import org.graalvm.vm.trcview.data.Semantics;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.data.ir.MemoryOperand;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.x86.trcview.test.mock.MockTraceAnalyzer;
import org.junit.Before;
import org.junit.Test;

public class ArrayRecoveryTest {
    private DynamicTypePropagation prop;
    private long step;
    private StepEvent lastStep;
    private List<MemoryEvent> reads;
    private List<MemoryEvent> writes;
    private MockTraceAnalyzer trc;
    private TypedMemory mem;

    @Before
    public void setup() {
        StepFormat fmt = new StepFormat(StepFormat.NUMBERFMT_HEX, 4, 4, 1, true);
        Architecture arch = new Architecture() {
            @Override
            public short getId() {
                return 0;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public ArchTraceReader getTraceReader(InputStream in) {
                return null;
            }

            @Override
            public SyscallDecoder getSyscallDecoder() {
                return null;
            }

            @Override
            public CallDecoder getCallDecoder() {
                return null;
            }

            @Override
            public int getTabSize() {
                return 0;
            }

            @Override
            public StepFormat getFormat() {
                return fmt;
            }

            @Override
            public boolean isSystemLevel() {
                return false;
            }

            @Override
            public boolean isStackedTraps() {
                return false;
            }

            @Override
            public int getRegisterCount() {
                return 1;
            }
        };
        SymbolTable symtab = new SymbolTable(fmt, Collections.emptyNavigableMap());
        MemoryTrace memtrc = new MemoryTrace();
        prop = new DynamicTypePropagation(arch, symtab, memtrc);
        step = 0;
        lastStep = null;
        reads = new ArrayList<>();
        writes = new ArrayList<>();
        mem = new TypedMemory();
        trc = new MockTraceAnalyzer() {
            @Override
            public Architecture getArchitecture() {
                return arch;
            }

            @Override
            public TypedMemory getTypedMemory() {
                return mem;
            }
        };
    }

    private void finish() {
        prop.finish();

        prop.transfer(trc);
    }

    private void step(long pc) {
        CpuState state = new CpuState() {
            private final long stepId = step;

            public long getStep() {
                return stepId;
            }

            public long getPC() {
                return pc;
            }

            public long get(String name) {
                return 0;
            }

            public int getTid() {
                return 0;
            }
        };

        lastStep = new StepEvent(0) {
            private final long stepId = step;

            @Override
            public byte[] getMachinecode() {
                return new byte[]{42};
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
                return pc;
            }

            @Override
            public InstructionType getType() {
                return InstructionType.OTHER;
            }

            @Override
            public long getStep() {
                return stepId;
            }

            @Override
            public CpuState getState() {
                return state;
            }

            @Override
            public StepFormat getFormat() {
                return null;
            }

            @Override
            public void getSemantics(Semantics s) {
                for (MemoryEvent rd : getDataReads()) {
                    VariableType type;
                    switch (rd.getSize()) {
                        default:
                        case 1:
                            type = VariableType.I8;
                            break;
                        case 2:
                            type = VariableType.I16;
                            break;
                        case 4:
                            type = VariableType.I32;
                            break;
                        case 8:
                            type = VariableType.I64;
                            break;
                    }
                    s.set(new MemoryOperand(rd.getAddress()), type);
                }

                for (MemoryEvent rd : getDataWrites()) {
                    VariableType type;
                    switch (rd.getSize()) {
                        default:
                        case 1:
                            type = VariableType.I8;
                            break;
                        case 2:
                            type = VariableType.I16;
                            break;
                        case 4:
                            type = VariableType.I32;
                            break;
                        case 8:
                            type = VariableType.I64;
                            break;
                    }
                    s.set(new MemoryOperand(rd.getAddress()), type);
                }
            }
        };

        for (MemoryEvent read : reads) {
            lastStep.addRead(read);
        }
        for (MemoryEvent write : writes) {
            lastStep.addWrite(write);
        }
        reads.clear();
        writes.clear();

        prop.step(lastStep, state);

        step++;
    }

    private void read(long addr, byte value) {
        reads.add(new MemoryEventI8(true, 0, addr, false, value));
    }

    private void read(long addr, short value) {
        reads.add(new MemoryEventI16(true, 0, addr, false, value));
    }

    private void read(long addr, int value) {
        reads.add(new MemoryEventI32(true, 0, addr, false, value));
    }

    private void read(long addr, long value) {
        reads.add(new MemoryEventI64(true, 0, addr, false, value));
    }

    @Test
    public void testSimple8bit() {
        for (int i = 0; i < 64; i++) {
            read(0x100 + i, (byte) (i + 42));
            step(0);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(1, var.getType().getElementSize());
        assertEquals(DataType.U8, var.getType().getElementType().getType());
    }

    @Test
    public void testSimple16bit() {
        for (int i = 0; i < 64; i++) {
            read(0x100 + i * 2, (short) (i + 42));
            step(0);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(2, var.getType().getElementSize());
        assertEquals(DataType.U16, var.getType().getElementType().getType());
    }

    @Test
    public void testSimple32bit() {
        for (int i = 0; i < 64; i++) {
            read(0x100 + i * 4, i + 42);
            step(0);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(4, var.getType().getElementSize());
        assertEquals(DataType.U32, var.getType().getElementType().getType());
    }

    @Test
    public void testSimple64bit() {
        for (int i = 0; i < 64; i++) {
            read(0x100 + i * 8, (long) (i + 42));
            step(0);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(8, var.getType().getElementSize());
        assertEquals(DataType.U64, var.getType().getElementType().getType());
    }

    @Test
    public void testUnaligned16bit() {
        for (int i = 0; i < 64; i++) {
            read(0x101 + i * 2, (short) (i + 42));
            step(0);
        }

        finish();

        Variable var = mem.get(0x101);
        assertNotNull(var);
        assertEquals(0x101, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(2, var.getType().getElementSize());
        assertEquals(DataType.U16, var.getType().getElementType().getType());
    }

    @Test
    public void testUnaligned32bit() {
        for (int i = 0; i < 64; i++) {
            read(0x101 + i * 4, i + 42);
            step(0);
        }

        finish();

        Variable var = mem.get(0x101);
        assertNotNull(var);
        assertEquals(0x101, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(4, var.getType().getElementSize());
        assertEquals(DataType.U32, var.getType().getElementType().getType());
    }

    @Test
    public void testUnaligned64bit() {
        for (int i = 0; i < 64; i++) {
            read(0x101 + i * 8, (long) (i + 42));
            step(0);
        }

        finish();

        Variable var = mem.get(0x101);
        assertNotNull(var);
        assertEquals(0x101, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(8, var.getType().getElementSize());
        assertEquals(DataType.U64, var.getType().getElementType().getType());
    }

    @Test
    public void testOverlap8bit() {
        for (int i = 0; i < 48; i++) {
            read(0x100 + i, (byte) (i + 42));
            step(0);
        }

        for (int i = 0; i < 48; i++) {
            read(0x110 + i, (byte) (i + 42));
            step(4);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(1, var.getType().getElementSize());
        assertEquals(DataType.U8, var.getType().getElementType().getType());
    }

    @Test
    public void testOverlap16bit() {
        for (int i = 0; i < 48; i++) {
            read(0x100 + i * 2, (short) (i + 42));
            step(0);
        }

        for (int i = 0; i < 48; i++) {
            read(0x120 + i * 2, (short) (i + 42));
            step(4);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(2, var.getType().getElementSize());
        assertEquals(DataType.U16, var.getType().getElementType().getType());
    }

    @Test
    public void testOverlap32bit() {
        for (int i = 0; i < 48; i++) {
            read(0x100 + i * 4, i + 42);
            step(0);
        }

        for (int i = 0; i < 48; i++) {
            read(0x140 + i * 4, i + 42);
            step(4);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(4, var.getType().getElementSize());
        assertEquals(DataType.U32, var.getType().getElementType().getType());
    }

    @Test
    public void testOverlap64bit() {
        for (int i = 0; i < 48; i++) {
            read(0x100 + i * 8, (long) (i + 42));
            step(0);
        }

        for (int i = 0; i < 48; i++) {
            read(0x180 + i * 8, (long) (i + 42));
            step(4);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(8, var.getType().getElementSize());
        assertEquals(DataType.U64, var.getType().getElementType().getType());
    }

    @Test
    public void testMultiOverlap8bit() {
        for (int i = 0; i < 24; i++) {
            read(0x100 + i, (byte) (i + 42));
            step(0);
        }

        for (int i = 0; i < 24; i++) {
            read(0x110 + i, (byte) (i + 42));
            step(4);
        }

        for (int i = 0; i < 24; i++) {
            read(0x120 + i, (byte) (i + 42));
            step(8);
        }

        for (int i = 0; i < 24; i++) {
            read(0x128 + i, (byte) (i + 42));
            step(12);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(1, var.getType().getElementSize());
        assertEquals(DataType.U8, var.getType().getElementType().getType());
    }

    @Test
    public void testMultiOverlap16bit() {
        for (int i = 0; i < 24; i++) {
            read(0x100 + i * 2, (short) (i + 42));
            step(0);
        }

        for (int i = 0; i < 24; i++) {
            read(0x120 + i * 2, (short) (i + 42));
            step(4);
        }

        for (int i = 0; i < 24; i++) {
            read(0x140 + i * 2, (short) (i + 42));
            step(8);
        }

        for (int i = 0; i < 24; i++) {
            read(0x150 + i * 2, (short) (i + 42));
            step(12);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(2, var.getType().getElementSize());
        assertEquals(DataType.U16, var.getType().getElementType().getType());
    }

    @Test
    public void testMultiOverlap32bit() {
        for (int i = 0; i < 24; i++) {
            read(0x100 + i * 4, i + 42);
            step(0);
        }

        for (int i = 0; i < 24; i++) {
            read(0x140 + i * 4, i + 42);
            step(4);
        }

        for (int i = 0; i < 24; i++) {
            read(0x180 + i * 4, i + 42);
            step(8);
        }

        for (int i = 0; i < 24; i++) {
            read(0x1A0 + i * 4, i + 42);
            step(12);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(4, var.getType().getElementSize());
        assertEquals(DataType.U32, var.getType().getElementType().getType());
    }

    @Test
    public void testMultiOverlap64bit() {
        for (int i = 0; i < 24; i++) {
            read(0x100 + i * 8, (long) (i + 42));
            step(0);
        }

        for (int i = 0; i < 24; i++) {
            read(0x180 + i * 8, (long) (i + 42));
            step(4);
        }

        for (int i = 0; i < 24; i++) {
            read(0x200 + i * 8, (long) (i + 42));
            step(8);
        }

        for (int i = 0; i < 24; i++) {
            read(0x240 + i * 8, (long) (i + 42));
            step(12);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(8, var.getType().getElementSize());
        assertEquals(DataType.U64, var.getType().getElementType().getType());
    }

    @Test
    public void testMultiOverlap8bitPattern2() {
        for (int i = 0; i < 24; i++) {
            read(0x100 + i, (byte) (i + 42));
            step(0);
        }

        for (int i = 0; i < 24; i++) {
            read(0x120 + i, (byte) (i + 42));
            step(8);
        }

        for (int i = 0; i < 24; i++) {
            read(0x128 + i, (byte) (i + 42));
            step(12);
        }

        for (int i = 0; i < 24; i++) {
            read(0x110 + i, (byte) (i + 42));
            step(4);
        }

        finish();

        Variable var = mem.get(0x100);
        assertNotNull(var);
        assertEquals(0x100, var.getAddress());
        assertEquals(64, var.getType().getElements());
        assertEquals(1, var.getType().getElementSize());
        assertEquals(DataType.U8, var.getType().getElementType().getType());
    }
}
