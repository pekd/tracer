package org.graalvm.vm.x86.trcview.decode;

import static org.graalvm.vm.x86.trcview.decode.DecoderUtils.cstr;
import static org.graalvm.vm.x86.trcview.decode.DecoderUtils.ptr;

import org.graalvm.vm.x86.isa.CpuState;
import org.graalvm.vm.x86.trcview.analysis.type.Function;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.analysis.type.Representation;
import org.graalvm.vm.x86.trcview.analysis.type.Type;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;

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

    private static String str(Type type, long val, CpuState state, TraceAnalyzer trc) {
        Representation repr = type.getRepresentation();
        switch (type.getType()) {
            case VOID:
                return "";
            case PTR:
                if (repr == Representation.STRING) {
                    return cstr(val, state.instructionCount, trc);
                } else {
                    return ptr(val);
                }
            case STRING:
                return cstr(val, state.instructionCount, trc);
            case U8:
                switch (repr) {
                    case CHAR:
                        return "'" + DecoderUtils.encode(Byte.toUnsignedInt((byte) val)) + "'";
                    default:
                    case DEC:
                        return Integer.toString(Byte.toUnsignedInt((byte) val));
                    case HEX:
                        return "0x" + Integer.toHexString(Byte.toUnsignedInt((byte) val));
                }
            case S8:
                switch (repr) {
                    case CHAR:
                        return "'" + DecoderUtils.encode(Byte.toUnsignedInt((byte) val)) + "'";
                    default:
                    case DEC:
                        return Byte.toString((byte) val);
                    case HEX:
                        return Integer.toHexString(Byte.toUnsignedInt((byte) val));
                }
            case U16:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return Integer.toString(Short.toUnsignedInt((short) val));
                        }
                    default:
                    case DEC:
                        return Integer.toString(Short.toUnsignedInt((short) val));
                    case HEX:
                        return "0x" + Integer.toHexString(Short.toUnsignedInt((short) val));
                }
            case S16:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return Short.toString((short) val);
                        }
                    default:
                    case DEC:
                        return Short.toString((short) val);
                    case HEX:
                        return "0x" + Integer.toHexString(Short.toUnsignedInt((short) val));
                }
            case U32:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return Short.toString((short) val);
                        }
                    default:
                    case DEC:
                        return Integer.toUnsignedString((int) val);
                    case HEX:
                        return "0x" + Integer.toUnsignedString((int) val, 16);
                }
            case S32:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return Short.toString((short) val);
                        }
                    default:
                    case DEC:
                        return Integer.toString((int) val);
                    case HEX:
                        return "0x" + Integer.toUnsignedString((int) val, 16);
                }
            case U64:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return Short.toString((short) val);
                        }
                    default:
                    case DEC:
                        return Long.toUnsignedString(val);
                    case HEX:
                        return "0x" + Long.toUnsignedString(val, 16);
                }
            case S64:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return Short.toString((short) val);
                        }
                    default:
                    case DEC:
                        return Long.toString(val);
                    case HEX:
                        return "0x" + Long.toUnsignedString(val, 16);
                }
            case STRUCT:
                return "/* struct */";
        }
        throw new AssertionError("this should be unreachable");
    }

    public static String decode(Function function, CpuState state, CpuState nextState, TraceAnalyzer trc) {
        StringBuilder buf = new StringBuilder(function.getName());
        buf.append('(');
        Prototype prototype = function.getPrototype();
        for (int i = 0; i < prototype.args.size(); i++) {
            Type type = prototype.args.get(i);
            long val = getRegister(state, i);
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(str(type, val, state, trc));
        }
        buf.append(')');
        if (nextState != null) {
            String s = str(prototype.returnType, nextState.rax, nextState, trc);
            if (s.length() > 0) {
                buf.append(" = ");
                buf.append(s);
            }
        }
        return buf.toString();
    }
}
