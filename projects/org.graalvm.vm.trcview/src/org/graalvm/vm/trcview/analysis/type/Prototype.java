package org.graalvm.vm.trcview.analysis.type;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.graalvm.vm.trcview.expression.ast.Expression;

public class Prototype {
    public final Type returnType;
    public final List<Type> args;
    public final Expression expr;

    public Prototype() {
        this(new Type(DataType.VOID), Collections.emptyList());
    }

    public Prototype(Type returnType) {
        this(returnType, Collections.emptyList());
    }

    public Prototype(Type returnType, Expression expr) {
        this(returnType, Collections.emptyList(), expr);
    }

    public Prototype(Type returnType, List<Type> args) {
        this.returnType = returnType;
        this.args = args;
        this.expr = null;
    }

    public Prototype(Type returnType, List<Type> args, Expression expr) {
        this.returnType = returnType;
        this.args = args;
        this.expr = expr;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Prototype)) {
            return false;
        }

        Prototype p = (Prototype) o;
        return returnType.equals(p.returnType) && args.equals(p.args) && Objects.equals(expr, p.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, args, expr);
    }
}
