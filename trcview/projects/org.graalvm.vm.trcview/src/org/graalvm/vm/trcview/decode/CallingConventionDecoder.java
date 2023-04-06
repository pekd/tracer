package org.graalvm.vm.trcview.decode;

import java.util.List;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.data.ir.MemoryOperand;
import org.graalvm.vm.trcview.data.ir.Operand;
import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.ast.CallNode;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.expression.ast.VariableNode;
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

    public static Operand getLocation(Expression expr, CpuState state, TraceAnalyzer trc) {
        Architecture arch = trc.getArchitecture();
        if (expr instanceof VariableNode) {
            VariableNode v = (VariableNode) expr;
            // register access => RegisterOperand
            int id = arch.getRegisterId(v.name);
            if (id == -1) {
                return null;
            } else {
                return new RegisterOperand(id);
            }
        } else if (expr instanceof CallNode) {
            CallNode c = (CallNode) expr;
            // memory access => MemoryOperand
            switch (c.name) {
                case "getI8":
                case "getI16":
                case "getI32":
                case "getI64":
                case "getU8":
                case "getU16":
                case "getU32":
                case "getU64":
                case "getU16B":
                case "getU32B":
                case "getU64B":
                case "getU16BE":
                case "getU32BE":
                case "getU64BE":
                case "getU16L":
                case "getU32L":
                case "getU64L":
                case "getU16LE":
                case "getU32LE":
                case "getU64LE":
                    ExpressionContext ctx = DecoderUtils.getExpressionContext(state, trc);
                    try {
                        long ea = c.args.get(0).evaluate(ctx);
                        return new MemoryOperand(ea);
                    } catch (EvaluationException e) {
                        return null;
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    public static Operand getArgumentLocation(CallingConvention cc, int i, CpuState state, TraceAnalyzer trc) {
        return getLocation(cc.getArgument(i), state, trc);
    }

    public static Operand getReturnLocation(CallingConvention cc, CpuState state, TraceAnalyzer trc) {
        return getLocation(cc.getReturn(), state, trc);
    }
}
