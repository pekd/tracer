package org.graalvm.vm.trcview.arch.custom;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.arch.custom.analysis.CustomAnalyzer;
import org.graalvm.vm.trcview.arch.custom.io.CustomCpuState;
import org.graalvm.vm.trcview.arch.custom.io.CustomStepEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.script.SymbolTable;
import org.graalvm.vm.trcview.script.ast.Intrinsic;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.JavaPointer;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.rt.Record;
import org.graalvm.vm.trcview.script.type.ArrayType;
import org.graalvm.vm.trcview.script.type.BasicType;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.trcview.script.type.Type;

public class Intrinsics {
    // Java 1.8 compatibility ... (essentially List.of from Java 9)
    private static List<Type> list(Type... t) {
        List<Type> list = new ArrayList<>();
        for (Type type : t) {
            list.add(type);
        }
        return list;
    }

    public static class Alloca extends Intrinsic {
        public Alloca() {
            super("alloca", new PointerType(new PrimitiveType(BasicType.VOID)), list(new PrimitiveType(BasicType.LONG, true)));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            throw new UnsupportedOperationException("invalid integer call to alloca");
        }

        @Override
        public Pointer executePointer(Context ctx, Object... args) {
            Type pointee = new PrimitiveType(BasicType.CHAR);
            Record data = new Record(new ArrayType(pointee, (long) args[0]));
            return new Pointer(pointee, 0, data);
        }
    }

    public static class Strlen extends Intrinsic {
        public Strlen() {
            super("strlen", new PrimitiveType(BasicType.LONG, true), list(new PointerType(new PrimitiveType(BasicType.CHAR))));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Pointer ptr = (Pointer) args[0];
            return ptr.cstr().length();
        }
    }

    public static class Strcmp extends Intrinsic {
        public Strcmp() {
            super("strcmp", new PrimitiveType(BasicType.INT), list(new PointerType(new PrimitiveType(BasicType.CHAR)), new PointerType(new PrimitiveType(BasicType.CHAR))));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Pointer ptr1 = (Pointer) args[0];
            Pointer ptr2 = (Pointer) args[1];
            String s1 = ptr1.cstr();
            String s2 = ptr2.cstr();
            return s1.compareTo(s2);
        }
    }

    public static class SetName extends Intrinsic {
        private final CustomArchitecture arch;

        public SetName(CustomArchitecture arch) {
            super("set_name", new PrimitiveType(BasicType.VOID), list(new PointerType(new PrimitiveType(BasicType.CHAR))));
            this.arch = arch;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Pointer ptr = (Pointer) args[0];
            arch.setName(ptr.cstr());
            return 0;
        }
    }

    public static class SetDescription extends Intrinsic {
        private final CustomArchitecture arch;

        public SetDescription(CustomArchitecture arch) {
            super("set_description", new PrimitiveType(BasicType.VOID), list(new PointerType(new PrimitiveType(BasicType.CHAR))));
            this.arch = arch;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Pointer ptr = (Pointer) args[0];
            arch.setDescription(ptr.cstr());
            return 0;
        }
    }

    public static class SetStepType extends Intrinsic {
        private final CustomArchitecture arch;

        public SetStepType(CustomArchitecture arch) {
            super("set_step_type", new PrimitiveType(BasicType.VOID), list(new PointerType(new PrimitiveType(BasicType.CHAR)), new PointerType(new PrimitiveType(BasicType.CHAR)),
                            new PointerType(new PrimitiveType(BasicType.CHAR)), new PointerType(new PrimitiveType(BasicType.CHAR))));
            this.arch = arch;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            String name = ((Pointer) args[0]).cstr();
            String stateName = ((Pointer) args[1]).cstr();
            String insnName = ((Pointer) args[2]).cstr();
            String insnLength = ((Pointer) args[3]).cstr();
            if (arch.types.hasType(name)) {
                Type type = arch.types.get(name);
                arch.setStepType(type);
                arch.setStateName(stateName);
                arch.setInsnName(insnName);
                arch.setInsnLength(insnLength);
                return 0;
            } else {
                return 1;
            }
        }
    }

    public static class SetStateType extends Intrinsic {
        private final CustomArchitecture arch;

        public SetStateType(CustomArchitecture arch) {
            super("set_state_type", new PrimitiveType(BasicType.VOID),
                            list(new PointerType(new PrimitiveType(BasicType.CHAR)), new PointerType(new PrimitiveType(BasicType.CHAR)), new PointerType(new PrimitiveType(BasicType.CHAR))));
            this.arch = arch;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            String name = ((Pointer) args[0]).cstr();
            String pcName = ((Pointer) args[1]).cstr();
            String stepName = ((Pointer) args[2]).cstr();
            if (arch.types.hasType(name)) {
                Type type = arch.types.get(name);
                arch.setStateType(type);
                arch.setPCName(pcName);
                arch.setStepName(stepName);
                return 0;
            } else {
                return 1;
            }
        }
    }

    public static class IsStepEvent extends Intrinsic {
        public IsStepEvent() {
            super("is_step_event", new PrimitiveType(BasicType.INT), list(new PointerType(new PrimitiveType(BasicType.VOID))));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Event evt = ((JavaPointer) args[0]).getObject();
            return evt instanceof StepEvent ? 1 : 0;
        }
    }

    public static class GetField extends Intrinsic {
        public GetField() {
            super("get_field", new PrimitiveType(BasicType.VOID), list(new PointerType(new PrimitiveType(BasicType.VOID)), new PointerType(new PrimitiveType(BasicType.CHAR))));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            StepEvent evt = ((JavaPointer) args[0]).getObject();
            String field = ((Pointer) args[1]).cstr();
            switch (field) {
                case "pc":
                    return evt.getPC();
                case "step":
                    return evt.getStep();
                case "tid":
                    return evt.getTid();
                default:
                    return evt.getState().get(field);
            }
        }
    }

    public static class CreateStep extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public CreateStep(CustomAnalyzer analyzer) {
            super("create_step", new PrimitiveType(BasicType.VOID), list(new PointerType(new PrimitiveType(BasicType.VOID))));
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Pointer data = (Pointer) args[0];
            Type type = data.getType();
            if (!(type instanceof Struct)) {
                throw new IllegalArgumentException("not a struct");
            }
            CustomArchitecture arch = analyzer.getArchitecture();
            String pcName = arch.getPCName();
            String stateName = arch.getStateName();
            short id = arch.getId();
            Member statemember = ((Struct) type).getMember(stateName);
            Pointer state = data.add(statemember.type, statemember.offset);
            CustomCpuState cpustate = new CustomCpuState(id, 0, pcName, state);
            Event evt = new CustomStepEvent(id, 0, cpustate);
            analyzer.createEvent(evt);
            return 0;
        }
    }

    public static void register(SymbolTable symtab, CustomArchitecture arch) {
        symtab.define(new Alloca());
        symtab.define(new Strlen());
        symtab.define(new Strcmp());
        symtab.define(new SetName(arch));
        symtab.define(new SetDescription(arch));
        symtab.define(new SetStepType(arch));
        symtab.define(new SetStateType(arch));
        symtab.define(new IsStepEvent());
        symtab.define(new GetField());
    }

    public static List<Intrinsic> getAnalyzerIntrinsics(CustomAnalyzer analyzer) {
        List<Intrinsic> result = new ArrayList<>();
        result.add(new CreateStep(analyzer));
        return result;
    }
}
