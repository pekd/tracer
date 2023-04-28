package org.graalvm.vm.trcview.arch.vm.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.trcview.arch.vm.VMArchitecture;
import org.graalvm.vm.trcview.arch.vm.io.VMTraceReader;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.util.ui.MessageBox;

public class VMAnalyzer implements Analyzer {
    private static final Logger log = Trace.create(VMAnalyzer.class);

    private final VMTransformer transformer;

    private List<Event> events;

    private MemoryTrace memory;
    private long currentStep;

    public VMAnalyzer(VMTransformer transformer) {
        this.transformer = transformer;
        transformer.setAnalyzer(this);
    }

    @Override
    public void start(MemoryTrace mem, Architecture hostarch) {
        memory = mem;
        currentStep = 0;
        events = new ArrayList<>();
        transformer.setHostArchitecture(hostarch);
        transformer.start();
    }

    @Override
    public void process(Event event, Node node, CpuState state) {
        if (event instanceof StepEvent) {
            currentStep = ((StepEvent) event).getStep();
        }
        transformer.process(event, node, state);
    }

    @Override
    public void finish() {
        transformer.finish();
        log.info("Created " + events.size() + " events");
    }

    public MemoryTrace getMemoryTrace() {
        return memory;
    }

    public long getCurrentStep() {
        return currentStep;
    }

    public VMArchitecture getArchitecture() {
        return transformer.getArchitecture();
    }

    public void createEvent(Event event) {
        events.add(event);
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void traceLoaded(MainWindow master, TraceAnalyzer trc) {
        if (trc.getArchitecture() == transformer.getArchitecture()) {
            log.info("Same architecture, aborting");
            return;
        }

        if (getEvents().isEmpty()) {
            log.info("No events in transformed trace");
            return;
        }

        MainWindow window = new MainWindow(master);
        window.setVisible(true);
        window.setStatus("Loading...");
        TraceReader reader = new VMTraceReader(this);
        long size = getEvents().size();
        String file = "Transformed trace [" + getArchitecture().getName() + "]";
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    window.load(reader, size, file);
                } catch (IOException ex) {
                    MessageBox.showError(window, ex);
                    window.dispose();
                }
                return null;
            }
        };
        worker.execute();
    }
}
