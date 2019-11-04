package org.graalvm.vm.x86.el;

import java.text.ParseException;
import java.util.List;

import org.graalvm.vm.x86.ArchitecturalState;
import org.graalvm.vm.x86.el.ast.Expression;
import org.graalvm.vm.x86.el.ast.func.GetI16Node;
import org.graalvm.vm.x86.el.ast.func.GetI32Node;
import org.graalvm.vm.x86.el.ast.func.GetI64Node;
import org.graalvm.vm.x86.el.ast.func.GetI8Node;
import org.graalvm.vm.x86.el.ast.func.IfNode;

public abstract class Call {
    public static Expression create(String name, List<Expression> args, ArchitecturalState state) throws ParseException {
        switch (name) {
            case "if":
                if (args.size() != 3) {
                    throw new ParseException("arity mismatch", 0);
                }
                return new IfNode(args.get(0), args.get(1), args.get(2));
            case "getI8":
                if (args.size() != 1) {
                    throw new ParseException("arity mismatch", 0);
                }
                return new GetI8Node(args.get(0), state);
            case "getI16":
                if (args.size() != 1) {
                    throw new ParseException("arity mismatch", 0);
                }
                return new GetI16Node(args.get(0), state);
            case "getI32":
                if (args.size() != 1) {
                    throw new ParseException("arity mismatch", 0);
                }
                return new GetI32Node(args.get(0), state);
            case "getI64":
                if (args.size() != 1) {
                    throw new ParseException("arity mismatch", 0);
                }
                return new GetI64Node(args.get(0), state);
            default:
                throw new ParseException("unknown function", 0);
        }
    }
}
