package org.graalvm.vm.trcview.expression.ast;

import java.util.List;

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
            case "getI16":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return ctx.getI16(tmp);
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getU16":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Short.toUnsignedLong(ctx.getI16(tmp));
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getI32":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return ctx.getI32(tmp);
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            case "getU32":
                if (args.size() != 1) {
                    throw new ArityException(1, args.size());
                }
                tmp = args.get(0).evaluate(ctx);
                try {
                    return Integer.toUnsignedLong(ctx.getI32(tmp));
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
                    return ctx.getI64(tmp);
                } catch (MemoryNotMappedException e) {
                    throw new EvaluationException("memory not mapped at 0x" + HexFormatter.tohex(tmp, 16));
                }
            default:
                throw new EvaluationException("not implemented: " + name);
        }
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
}
