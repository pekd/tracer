package org.graalvm.vm.trcview.decode;

import static org.graalvm.vm.trcview.decode.DecoderUtils.str;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class GenericCallDecoder extends CallDecoder {
    @Override
    public String decode(Function function, CpuState state, CpuState nextState, TraceAnalyzer trc) {
        ABI abi = trc.getABI();
        ExpressionContext ctx = DecoderUtils.getExpressionContext(state, trc);
        StringBuilder buf = new StringBuilder(function.getName());
        buf.append('(');
        Prototype prototype = function.getPrototype();
        for (int i = 0; i < prototype.args.size(); i++) {
            Type type = prototype.args.get(i);
            String strval;
            if (type.getExpression() != null) {
                try {
                    long val = type.getExpression().evaluate(ctx);
                    strval = str(type, val, state, trc);
                } catch (EvaluationException e) {
                    strval = "?";
                }
            } else if (abi != null) {
                int argCount = abi.getCallArgumentCount();
                if (argCount > i) {
                    try {
                        long val = abi.getCallArgument(i).evaluate(ctx);
                        strval = str(type, val, state, trc);
                    } catch (EvaluationException e) {
                        strval = "?";
                    }
                } else {
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
        buf.append(')');
        if (nextState != null) {
            String s;
            if (prototype.returnType.getExpression() != null) {
                try {
                    ctx = DecoderUtils.getExpressionContext(nextState, trc);
                    long retval = prototype.returnType.getExpression().evaluate(ctx);
                    s = str(prototype.returnType, retval, nextState, trc);
                } catch (EvaluationException e) {
                    s = "?";
                }
            } else if (prototype.returnType.getType() == DataType.VOID) {
                s = "";
            } else if (abi != null && abi.getReturnExpression() != null) {
                try {
                    ctx = DecoderUtils.getExpressionContext(nextState, trc);
                    long retval = abi.getReturnExpression().evaluate(ctx);
                    s = str(prototype.returnType, retval, nextState, trc);
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
}
