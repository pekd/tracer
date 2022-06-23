package org.graalvm.vm.x86.trcview.test.device;

import static org.junit.Assert.assertEquals;

import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldNumberType;
import org.junit.Test;

public class FormatTest {
    @Test
    public void format1() {
        IntegerFieldFormat fmt = new IntegerFieldFormat("reg", 0);
        assertEquals("0", fmt.format(0));
        assertEquals("1", fmt.format(1));
        assertEquals("0", fmt.format(2));
        assertEquals("1", fmt.format(3));
    }

    @Test
    public void format2() {
        IntegerFieldFormat fmt = new IntegerFieldFormat("reg", 1);
        assertEquals("0", fmt.format(0));
        assertEquals("0", fmt.format(1));
        assertEquals("1", fmt.format(2));
        assertEquals("1", fmt.format(3));
        assertEquals("0", fmt.format(4));
    }

    @Test
    public void format3() {
        IntegerFieldFormat fmt = new IntegerFieldFormat("reg", 1, 0, FieldNumberType.BIN);
        assertEquals("00", fmt.format(0));
        assertEquals("01", fmt.format(1));
        assertEquals("10", fmt.format(2));
        assertEquals("11", fmt.format(3));
        assertEquals("00", fmt.format(4));
    }

    @Test
    public void format4() {
        IntegerFieldFormat fmt = new IntegerFieldFormat("reg", 4, 1, FieldNumberType.OCT);
        assertEquals("00", fmt.format(0));
        assertEquals("00", fmt.format(1));
        assertEquals("01", fmt.format(2));
        assertEquals("01", fmt.format(3));
        assertEquals("02", fmt.format(4));
        assertEquals("07", fmt.format(15));
        assertEquals("10", fmt.format(16));
    }
}
