package org.graalvm.vm.trcview.analysis.type;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private final String name;
    private final Prototype type;

    public Function(String name, Prototype type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Prototype getPrototype() {
        return type;
    }

    @Override
    public String toString() {
        List<String> args = new ArrayList<>();
        for (int i = 0; i < type.args.size(); i++) {
            String t = type.args.get(i).toString();
            String n = type.names.get(i);
            if (n != null) {
                args.add(t + " " + n);
            } else {
                args.add(t);
            }
        }
        return type.returnType.toString() + " " + name + "(" + String.join(", ", args) + ")";
    }
}
