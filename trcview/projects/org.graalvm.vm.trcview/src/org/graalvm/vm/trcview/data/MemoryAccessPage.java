package org.graalvm.vm.trcview.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.graalvm.vm.trcview.arch.io.StepEvent;

public class MemoryAccessPage {
    public static final int PAGE_SIZE = 4096;

    @SuppressWarnings("unchecked") private final Set<StepEvent>[] steps = new HashSet[PAGE_SIZE];

    public void access(int offset, StepEvent step) {
        if (steps[offset] == null) {
            steps[offset] = new HashSet<>();
        }

        steps[offset].add(step);
    }

    public Set<StepEvent> getSteps(int offset) {
        if (steps[offset] == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(steps[offset]);
        }
    }

    public int[] getOffsets() {
        int[] offsets = new int[PAGE_SIZE];
        int i = 0;
        for (int offset = 0; offset < steps.length; offset++) {
            if (steps[offset] != null) {
                offsets[i++] = offset;
            }
        }

        int[] result = new int[i];
        System.arraycopy(offsets, 0, result, 0, i);
        return result;
    }

    public Set<StepEvent> getCode() {
        Set<StepEvent> result = new HashSet<>();
        for (Set<StepEvent> s : steps) {
            if (s != null) {
                // add first step
                Iterator<StepEvent> i = s.iterator();
                if (i.hasNext()) {
                    result.add(i.next());
                }
            }
        }
        return result;
    }
}
