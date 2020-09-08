package org.graalvm.vm.trcview.decode;

import static org.graalvm.vm.trcview.decode.DecoderUtils.str;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class GenericSyscallDecoder extends SyscallDecoder {
    @Override
    public String decode(CpuState state, CpuState next, TraceAnalyzer trc) {
        ABI abi = trc.getABI();
        Expression syscallIdExpr = abi.getSyscallIdExpression();
        if (syscallIdExpr == null) {
            return null;
        }

        ExpressionContext ctx = DecoderUtils.getExpressionContext(state, trc);
        long id;
        try {
            id = syscallIdExpr.evaluate(ctx);
        } catch (EvaluationException e) {
            return null;
        }

        Function function = abi.getSyscall(id);
        if (function == null) {
            return null;
        }

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
                int argCount = abi.getSyscallArgumentCount();
                if (argCount > i) {
                    try {
                        long val = abi.getSyscallArgument(i).evaluate(ctx);
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
        if (next != null) {
            String s;
            if (prototype.returnType.getExpression() != null) {
                try {
                    ctx = DecoderUtils.getExpressionContext(next, trc);
                    long retval = prototype.returnType.getExpression().evaluate(ctx);
                    s = str(prototype.returnType, retval, next, trc);
                } catch (EvaluationException e) {
                    s = "?";
                }
            } else if (prototype.returnType.getType() == DataType.VOID) {
                s = "";
            } else if (abi != null && abi.getSyscallReturnExpression() != null) {
                try {
                    ctx = DecoderUtils.getExpressionContext(next, trc);
                    long retval = abi.getSyscallReturnExpression().evaluate(ctx);
                    s = str(prototype.returnType, retval, next, trc);
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

    @Override
    public String decodeResult(int sc, CpuState state, TraceAnalyzer trc) {
        return null;
    }

    @Override
    public String decode(CpuState state, TraceAnalyzer trc) {
        return decode(state, null, trc);
    }
}
