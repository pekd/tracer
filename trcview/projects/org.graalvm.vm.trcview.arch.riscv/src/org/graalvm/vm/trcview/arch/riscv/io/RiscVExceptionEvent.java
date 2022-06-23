package org.graalvm.vm.trcview.arch.riscv.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.io.WordInputStream;

public class RiscVExceptionEvent extends InterruptEvent {
    public static final int EXCEPTION_DECREMENTER = 0x00000001;
    public static final int EXCEPTION_SYSCALL = 0x00000002;
    public static final int EXCEPTION_EXTERNAL_INT = 0x00000004;
    public static final int EXCEPTION_DSI = 0x00000008;
    public static final int EXCEPTION_ISI = 0x00000010;
    public static final int EXCEPTION_ALIGNMENT = 0x00000020;
    public static final int EXCEPTION_FPU_UNAVAILABLE = 0x00000040;
    public static final int EXCEPTION_PROGRAM = 0x00000080;
    public static final int EXCEPTION_PERFORMANCE_MONITOR = 0x00000100;
    private static final Map<Integer, String> TYPES;

    private final int type;
    private final RiscVStepEvent step;

    static {
        TYPES = new HashMap<>();
        TYPES.put(EXCEPTION_DECREMENTER, "DECREMENTER");
        TYPES.put(EXCEPTION_SYSCALL, "SYSCALL");
        TYPES.put(EXCEPTION_EXTERNAL_INT, "EXTERNAL_INT");
        TYPES.put(EXCEPTION_DSI, "DSI");
        TYPES.put(EXCEPTION_ISI, "ISI");
        TYPES.put(EXCEPTION_ALIGNMENT, "ALIGNMENT");
        TYPES.put(EXCEPTION_FPU_UNAVAILABLE, "FPU_UNAVAILABLE");
        TYPES.put(EXCEPTION_PROGRAM, "PROGRAM");
        TYPES.put(EXCEPTION_PERFORMANCE_MONITOR, "PERFORMANCE_MONITOR");
    }

    protected RiscVExceptionEvent(WordInputStream in, int tid, RiscVStepEvent step) throws IOException {
        super(tid);
        this.step = step;
        type = in.read32bit();
    }

    @Override
    public StepEvent getStep() {
        return step;
    }

    public static String getExceptionString(int type) {
        if (type == 0) {
            return "0";
        }

        List<String> result = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            int bit = 1 << i;
            if (BitTest.test(type, bit)) {
                String s = TYPES.get(bit);
                if (s == null) {
                    s = "0x" + Integer.toHexString(bit);
                }
                result.add(s);
            }
        }

        return String.join("|", result);
    }

    @Override
    public String toString() {
        return "<exception " + getExceptionString(type) + ">";
    }
}
