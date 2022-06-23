package org.graalvm.vm.x86.trcview.test.data;

import static org.junit.Assert.assertSame;

import org.graalvm.vm.trcview.data.type.VariableType;
import org.junit.Test;

public class VariableTypeTest {
    @Test
    public void testI16Pointer() {
        long bits = VariableType.POINTER_I16.getMask();

        VariableType result = VariableType.resolve(bits, 2);
        assertSame(VariableType.POINTER_I16, result);
    }

    @Test
    public void testI16Pointer16bit() {
        long bits = VariableType.POINTER_I16.getMask() | VariableType.I16.getMask();

        VariableType result = VariableType.resolve(bits, 2);
        assertSame(VariableType.POINTER_I16, result);
    }

    @Test
    public void testI8on16bit() {
        long bits = VariableType.I8.getMask() | VariableType.CHAIN_BIT;

        VariableType result = VariableType.resolve(bits, 2);
        assertSame(VariableType.I8, result);
    }
}
