package org.graalvm.vm.trcview.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.graalvm.vm.trcview.arch.io.StepEvent;

public class MemoryAccessMap {
    private final Map<Long, MemoryAccessPage> pages = new HashMap<>();

    private static long getPageAddress(long address) {
        return address & 0xFFFFFFFFFFFFF000L;
    }

    private static int getPageOffset(long address) {
        return (int) (address & 0xFFF);
    }

    public void access(long addr, StepEvent step) {
        long pageAddress = getPageAddress(addr);
        MemoryAccessPage page = pages.get(pageAddress);
        if (page == null) {
            page = new MemoryAccessPage();
            pages.put(pageAddress, page);
        }
        page.access(getPageOffset(addr), step);
    }

    public Set<StepEvent> getSteps(long pc) {
        long pageAddress = getPageAddress(pc);
        MemoryAccessPage page = pages.get(pageAddress);
        if (page == null) {
            return Collections.emptySet();
        } else {
            return page.getSteps(getPageOffset(pc));
        }
    }

    public Set<Long> getPCs() {
        Set<Long> pcs = new HashSet<>();
        for (Entry<Long, MemoryAccessPage> entry : pages.entrySet()) {
            long base = entry.getKey();
            MemoryAccessPage page = entry.getValue();
            for (int offset : page.getOffsets()) {
                pcs.add(base + offset);
            }
        }
        return pcs;
    }

    public Set<StepEvent> getCode() {
        Set<StepEvent> steps = new HashSet<>();
        for (Entry<Long, MemoryAccessPage> entry : pages.entrySet()) {
            MemoryAccessPage page = entry.getValue();
            steps.addAll(page.getCode());
        }
        return steps;
    }
}
