package org.graalvm.vm.trcview.analysis.memory;

public class MemorySegment {
    private final long start;
    private final long end;
    private final Protection protection;
    private String name;

    public MemorySegment(long start, long end, Protection protection, String name) {
        this.start = start;
        this.end = end;
        this.protection = protection;
        this.name = name;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public Protection getProtection() {
        return protection;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return end - start + 1;
    }

    @Override
    public String toString() {
        if (name != null) {
            return String.format("%x-%x %s %s", start, end, protection, name);
        } else {
            return String.format("%x-%x %s", start, end, protection);
        }
    }
}
