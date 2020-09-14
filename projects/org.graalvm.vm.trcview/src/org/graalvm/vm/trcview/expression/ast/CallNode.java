package org.graalvm.vm.trcview.expression.ast;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.expression.ArityException;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.util.HexFormatter;

public class CallNode extends Expression {
    public final String name;
    public final List<Expression> args;

    public CallNode(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        long tmp;
        switch (name) {
            case "if":
                if (args.size() != 3) {
                    throw new ArityException(3, args.size());
                }
                long value = args.get(0).evaluate(ctx);
                if (value != 0) {
                    return args.get(1).evaluate(ctx);
                } else {
                    return args.get(2).evaluate(ctx);
                }
            case "getI8":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return ctx.getI8(tmp);
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getU8":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Byte.toUnsignedLong(ctx.getI8(tmp));
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getI16L":
            case "getI16LE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return ctx.getI16(tmp);
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getU16L":
            case "getU16LE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Short.toUnsignedLong(ctx.getI16(tmp));
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getI32L":
            case "getI32LE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return ctx.getI32(tmp);
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getU32L":
            case "getU32LE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Integer.toUnsignedLong(ctx.getI32(tmp));
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getI64L":
            case "getU64L":
            case "getI64LE":
            case "getU64LE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return ctx.getI64(tmp);
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getI16B":
            case "getI16BE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Short.reverseBytes(ctx.getI16(tmp));
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getU16B":
            case "getU16BE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Short.toUnsignedLong(Short.reverseBytes(ctx.getI16(tmp)));
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getI32B":
            case "getI32BE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Integer.reverseBytes(ctx.getI32(tmp));
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getU32B":
            case "getU32BE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Integer.toUnsignedLong(Integer.reverseBytes(ctx.getI32(tmp)));
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getI64B":
            case "getU64B":
            case "getI64BE":
            case "getU64BE":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Long.reverseBytes(ctx.getI64(tmp));
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "bswap16":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                return Short.toUnsignedLong(Short.reverseBytes((short) tmp));
            case "bswap32":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                return Integer.toUnsignedLong(Integer.reverseBytes((int) tmp));
            case "bswap64":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                return Long.reverseBytes(tmp);
            case "getI16":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    if (ctx.trc.getArchitecture().getFormat().be) {
                        return Short.reverseBytes(ctx.getI16(tmp));
                    } else {
                        return ctx.getI16(tmp);
                    }
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getI32":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    if (ctx.trc.getArchitecture().getFormat().be) {
                        return Integer.reverseBytes(ctx.getI32(tmp));
                    } else {
                        return ctx.getI32(tmp);
                    }
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getU16":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    if (ctx.trc.getArchitecture().getFormat().be) {
                        return Short.toUnsignedLong(Short.reverseBytes(ctx.getI16(tmp)));
                    } else {
                        return Short.toUnsignedLong(ctx.getI16(tmp));
                    }
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getU32":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    if (ctx.trc.getArchitecture().getFormat().be) {
                        return Integer.toUnsignedLong(Integer.reverseBytes(ctx.getI32(tmp)));
                    } else {
                        return Integer.toUnsignedLong(ctx.getI32(tmp));
                    }
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getI64":
            case "getU64":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    if (ctx.trc.getArchitecture().getFormat().be) {
                        return Long.reverseBytes(ctx.getI64(tmp));
                    } else {
                        return ctx.getI64(tmp);
                    }
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getMem":
            case "getUMem": {
                if (args.size() != 2) {
                    throw new ArityException(2, args.size());
                }
                long addr = args.get(0).evaluate(ctx);
                long size = args.get(1).evaluate(ctx);
                if (size < 1 || size > 8) {
                    throw new EvaluationException("invalid data size: " + size);
                }
                try {
                    if (ctx.trc.getArchitecture().getFormat().be) {
                        switch ((int) size) {
                            case 1:
                                return Byte.toUnsignedLong(ctx.getI8(addr));
                            case 2:
                                return Short.toUnsignedLong(Short.reverseBytes(ctx.getI16(addr)));
                            case 4:
                                return Integer.toUnsignedLong(Integer.reverseBytes(ctx.getI32(addr)));
                            case 8:
                                return Long.reverseBytes(ctx.getI64(addr));
                            default:
                                throw new EvaluationException("invalid data size: " + size);
                        }
                    } else {
                        switch ((int) size) {
                            case 1:
                                return Byte.toUnsignedLong(ctx.getI8(addr));
                            case 2:
                                return Short.toUnsignedLong(ctx.getI16(addr));
                            case 4:
                                return Integer.toUnsignedLong(ctx.getI32(addr));
                            case 8:
                                return ctx.getI64(addr);
                            default:
                                throw new EvaluationException("invalid data size: " + size);
                        }
                    }
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(addr, 16));
                }
            }
            default:
                throw new EvaluationException("not implemented: " + name);
        }
    }

    @Override
    public Expression materialize(Map<String, Long> vars) {
        return new CallNode(name, args.stream().map(x -> x.materialize(vars)).collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(name);
        buf.append('(');
        boolean first = true;
        for (Expression arg : args) {
            if (!first) {
                buf.append(", ");
            } else {
                first = false;
            }
            buf.append(arg);
        }
        buf.append(')');
        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof CallNode)) {
            return false;
        }
        CallNode n = (CallNode) o;
        return name.equals(n.name) && args.equals(n.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, args);
    }
}
