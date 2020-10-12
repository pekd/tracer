package org.graalvm.vm.trcview.analysis.memory;

public class MemorySegment {
    private final long start;
    private final long end;
    private final Protection permission;
    private String name;

    public MemorySegment(long start, long end, Protection permission, String name) {
        this.start = start;
        this.end = end;
        this.permission = permission;
        this.name = name;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public Protection getProtection() {
        return permission;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return end - start + 1;
    }

    @Override
    public String toString() {
        return String.format("%x-%x %s %s", start, end, permission, name);
    }
}
