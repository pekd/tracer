package org.graalvm.vm.trcview.decode;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.expression.ast.Expression;

public abstract class CallingConvention {
    public abstract int getFixedArgumentCount();

    public abstract Expression getArgument(int i);

    public abstract Expression getReturn();

    public List<Expression> getArguments(Prototype proto) {
        List<Expression> result = new ArrayList<>();
        for (int i = 0, j = 0; i < proto.args.size(); i++) {
            Type arg = proto.args.get(i);
            if (arg.getExpression() == null) {
                result.add(getArgument(j++));
            } else {
                result.add(arg.getExpression());
            }
        }
        return result;
    }
}
