package org.graalvm.vm.trcview.arch.pdp11.decode;

import java.util.List;

import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.pdp11.io.PDP11CpuState;
import org.graalvm.vm.trcview.decode.CallDecoder;

public class PDP11CallDecoder extends CallDecoder {
    @Override
    public long getArgument(CpuState state, int id, List<Type> types) {
        return getRegister((PDP11CpuState) state, id);
    }

    @Override
    public long getReturnValue(CpuState state, Type type) {
        return ((PDP11CpuState) state).getRegister(0);
    }

    private static long getRegister(PDP11CpuState state, int reg) {
        if (reg < 4) {
            return Short.toUnsignedInt(state.getRegister(reg));
        } else {
            return 0;
        }
    }
}
