package org.graalvm.vm.x86.trcview.test.mock;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.TraceReader;

public class MockTraceReader extends TraceReader {
    private final Architecture arch;
    private final Iterator<Event> events;
    private long pos;

    public MockTraceReader(List<Event> data, Architecture arch) {
        this.arch = arch;
        events = data.iterator();
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

    @Override
    public Architecture getArchitecture() {
        return arch;
    }
}
