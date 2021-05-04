package org.graalvm.vm.trcview.decode;

import static org.graalvm.vm.util.HexFormatter.tohex;
import static org.graalvm.vm.util.OctFormatter.tooct;

import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;

public class DecoderUtils {
    public static final int STRING_MAXLEN = 50;

    private static String hex(long x) {
        return tohex(x, 1);
    }

    private static String oct(long x) {
        return tooct(x, 1);
    }

    public static String ptr(long x) {
        return ptr(x, false);
    }

    public static String ptr(long x, StepFormat format) {
        return ptr(x, format.numberfmt == StepFormat.NUMBERFMT_OCT);
    }

    public static String ptr(long x, TraceAnalyzer trc) {
        if (trc == null) {
            return ptr(x, false);
        } else {
            return ptr(x, trc.getArchitecture().getFormat());
        }
    }

    public static String ptr(long x, boolean oct) {
        if (x == 0) {
            return "NULL";
        } else if (oct) {
            return "0" + oct(x);
        } else {
            return "0x" + hex(x);
        }
    }

    public static String encode(int b) {
        switch (b) {
            case 0:
                return "\\0";
            case '"':
                return "\\\"";
            case '\'':
                return "\\\'";
            case '\\':
                return "\\\\";
            case '\r':
                return "\\r";
            case '\n':
                return "\\n";
            case '\t':
                return "\\t";
            case '\f':
                return "\\f";
            case '\b':
                return "\\b";
            default:
                if (b < 0x20 || b > Character.MAX_VALUE) {
                    return "\\x" + HexFormatter.tohex(Integer.toUnsignedLong(b), 2);
                } else {
                    return Character.toString((char) b);
                }
        }
    }

    public static String encode(byte b) {
        switch (Byte.toUnsignedInt(b)) {
            case 0:
                return "\\0";
            case '"':
                return "\\\"";
            case '\'':
                return "\\\'";
            case '\\':
                return "\\\\";
            case '\r':
                return "\\r";
            case '\n':
                return "\\n";
            case '\t':
                return "\\t";
            case '\f':
                return "\\f";
            case '\b':
                return "\\b";
            default:
                if (b < 0x20 || b >= 0x7f) {
                    return "\\x" + HexFormatter.tohex(Byte.toUnsignedInt(b), 2);
                } else {
                    return Character.toString((char) Byte.toUnsignedInt(b));
                }
        }
    }

    public static String str(String s) {
        if (s == null) {
            return "NULL";
        }
        StringBuilder buf = new StringBuilder();
        for (char c : s.toCharArray()) {
            buf.append(encode(c));
        }
        return '"' + buf.toString() + '"';
    }

    public static String cstr(long addr, long insn, TraceAnalyzer trc) {
        return cstr(addr, insn, trc, STRING_MAXLEN);
    }

    public static String cstr(long addr, long insn, TraceAnalyzer trc, int maxlen) {
        if (addr == 0) {
            try {
                // check if address 0 is mapped
                trc.getI8(addr, insn);
            } catch (MemoryNotMappedException e) {
                return "NULL";
            }
        }
        try {
            StringBuilder buf = new StringBuilder();
            long ptr = addr;
            for (int i = 0; true; i++) {
                int b = Byte.toUnsignedInt(trc.getI8(ptr++, insn));
                if (b == 0) {
                    return "\"" + buf + "\"";
                }
                buf.append(encode(b));
                if (i >= maxlen) {
                    return "\"" + buf + "\"...";
                }
            }
        } catch (MemoryNotMappedException e) {
            return ptr(addr, trc);
        }
    }

    public static String mem(long addr, long length, long insn, TraceAnalyzer trc) {
        if (addr == 0) {
            return "NULL";
        }
        try {
            StringBuilder buf = new StringBuilder();
            long ptr = addr;
            for (int i = 0; i < length; i++) {
                int b = Byte.toUnsignedInt(trc.getI8(ptr++, insn));
                buf.append(encode(b));
                if (i >= STRING_MAXLEN) {
                    return "\"" + buf + "\"...";
                }
            }
            return "\"" + buf + "\"";
        } catch (MemoryNotMappedException e) {
            return ptr(addr, trc);
        }
    }

    public static ExpressionContext getExpressionContext(CpuState state, TraceAnalyzer trc) {
        return getExpressionContext(state, trc, null);
    }

