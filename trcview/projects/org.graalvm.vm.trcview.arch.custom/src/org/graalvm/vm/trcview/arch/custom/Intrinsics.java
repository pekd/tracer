package org.graalvm.vm.trcview.arch.custom;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.arch.custom.analysis.CustomAnalyzer;
import org.graalvm.vm.trcview.arch.custom.io.CustomStepEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.GenericMemoryEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
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
            super("alloca", PointerType.VOIDPTR, list(PrimitiveType.ULONG));
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
            super("strlen", PrimitiveType.ULONG, list(PointerType.CHARPTR));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Pointer ptr = (Pointer) args[0];
            return ptr.cstr().length();
        }
    }

    public static class Strcmp extends Intrinsic {
        public Strcmp() {
            super("strcmp", PrimitiveType.INT, list(PointerType.CHARPTR, PointerType.CHARPTR));
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

    public static class Strcpy extends Intrinsic {
        public Strcpy() {
            super("strcpy", PrimitiveType.INT, list(PointerType.CHARPTR, PointerType.CHARPTR));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            PrimitiveType chr = new PrimitiveType(BasicType.CHAR);
            Pointer dst = (Pointer) args[0];
            Pointer src = (Pointer) args[1];
            while (src.getI8() != 0) {
                dst.setI8(src.getI8());
                src = src.add(chr, 1);
                dst = dst.add(chr, 1);
            }
            dst.setI8((byte) 0);
            return 0;
        }
    }

    public static class Strcat extends Intrinsic {
        public Strcat() {
            super("strcat", PrimitiveType.INT, list(PointerType.CHARPTR, PointerType.CHARPTR));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            PrimitiveType chr = new PrimitiveType(BasicType.CHAR);
            Pointer dst = (Pointer) args[0];
            Pointer src = (Pointer) args[1];

            while (dst.getI8() != 0) {
                dst = dst.add(chr, 1);
            }

            while (src.getI8() != 0) {
                dst.setI8(src.getI8());
                src = src.add(chr, 1);
                dst = dst.add(chr, 1);
            }
            dst.setI8((byte) 0);

            return 0;
        }
    }

    public static class Sprintf extends Intrinsic {
        public Sprintf() {
            super("sprintf", PrimitiveType.VOID, list(PointerType.CHARPTR, PointerType.CHARPTR));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Pointer dst = (Pointer) args[0];
            Pointer src = (Pointer) args[1];

            Object[] vararg = new Object[args.length - 2];
            for (int i = 0; i < vararg.length; i++) {
                if (args[i + 2] instanceof Pointer) {
                    vararg[i] = ((Pointer) args[i + 2]).cstr();
                } else {
                    vararg[i] = args[i + 2];
                }
            }

            String result = String.format(src.cstr(), vararg);

            int i;
            for (i = 0; i < result.length(); i++) {
                dst.setI8(i, (byte) result.charAt(i));
            }
            dst.setI8(i++, (byte) 0);

            return 0;
        }
    }

    public static class SetName extends Intrinsic {
        private final CustomArchitecture arch;

        public SetName(CustomArchitecture arch) {
            super("set_name", PrimitiveType.VOID, list(PointerType.CHARPTR));
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
            super("set_description", PrimitiveType.VOID, list(PointerType.CHARPTR));
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
            super("set_step_type", PrimitiveType.VOID, list(PointerType.CHARPTR, PointerType.CHARPTR, PointerType.CHARPTR, PointerType.CHARPTR));
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
            super("set_state_type", PrimitiveType.VOID, list(PointerType.CHARPTR, PointerType.CHARPTR, PointerType.CHARPTR));
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

    public static class SetFormat extends Intrinsic {
        private final CustomArchitecture arch;

        public SetFormat(CustomArchitecture arch) {
            super("set_format", PrimitiveType.VOID, list(PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT));
            this.arch = arch;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            int numberfmt = (int) (long) args[0];
            int addrwidth = (int) (long) args[1];
            int wordwidth = (int) (long) args[2];
            int machinecodesz = (int) (long) args[3];
            boolean be = (long) args[4] != 0;
            StepFormat fmt = new StepFormat(numberfmt, addrwidth, wordwidth, machinecodesz, be);
            arch.setFormat(fmt);
            return 0;
        }
    }

    public static class IsStepEvent extends Intrinsic {
        public IsStepEvent() {
            super("is_step_event", PrimitiveType.INT, list(PointerType.VOIDPTR));
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Event evt = ((JavaPointer) args[0]).getObject();
            return evt instanceof StepEvent ? 1 : 0;
        }
    }

    public static class GetField extends Intrinsic {
        public GetField() {
            super("get_field", PrimitiveType.VOID, list(PointerType.VOIDPTR, PointerType.CHARPTR));
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

    public static class GetI8 extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public GetI8(CustomAnalyzer analyzer) {
            super("getI8", PrimitiveType.CHAR, list(PrimitiveType.ULONG));
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            long addr = (long) args[0];
            long step = analyzer.getCurrentStep();
            try {
                return analyzer.getMemoryTrace().getByte(addr, step);
            } catch (MemoryNotMappedException e) {
                return 0;
            }
        }
    }

    public static class GetI16 extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public GetI16(CustomAnalyzer analyzer) {
            super("getI16", PrimitiveType.SHORT, list(PrimitiveType.ULONG));
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            long addr = (long) args[0];
            long step = analyzer.getCurrentStep();
            try {
                return analyzer.getMemoryTrace().getShort(addr, step);
            } catch (MemoryNotMappedException e) {
                return 0;
            }
        }
    }

    public static class GetI32 extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public GetI32(CustomAnalyzer analyzer) {
            super("getI32", PrimitiveType.INT, list(PrimitiveType.ULONG));
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            long addr = (long) args[0];
            long step = analyzer.getCurrentStep();
            try {
                return analyzer.getMemoryTrace().getInt(addr, step);
            } catch (MemoryNotMappedException e) {
                return 0;
            }
        }
    }

    public static class GetI64 extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public GetI64(CustomAnalyzer analyzer) {
            super("getI64", PrimitiveType.LONG, list(PrimitiveType.ULONG));
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            long addr = (long) args[0];
            long step = analyzer.getCurrentStep();
            try {
                return analyzer.getMemoryTrace().getWord(addr, step);
            } catch (MemoryNotMappedException e) {
                return 0;
            }
        }
    }

    public static class CreateStep extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public CreateStep(CustomAnalyzer analyzer) {
            super("create_step", PrimitiveType.VOID, list(PointerType.VOIDPTR));
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
            Member statemember = ((Struct) type).getMember(stateName);
            Pointer state = data.add(statemember.type, statemember.offset);
            long parentStep = analyzer.getCurrentStep();
            Event evt = new CustomStepEvent(analyzer, 0, pcName, data, state, parentStep);
            analyzer.createEvent(evt);
            return 0;
        }
    }

    public static class CreateMmap extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public CreateMmap(CustomAnalyzer analyzer) {
            super("create_mmap", PrimitiveType.VOID, list(PrimitiveType.ULONG, PrimitiveType.ULONG, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.ULONG, PrimitiveType.ULONG));
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            long addr = (long) args[0];
            long len = (long) args[1];
            int prot = (int) (long) args[2];
            int flags = (int) (long) args[3];
            int fd = (int) (long) args[4];
            long off = (long) args[5];
            long result = (long) args[6];
            MmapEvent evt = new MmapEvent(0, addr, len, prot, flags, fd, off, null, result, null);
            analyzer.createEvent(evt);
            return 0;
        }
    }

    public static class CreateWrite extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public CreateWrite(CustomAnalyzer analyzer) {
            super("create_write", PrimitiveType.VOID, list(PrimitiveType.UCHAR, PrimitiveType.ULONG, PrimitiveType.UCHAR, PrimitiveType.ULONG));
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            boolean be = (long) args[0] != 0;
            long addr = (long) args[1];
            byte len = (byte) (long) args[2];
            long value = (long) args[3];
            MemoryEvent evt = new GenericMemoryEvent(be, 0, addr, len, true, value);
            analyzer.createEvent(evt);
            return 0;
        }
    }

    public static class CreateRead extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public CreateRead(CustomAnalyzer analyzer) {
            super("create_read", PrimitiveType.VOID, list(PrimitiveType.UCHAR, PrimitiveType.ULONG, PrimitiveType.UCHAR, PrimitiveType.ULONG));
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            boolean be = (long) args[0] != 0;
            long addr = (long) args[1];
            byte len = (byte) (long) args[2];
            long value = (long) args[3];
            MemoryEvent evt = new GenericMemoryEvent(be, 0, addr, len, false, value);
            analyzer.createEvent(evt);
            return 0;
        }
    }

    public static class SetContext extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public SetContext(CustomAnalyzer analyzer) {
            super("set_context", PrimitiveType.VOID, list(PointerType.VOIDPTR));
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            Pointer data = (Pointer) args[0];
            analyzer.setGlobals(data);
            return 0;
        }
    }

    public static class GetContext extends Intrinsic {
        private final CustomAnalyzer analyzer;

        public GetContext(CustomAnalyzer analyzer) {
            super("get_context", PointerType.VOIDPTR, list());
            this.analyzer = analyzer;
        }

        @Override
        public long execute(Context ctx, Object... args) {
            throw new UnsupportedOperationException("invalid integer call to alloca");
        }

        @Override
        public Pointer executePointer(Context ctx, Object... args) {
            return analyzer.getGlobals();
        }
    }

    public static void register(SymbolTable symtab) {
        symtab.define(new Alloca());
        symtab.define(new Strlen());
        symtab.define(new Strcmp());
        symtab.define(new Strcpy());
        symtab.define(new Strcat());
        symtab.define(new Sprintf());
    }

    public static void register(SymbolTable symtab, CustomArchitecture arch) {
        register(symtab);
        symtab.define(new SetName(arch));
        symtab.define(new SetDescription(arch));
        symtab.define(new SetStepType(arch));
        symtab.define(new SetStateType(arch));
        symtab.define(new SetFormat(arch));
        symtab.define(new IsStepEvent());
        symtab.define(new GetField());
    }

    public static List<Intrinsic> getAnalyzerIntrinsics(CustomAnalyzer analyzer) {
        List<Intrinsic> result = new ArrayList<>();
        result.add(new CreateStep(analyzer));
        result.add(new CreateMmap(analyzer));
        result.add(new CreateRead(analyzer));
        result.add(new CreateWrite(analyzer));
        result.add(new SetContext(analyzer));
        result.add(new GetContext(analyzer));
        result.add(new GetI8(analyzer));
        result.add(new GetI16(analyzer));
        result.add(new GetI32(analyzer));
        result.add(new GetI64(analyzer));
        return result;
    }
}
