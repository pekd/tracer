package org.graalvm.vm.trcview.arch.custom.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.arch.custom.CustomArchitecture;
import org.graalvm.vm.trcview.arch.custom.Intrinsics;
import org.graalvm.vm.trcview.arch.custom.io.CustomStepEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.script.ast.Function;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.JavaPointer;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.rt.Record;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.util.log.Trace;

public class CustomAnalyzer implements Analyzer {
    private static final Logger log = Trace.create(CustomAnalyzer.class);

    private final CustomArchitecture arch;
    private final Function start;
    private final Function finish;
    private final Function process;
    private final Function disasm;
    private final Function getType;
    private final Function printState;

    private Context ctx;
    private Pointer globals;

    private List<Event> events;

    private MemoryTrace memory;
    private long currentStep;

    public CustomAnalyzer(String script) {
        arch = new CustomArchitecture(script, Intrinsics.getAnalyzerIntrinsics(this));
        start = arch.symbols.getFunction("start");
        finish = arch.symbols.getFunction("finish");
        process = arch.symbols.getFunction("process");
        disasm = arch.symbols.getFunction("disasm");
        getType = arch.symbols.getFunction("get_type");
        printState = arch.symbols.getFunction("print_state");
        if (process == null) {
            throw new IllegalArgumentException("no process function");
        }
        globals = null;
    }

    public void setGlobals(Pointer globals) {
        this.globals = globals;
    }

    public Pointer getGlobals() {
        return globals;
    }

    @Override
    public void start(MemoryTrace mem) {
        memory = mem;
        currentStep = 0;
        ctx = new Context(arch.context);
        events = new ArrayList<>();
        if (start != null) {
            start.execute(ctx);
        }
    }

    @Override
    public void process(Event event, Node node) {
        if (event instanceof StepEvent) {
            currentStep = ((StepEvent) event).getStep();
        }
        process.execute(ctx, new JavaPointer(event), new JavaPointer(node));
    }

    @Override
    public void finish() {
        if (finish != null) {
            finish.execute(ctx);
        }
        log.info("Created " + events.size() + " events");
    }

    public MemoryTrace getMemoryTrace() {
        return memory;
    }

    public long getCurrentStep() {
        return currentStep;
    }

    public String disassemble(Pointer data) {
        if (disasm == null) {
            return "unknown";
        } else {
            byte[] result = new byte[64];
            Pointer ptr = new Pointer(PointerType.CHARPTR, 0, new Record(PrimitiveType.CHAR, result));
            disasm.execute(ctx, data, ptr);
            return ptr.cstr();
        }
    }

    public InstructionType getType(Pointer data) {
        if (getType == null) {
            return InstructionType.OTHER;
        } else {
            switch ((int) getType.execute(ctx, data)) {
                default:
                case 0:
                    return InstructionType.OTHER;
                case 1:
                    return InstructionType.JCC;
                case 2:
                    return InstructionType.JMP;
                case 3:
                    return InstructionType.JMP_INDIRECT;
                case 4:
                    return InstructionType.CALL;
                case 5:
                    return InstructionType.RET;
                case 6:
                    return InstructionType.SYSCALL;
                case 7:
                    return InstructionType.RTI;
            }
        }
    }

    public String printState(CustomStepEvent state) {
        Pointer data = state.getPointer();
        if (printState == null) {
            Struct struct = (Struct) arch.getStepType();
            StringBuilder result = new StringBuilder();
            for (Member member : struct.getMembers()) {
                result.append(member.name);
                result.append('=');
                long value = state.get(member.name);
                result.append(arch.getFormat().formatWord(value));
            }
            return result.toString();
        } else {
            byte[] result = new byte[4096];
            Pointer out = new Pointer(PointerType.CHARPTR, 0, new Record(PrimitiveType.CHAR, result));
            printState.execute(ctx, data, out);
            return out.cstr();
        }
    }

    public CustomArchitecture getArchitecture() {
        return arch;
    }

    public void createEvent(Event event) {
        events.add(event);
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }
}
