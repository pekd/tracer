package org.graalvm.vm.x86.trcview.test.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Field;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.data.StructArrayRecovery;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.trcview.test.mock.MockArchitecture;
import org.graalvm.vm.x86.trcview.test.mock.MockTraceAnalyzer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StructArrayRecoveryTest {
    private TraceAnalyzer trc;
    private TypedMemory mem;
    private StructArrayRecovery array;

    private void createAnalyzer() {
        Architecture arch = new MockArchitecture(false, true);
        ArchitectureTypeInfo archinfo = arch.getTypeInfo();
        UserTypeDatabase types = new UserTypeDatabase(archinfo);
        mem = new TypedMemory();
        trc = new MockTraceAnalyzer() {
            @Override
            public Architecture getArchitecture() {
                return arch;
            }

            @Override
            public UserTypeDatabase getTypeDatabase() {
                return types;
            }

            @Override
            public TypedMemory getTypedMemory() {
                return mem;
            }
        };
    }

    @BeforeClass
    public static void setupClass() {
        Trace.setup();
    }

    @Before
    public void setup() {
        createAnalyzer();
        array = new StructArrayRecovery(trc);
    }

    @Test
    public void testSimpleArray() {
        array.defineArray(new Type(DataType.U8), 2, 0x1000, 0x1100);
        array.transfer();
        Variable var = mem.get(0x1000);
        assertEquals(1, mem.getAllTypes().size());
        assertNotNull(var);
        assertEquals(0x1000, var.getAddress());
        assertNotNull(var.getType().getStruct());
    }

    @Test
    public void testStructBottom() {
        array.defineArray(new Type(DataType.U16), 4, 0x1000, 0x1100);
        array.defineArray(new Type(DataType.S16), 4, 0x1002, 0x1102);
        array.transfer();
        Variable var = mem.get(0x1000);
        assertEquals(1, mem.getAllTypes().size());
        assertNotNull(var);
        assertEquals(0x1000, var.getAddress());

        Struct struct = var.getType().getStruct();
        assertNotNull(struct);

        assertEquals(4, struct.getSize());
        assertEquals(2, struct.getFields().size());

        assertEquals(new Field(0, "field_0", new Type(DataType.U16)), struct.getFieldAt(0));
        assertEquals(new Field(2, "field_2", new Type(DataType.S16)), struct.getFieldAt(2));
    }

    @Test
    public void testStructTop() {
        array.defineArray(new Type(DataType.S16), 4, 0x1002, 0x1102);
        array.defineArray(new Type(DataType.U16), 4, 0x1000, 0x1100);
        array.transfer();
        Variable var = mem.get(0x1000);
        assertEquals(1, mem.getAllTypes().size());
        assertNotNull(var);
        assertEquals(0x1000, var.getAddress());

        Struct struct = var.getType().getStruct();
        assertNotNull(struct);

        assertEquals(4, struct.getSize());
        assertEquals(2, struct.getFields().size());

        assertEquals(new Field(0, "field_0", new Type(DataType.U16)), struct.getFieldAt(0));
        assertEquals(new Field(2, "field_2", new Type(DataType.S16)), struct.getFieldAt(2));
    }

    @Test
    public void testGap() {
        array.defineArray(new Type(DataType.U16), 6, 0x1000, 0x1100);
        array.defineArray(new Type(DataType.S16), 6, 0x1004, 0x1104);
        array.transfer();
        Variable var = mem.get(0x1000);
        assertEquals(1, mem.getAllTypes().size());
        assertNotNull(var);
        assertEquals(0x1000, var.getAddress());

        Struct struct = var.getType().getStruct();
        assertNotNull(struct);

        assertEquals(6, struct.getSize());
        assertEquals(3, struct.getFields().size());

        assertEquals(new Field(0, "field_0", new Type(DataType.U16)), struct.getFieldAt(0));
        assertEquals(new Field(2, "pad_2", new Type(DataType.U8, false, 2)), struct.getFieldAt(2));
        assertEquals(new Field(4, "field_4", new Type(DataType.S16)), struct.getFieldAt(4));
    }

    @Test
    public void testGapAtEnd() {
        array.defineArray(new Type(DataType.U16), 6, 0x1000, 0x1100);
        array.defineArray(new Type(DataType.S16), 6, 0x1002, 0x1102);
        array.transfer();
        Variable var = mem.get(0x1000);
        assertEquals(1, mem.getAllTypes().size());
        assertNotNull(var);
        assertEquals(0x1000, var.getAddress());

        Struct struct = var.getType().getStruct();
        assertNotNull(struct);

        assertEquals(6, struct.getSize());
        assertEquals(3, struct.getFields().size());

        assertEquals(new Field(0, "field_0", new Type(DataType.U16)), struct.getFieldAt(0));
        assertEquals(new Field(2, "field_2", new Type(DataType.S16)), struct.getFieldAt(2));
        assertEquals(new Field(4, "pad_4", new Type(DataType.U8, false, 2)), struct.getFieldAt(4));
    }

    @Test
    public void testOverlapStart() {
        array.defineArray(new Type(DataType.U16), 4, 0x1000, 0x1100);
        array.defineArray(new Type(DataType.U16), 4, 0x800, 0x1010);
        array.transfer();
        Variable var = mem.get(0x800);
        assertEquals(1, mem.getAllTypes().size());
        assertNotNull(var);
        assertEquals(0x800, var.getAddress());

        Struct struct = var.getType().getStruct();
        assertNotNull(struct);

        assertEquals(4, struct.getSize());
        assertEquals(2, struct.getFields().size());

        assertEquals(new Field(0, "field_0", new Type(DataType.U16)), struct.getFieldAt(0));
        assertEquals(new Field(2, "pad_2", new Type(DataType.U8, false, 2)), struct.getFieldAt(2));
    }

    @Test
    public void testOverlapStartOffset() {
        array.defineArray(new Type(DataType.U16), 4, 0x1000, 0x1100);
        array.defineArray(new Type(DataType.S16), 4, 0x802, 0x1012);
        array.transfer();
        Variable var = mem.get(0x802);
        assertEquals(1, mem.getAllTypes().size());
        assertNotNull(var);
        assertEquals(0x802, var.getAddress());

        Struct struct = var.getType().getStruct();
        assertNotNull(struct);

        assertEquals(4, struct.getSize());
        assertEquals(2, struct.getFields().size());

        assertEquals(new Field(0, "field_0", new Type(DataType.S16)), struct.getFieldAt(0));
        assertEquals(new Field(2, "field_2", new Type(DataType.U16)), struct.getFieldAt(2));
    }
}
