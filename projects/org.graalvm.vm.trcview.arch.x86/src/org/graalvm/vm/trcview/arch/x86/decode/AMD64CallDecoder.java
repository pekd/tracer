package org.graalvm.vm.trcview.arch.x86.decode;

import java.util.List;

import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.x86.io.AMD64CpuState;
import org.graalvm.vm.trcview.decode.CallDecoder;

public class AMD64CallDecoder extends CallDecoder {
    // TODO: implement SysV ABI

    @Override
    public long getArgument(CpuState state, int id, List<Type> types) {
        return getRegister((AMD64CpuState) state, id);
    }

    @Override
    public long getReturnValue(CpuState state, Type type) {
        return ((AMD64CpuState) state).getRAX();
    }

    private static long getRegister(AMD64CpuState state, int reg) {
        switch (reg) {
            case 0:
                return state.getRDI();
            case 1:
                return state.getRSI();
            case 2:
                return state.getRDX();
            case 3:
                return state.getRCX();
            case 4:
                return state.getR8();
            case 5:
                return state.getR9();
            default:
                return 0;
        }
    }
}
