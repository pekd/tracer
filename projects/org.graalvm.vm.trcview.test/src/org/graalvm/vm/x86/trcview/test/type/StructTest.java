package org.graalvm.vm.x86.trcview.test.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.junit.Test;

public class StructTest {
    @Test
    public void emptyStruct() {
        Struct struct = new Struct("empty");
        assertEquals("empty", struct.getName());
        assertTrue(struct.getFields().isEmpty());
        assertEquals("struct empty { }", struct.toString());
    }

    @Test
    public void twoFields() {
        Struct struct = new Struct("twofields");
        struct.add("first", new Type(DataType.S32));
        struct.add(null, new Type(DataType.U32));
        assertEquals("twofields", struct.getName());
        assertFalse(struct.getFields().isEmpty());
        assertEquals("struct twofields { s32 first; u32 field_4; }", struct.toString());
        assertEquals("first", struct.getFieldAt(0).getName());
        assertEquals("field_4", struct.getFieldAt(4).getName());
        assertEquals(8, struct.getSize());
    }
}
