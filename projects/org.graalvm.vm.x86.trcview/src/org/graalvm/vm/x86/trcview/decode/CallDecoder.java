package org.graalvm.vm.x86.trcview.decode;

import static org.graalvm.vm.x86.trcview.decode.DecoderUtils.cstr;
import static org.graalvm.vm.x86.trcview.decode.DecoderUtils.ptr;

import org.graalvm.vm.x86.isa.CpuState;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.x86.trcview.analysis.type.DataType;
import org.graalvm.vm.x86.trcview.analysis.type.Function;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.analysis.type.Type;

public class CallDecoder {
    private static long getRegister(CpuState state, int reg) {
        switch (reg) {
            case 0:
                return state.rdi;
            case 1:
                return state.rsi;
            case 2:
                return state.rdx;
            case 3:
                return state.rcx;
            case 4:
                return state.r8;
            case 5:
                return state.r9;
            default:
                return 0;
        }
    }

    private static String str(Type type, long val, CpuState state, MemoryTrace mem) {
        switch (type.getType()) {
            case VOID:
                return "";
            case PTR:
                if (type.getPointee().getType() == DataType.U8 || type.getPointee().getType() == DataType.S8) {
                    return cstr(val, state.instructionCount, mem);
                } else {
                    return ptr(val);
                }
            case STRING:
                return cstr(val, state.instructionCount, mem);
            case U8:
                return Integer.toString(Byte.toUnsignedInt((byte) val));
            case S8:
                Byte.toString((byte) val);
            case U16:
                return Integer.toString(Short.toUnsignedInt((short) val));
            case S16:
                return Short.toString((short) val);
            case U32:
                return Integer.toUnsignedString((int) val);
            case S32:
                return Integer.toString((int) val);
            case U64:
                return Long.toUnsignedString(val);
            case S64:
                return Long.toString(val);
            case STRUCT:
                return "/* struct */";
        }
        throw new AssertionError("this should be unreachable");
    }

    public static String decode(Function function, CpuState state, CpuState nextState, MemoryTrace mem) {
        StringBuilder buf = new StringBuilder(function.getName());
        buf.append('(');
        Prototype prototype = function.getPrototype();
        for (int i = 0; i < prototype.args.size(); i++) {
            Type type = prototype.args.get(i);
            long val = getRegister(state, i);
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(str(type, val, state, mem));
        }
        buf.append(')');
        if (nextState != null) {
            String s = str(prototype.returnType, nextState.rax, state, mem);
            if (s.length() > 0) {
                buf.append(" = ");
                buf.append(s);
            }
        }
        return buf.toString();
    }
}
