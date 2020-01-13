package org.graalvm.vm.trcview.arch.custom.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.arch.custom.CustomArchitecture;
import org.graalvm.vm.trcview.arch.custom.Intrinsics;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.script.ast.Function;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.JavaPointer;
import org.graalvm.vm.trcview.script.rt.Pointer;

public class CustomAnalyzer implements Analyzer {
    private final CustomArchitecture arch;
    private final Function start;
    private final Function finish;
    private final Function process;

    private Context ctx;
    private Pointer globals;

    private List<Event> events;

    public CustomAnalyzer(String script) {
        arch = new CustomArchitecture(script, Intrinsics.getAnalyzerIntrinsics(this));
        start = arch.symbols.getFunction("start");
        finish = arch.symbols.getFunction("finish");
        process = arch.symbols.getFunction("process");
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

    public void start(TraceAnalyzer trc) {
        ctx = new Context(arch.context);
        events = new ArrayList<>();
        if (start != null) {
            start.execute(ctx);
        }
    }

    public void process(Event event, Node node) {
        process.execute(ctx, new JavaPointer(event), new JavaPointer(node));
    }

    public void finish() {
        if (finish != null) {
            finish.execute(ctx);
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
