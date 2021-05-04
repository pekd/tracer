package org.graalvm.vm.trcview.decode;

import static org.graalvm.vm.trcview.decode.DecoderUtils.str;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public abstract class CallDecoder {
    @SuppressWarnings("unused")
    public long getArgument(CpuState state, int id, Prototype prototype, TraceAnalyzer trc) {
        return getArgument(state, id, prototype);
    }

    @SuppressWarnings("unused")
    public long getArgument(CpuState state, int id, Prototype prototype) {
        return 0;
    }

    @SuppressWarnings("unused")
    public long getReturnValue(CpuState state, Type type, TraceAnalyzer trc) {
        return getReturnValue(state, type);
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
        ExpressionContext ctx = DecoderUtils.getExpressionContext(state, trc);
        for (int i = 0, arg = 0; i < prototype.args.size(); i++) {
            Type type = prototype.args.get(i);
            long val;
            if (type.getExpression() != null) {
                try {
                    val = type.getExpression().evaluate(ctx);
                } catch (EvaluationException e) {
                    val = 0;
                }
            } else {
                val = getArgument(state, arg++, prototype, trc);
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
                    ctx = DecoderUtils.getExpressionContext(nextState, trc);
                    retval = prototype.returnType.getExpression().evaluate(ctx);
                } catch (EvaluationException e) {
                    retval = 0;
                }
            } else {
                retval = getReturnValue(nextState, prototype.returnType, trc);
            }
            String s = str(prototype.returnType, retval, nextState, trc);
            if (s.length() > 0) {
                buf.append(" = ");
                buf.append(s);
            }
        }

        return buf.toString();
    }

}
