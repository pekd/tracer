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

public class CustomAnalyzer implements Analyzer {
    private final CustomArchitecture arch;
    private final Function process;

    private Context ctx;

    private List<Event> events;

    public CustomAnalyzer(String script) {
        arch = new CustomArchitecture(script, Intrinsics.getAnalyzerIntrinsics(this));
        process = arch.symbols.getFunction("process");
        if (process == null) {
            throw new IllegalArgumentException("no process function");
        }
    }

    public void start(TraceAnalyzer trc) {
        ctx = new Context(arch.context);
        events = new ArrayList<>();
    }

    public void process(Event event, Node node) {
        process.execute(ctx, new JavaPointer(event), new JavaPointer(node));
    }

    public void finish() {
        // TODO Auto-generated method stub
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
