package org.graalvm.vm.trcview.decode;

import static org.graalvm.vm.trcview.decode.DecoderUtils.cstr;
import static org.graalvm.vm.trcview.decode.DecoderUtils.ptr;

import java.util.List;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public abstract class CallDecoder {
    @SuppressWarnings("unused")
    public long getArgument(CpuState state, int id, List<Type> types) {
        return 0;
    }

    @SuppressWarnings("unused")
    public long getReturnValue(CpuState state, Type type) {
        return 0;
    }

    public String decode(Function function, CpuState state, CpuState nextState, TraceAnalyzer trc) {
        if (state == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder(function.getName());
        buf.append('(');
        Prototype prototype = function.getPrototype();
        for (int i = 0, arg = 0; i < prototype.args.size(); i++) {
            Type type = prototype.args.get(i);
            long val;
            if (type.getExpression() != null) {
                try {
                    ExpressionContext ctx = new ExpressionContext(state, trc);
                    val = type.getExpression().evaluate(ctx);
                } catch (EvaluationException e) {
                    val = 0;
                }
            } else {
                val = getArgument(state, arg++, prototype.args);
            }
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(str(type, val, state, trc));
        }
        buf.append(')');

        if (nextState != null) {
            long retval;
            if (prototype.returnType.getExpression() != null) {
                try {
                    ExpressionContext ctx = new ExpressionContext(nextState, trc);
                    retval = prototype.returnType.getExpression().evaluate(ctx);
                } catch (EvaluationException e) {
                    retval = 0;
                }
            } else {
                retval = getReturnValue(nextState, prototype.returnType);
            }
            String s = str(prototype.returnType, retval, nextState, trc);
            if (s.length() > 0) {
                buf.append(" = ");
                buf.append(s);
            }
        }

        return buf.toString();
    }

    protected static String str(Type type, long val, CpuState state, TraceAnalyzer trc) {
        Representation repr = type.getRepresentation();
        switch (type.getType()) {
            case VOID:
                return "";
            case PTR:
                if (repr == Representation.STRING) {
                    return cstr(val, state.getStep(), trc);
                } else {
                    return ptr(val, trc);
                }
            case STRING:
                return cstr(val, state.getStep(), trc);
            case U8:
                switch (repr) {
                    case CHAR:
                        return "'" + DecoderUtils.encode(Byte.toUnsignedInt((byte) val)) + "'";
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
                        return "'" + DecoderUtils.encode(Byte.toUnsignedInt((byte) val)) + "'";
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
                    case OCT:
                        return "0" + Integer.toOctalString(Short.toUnsignedInt((short) val));
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
                    case OCT:
                        return "0" + Integer.toUnsignedString((int) val, 8);
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
                    case OCT:
                        return "0" + Integer.toOctalString((int) val);
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
                    case OCT:
                        return "0" + Long.toUnsignedString(val, 8);
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
                    case OCT:
                        return "0" + Long.toString(val, 8);
                    case HEX:
                        return "0x" + Long.toUnsignedString(val, 16);
                }
            case STRUCT:
                return "/* struct */";
        }
        throw new AssertionError("this should be unreachable");
    }
}