    public static ExpressionContext getExpressionContext(CpuState state, TraceAnalyzer trc, Map<String, Long> vars) {
        Map<String, Long> constants = new HashMap<>();
        if (vars != null) {
            constants.putAll(vars);
        }
        if (state instanceof StepEvent) {
            byte[] code = ((StepEvent) state).getMachinecode();
            constants.put("codelen", (long) code.length);
            for (int i = 0; i < code.length; i++) {
                constants.put("machinecode_" + i, Byte.toUnsignedLong(code[i]));
            }
        }
        return new ExpressionContext(state, trc, constants);
    }

    public static String str(Type type, long val, TraceAnalyzer trc) {
        return str(type, val, null, trc);
    }

    private static long truncptr(long ptr, TraceAnalyzer trc) {
        ArchitectureTypeInfo info = trc.getArchitecture().getTypeInfo();
        if (info != null) {
            return truncptr(ptr, info.getPointerSize());
        } else {
            return ptr;
        }
    }

    private static long truncptr(long ptr, int size) {
        switch (size) {
            case 1:
                return ptr & 0xFF;
            case 2:
                return ptr & 0xFFFF;
            case 4:
                return ptr & 0xFFFFFFFFL;
            case 8:
            default:
                return ptr;
        }
    }

    public static long truncate(Type type, long value) {
        switch (type.getType()) {
            case PTR:
            case STRING:
                return truncptr(value, (int) type.getElementSize());
            case U8:
            case S8:
                return value & 0xFF;
            case U16:
            case S16:
            case FX16:
                return value & 0xFFFF;
            case U32:
            case S32:
            case FX32:
            case F32:
                return value & 0xFFFFFFFFL;
            default:
                return value;
        }
    }

