package org.graalvm.vm.trcview.decode;

import java.util.List;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class CallingConventionDecoder {
    public static String decode(Function function, CpuState state, CpuState nextState, TraceAnalyzer trc, CallingConvention cc) {
        ExpressionContext ctx = DecoderUtils.getExpressionContext(state, trc);
        StringBuilder buf = new StringBuilder(function.getName());

        buf.append('(');
        Prototype prototype = function.getPrototype();
        if (cc == null) {
            for (int i = 0; i < prototype.args.size(); i++) {
                Type type = prototype.args.get(i);
                String strval;
                if (type.getExpression() != null) {
                    try {
                        long val = type.getExpression().evaluate(ctx);
                        strval = DecoderUtils.str(type, val, state, trc);
                    } catch (EvaluationException e) {
                        strval = "?";
                    }
                } else {
                    strval = "?";
                }
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(strval);
            }
        } else {
            List<Expression> args = cc.getArguments(prototype);
            for (int i = 0; i < prototype.args.size(); i++) {
                Type type = prototype.args.get(i);
                String strval;
                Expression expr = args.get(i);
                if (expr != null) {
                    try {
                        long val = expr.evaluate(ctx);
                        strval = DecoderUtils.str(type, val, state, trc);
                    } catch (EvaluationException e) {
                        strval = "?";
                    }
                } else {
                    strval = "?";
                }
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(strval);
            }
        }
        buf.append(')');

        if (nextState != null) {
            String s;
            if (prototype.returnType.getExpression() != null) {
                try {
                    ctx = DecoderUtils.getExpressionContext(nextState, trc);
                    long retval = prototype.returnType.getExpression().evaluate(ctx);
                    s = DecoderUtils.str(prototype.returnType, retval, nextState, trc);
                } catch (EvaluationException e) {
                    s = "?";
                }
            } else if (prototype.returnType.getType() == DataType.VOID) {
                s = "";
            } else if (cc != null && cc.getReturn() != null) {
                try {
                    ctx = DecoderUtils.getExpressionContext(nextState, trc);
                    long retval = cc.getReturn().evaluate(ctx);
                    s = DecoderUtils.str(prototype.returnType, retval, nextState, trc);
                } catch (EvaluationException e) {
                    s = "?";
                }
            } else {
                s = "?";
            }
            if (s.length() > 0) {
                buf.append(" = ");
                buf.append(s);
            }
        }
        return buf.toString();
    }

    public static long get(Prototype prototype, CpuState state, TraceAnalyzer trc, CallingConvention cc, int i) {
        ExpressionContext ctx = DecoderUtils.getExpressionContext(state, trc);
        if (cc == null) {
            Type type = prototype.args.get(i);
            if (type.getExpression() != null) {
                try {
                    long val = type.getExpression().evaluate(ctx);
                    return DecoderUtils.truncate(type, val);
                } catch (EvaluationException e) {
                    return 0;
                }
            } else {
                return 0;
            }
        } else {
            List<Expression> args = cc.getArguments(prototype);
            Type type = prototype.args.get(i);
            Expression expr = args.get(i);
            if (expr != null) {
                try {
                    long val = expr.evaluate(ctx);
                    return DecoderUtils.truncate(type, val);
                } catch (EvaluationException e) {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }
}
