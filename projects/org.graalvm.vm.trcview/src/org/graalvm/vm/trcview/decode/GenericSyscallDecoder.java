package org.graalvm.vm.trcview.decode;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class GenericSyscallDecoder extends SyscallDecoder {
    @Override
    public String decode(CpuState state, CpuState next, TraceAnalyzer trc) {
        ABI abi = trc.getABI();
        if (abi == null) {
            return null;
        }

        Expression syscallIdExpr = abi.getSyscallId();
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

        return CallingConventionDecoder.decode(function, state, next, trc, abi.getSyscall());
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
