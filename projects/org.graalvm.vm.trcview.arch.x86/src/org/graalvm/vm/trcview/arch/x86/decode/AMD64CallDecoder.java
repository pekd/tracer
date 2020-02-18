package org.graalvm.vm.trcview.arch.x86.decode;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.x86.io.AMD64CpuState;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class AMD64CallDecoder extends CallDecoder {
    private static long getRegister(AMD64CpuState state, int reg) {
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

    public static String decode(Function function, AMD64CpuState state, AMD64CpuState nextState, TraceAnalyzer trc) {
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
                val = getRegister(state, arg++);
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
                retval = nextState.rax;
            }
            String s = str(prototype.returnType, retval, nextState, trc);
            if (s.length() > 0) {
                buf.append(" = ");
                buf.append(s);
            }
        }
        return buf.toString();
    }

    @Override
    public String decode(Function function, CpuState state, CpuState nextState, TraceAnalyzer trc) {
        if (!(state instanceof AMD64CpuState) || (nextState != null && !(nextState instanceof AMD64CpuState))) {
            return null;
        }
        return decode(function, (AMD64CpuState) state, (AMD64CpuState) nextState, trc);
    }
}