    public static String str(Type type, long val, CpuState state, TraceAnalyzer trc) {
        Representation repr = type.getRepresentation();
        switch (type.getType()) {
            case VOID:
                return "";
            case PTR:
                if (state != null && trc != null && repr == Representation.STRING) {
                    return cstr(truncptr(val, trc), state.getStep(), trc);
                } else {
                    return ptr(truncptr(val, trc), trc);
                }
            case STRING:
                if (state != null && trc != null) {
                    return cstr(truncptr(val, trc), state.getStep(), trc);
                } else {
                    return ptr(truncptr(val, trc), trc);
                }
            case U8:
                switch (repr) {
                    case CHAR:
                        return "'" + DecoderUtils.encode((byte) val) + "'";
                    default:
                    case DEC:
                        return Integer.toString(Byte.toUnsignedInt((byte) val));
                    case OCT:
                        return "0" + Integer.toOctalString(Byte.toUnsignedInt((byte) val));
                    case HEX:
                        return "0x" + Integer.toHexString(Byte.toUnsignedInt((byte) val));
                }
            case S8:
                switch (repr) {
                    case CHAR:
                        return "'" + DecoderUtils.encode((byte) val) + "'";
                    default:
                    case DEC:
                        return Byte.toString((byte) val);
                    case OCT:
                        return "0" + Integer.toOctalString(Byte.toUnsignedInt((byte) val));
                    case HEX:
                        return "0x" + Integer.toHexString(Byte.toUnsignedInt((byte) val));
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
                    case OCT:
                        return "0" + Integer.toOctalString(Short.toUnsignedInt((short) val));
                    case HEX:
                        return "0x" + Integer.toHexString(Short.toUnsignedInt((short) val));
                    case RAD50:
                        return "\\" + Radix50.decode((short) val) + "\\";
                    case FX16:
                        return "FX16_CONST(" + Fx16.toDouble((short) val) + ")";
                }
            case S16:
            case FX16:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return "'" + DecoderUtils.encode((int) ((val >> 8) & 0xFF)) + DecoderUtils.encode((int) (val & 0xFF)) + "'";
                        }
                    default:
                    case DEC:
                        return Short.toString((short) val);
                    case OCT:
                        return "0" + Integer.toOctalString(Short.toUnsignedInt((short) val));
                    case HEX:
                        return "0x" + Integer.toHexString(Short.toUnsignedInt((short) val));
                    case RAD50:
                        return "\\" + Radix50.decode((short) val) + "\\";
                    case FX16:
                        return "FX16_CONST(" + Fx16.toDouble((short) val) + ")";
                }
            case U32:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return "'" + DecoderUtils.encode((int) ((val >> 8) & 0xFF)) + DecoderUtils.encode((int) (val & 0xFF)) + "'";
                        }
                    default:
                    case DEC:
                        return Integer.toUnsignedString((int) val);
                    case OCT:
                        return "0" + Integer.toUnsignedString((int) val, 8);
                    case HEX:
                        return "0x" + Integer.toUnsignedString((int) val, 16);
                    case RAD50:
                        return "\\" + Radix50.decode((short) val) + Radix50.decode((short) (val >> 16)) + "\\";
                    case FX32:
                        return "FX32_CONST(" + Fx32.toDouble((int) val) + ")";
                    case FLOAT:
                        return Float.intBitsToFloat((int) val) + "f";
                }
            case S32:
            case FX32:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return "'" + DecoderUtils.encode((int) ((val >> 24) & 0xFF)) + DecoderUtils.encode((int) (val >> 16) & 0xFF) + DecoderUtils.encode((int) (val >> 8) & 0xFF) +
                                            DecoderUtils.encode((int) (val & 0xFF)) + "'";
                        }
                    default:
                    case DEC:
                        return Integer.toString((int) val);
                    case OCT:
                        return "0" + Integer.toOctalString((int) val);
                    case HEX:
                        return "0x" + Integer.toUnsignedString((int) val, 16);
                    case RAD50:
                        return "\\" + Radix50.decode((short) val) + Radix50.decode((short) (val >> 16)) + "\\";
                    case FX32:
                        return "FX32_CONST(" + Fx32.toDouble((int) val) + ")";
                    case FLOAT:
                        return Float.intBitsToFloat((int) val) + "f";
                }
            case U64:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return "'" + DecoderUtils.encode((int) ((val >> 24) & 0xFF)) + DecoderUtils.encode((int) (val >> 16) & 0xFF) + DecoderUtils.encode((int) (val >> 8) & 0xFF) +
                                            DecoderUtils.encode((int) (val & 0xFF)) + "'";
                        }
                    default:
                    case DEC:
                        return Long.toUnsignedString(val);
                    case OCT:
                        return "0" + Long.toUnsignedString(val, 8);
                    case HEX:
                        return "0x" + Long.toUnsignedString(val, 16);
                    case FLOAT:
                        return Double.toString(Double.longBitsToDouble(val));
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
                    case OCT:
                        return "0" + Long.toString(val, 8);
                    case HEX:
                        return "0x" + Long.toUnsignedString(val, 16);
                    case FLOAT:
                        return Double.toString(Double.longBitsToDouble(val));
                }
            case F32:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return "'" + DecoderUtils.encode((int) ((val >> 24) & 0xFF)) + DecoderUtils.encode((int) (val >> 16) & 0xFF) + DecoderUtils.encode((int) (val >> 8) & 0xFF) +
                                            DecoderUtils.encode((int) (val & 0xFF)) + "'";
                        }
                    case DEC:
                        return Integer.toString((int) val);
                    case OCT:
                        return "0" + Integer.toOctalString((int) val);
                    case HEX:
                        return "0x" + Integer.toUnsignedString((int) val, 16);
                    case RAD50:
                        return "\\" + Radix50.decode((short) val) + Radix50.decode((short) (val >> 16)) + "\\";
                    case FX32:
                        return "FX32_CONST(" + Fx32.toDouble((int) val) + ")";
                    default:
                    case FLOAT:
                        return Float.intBitsToFloat((int) val) + "f";
                }
            case F64:
                switch (repr) {
                    case CHAR:
                        if (Short.toUnsignedInt((short) val) < 0x100) {
                            return "'" + DecoderUtils.encode(Short.toUnsignedInt((short) val)) + "'";
                        } else {
                            return Short.toString((short) val);
                        }
                    case DEC:
                        return Long.toString(val);
                    case OCT:
                        return "0" + Long.toString(val, 8);
                    case HEX:
                        return "0x" + Long.toUnsignedString(val, 16);
                    default:
                    case FLOAT:
                        return Double.toString(Double.longBitsToDouble(val));
                }
            case STRUCT:
                return "/* struct */";
        }
        throw new AssertionError("this should be unreachable");
    }
}
