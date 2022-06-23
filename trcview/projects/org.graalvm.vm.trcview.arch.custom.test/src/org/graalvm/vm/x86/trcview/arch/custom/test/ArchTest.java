package org.graalvm.vm.x86.trcview.arch.custom.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.graalvm.vm.trcview.arch.custom.CustomArchitecture;
import org.junit.Test;

public class ArchTest {
    @Test
    public void arch() {
        String script = "int init() {\n" +
                        "    return 0xFF00;\n" +
                        "}\n";
        CustomArchitecture arch = new CustomArchitecture(script);
        assertEquals((short) 0xFF00, arch.getId());
        assertNull(arch.getName());
        assertNull(arch.getDescription());
    }
}
