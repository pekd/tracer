package org.graalvm.vm.trcview.arch.custom.io;

import java.io.IOException;
import java.util.Iterator;

import org.graalvm.vm.trcview.arch.custom.analysis.CustomAnalyzer;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.TraceReader;

public class CustomTraceReader extends TraceReader {
    private final Iterator<Event> events;
    private long pos;

    public CustomTraceReader(CustomAnalyzer analyzer) {
        setArchitecture(analyzer.getArchitecture());
        events = analyzer.getEvents().iterator();
        pos = 0;
    }

    @Override
    public Event read() throws IOException {
        if (events.hasNext()) {
            pos++;
            return events.next();
        } else {
            return null;
        }
    }

    @Override
    public long tell() {
        return pos;
    }
}
