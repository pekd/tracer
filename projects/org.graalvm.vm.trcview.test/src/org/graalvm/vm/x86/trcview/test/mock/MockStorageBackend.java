package org.graalvm.vm.x86.trcview.test.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.graalvm.vm.trcview.storage.Step;
import org.graalvm.vm.trcview.storage.StorageBackend;
import org.graalvm.vm.trcview.storage.TraceMetadata;

public class MockStorageBackend extends StorageBackend {
    private List<Step> steps = new ArrayList<>();

    @Override
    public List<TraceMetadata> list() {
        return Collections.emptyList();
    }

    @Override
    public void connect(String name) {
    }

    @Override
    public void create(String name, short arch) {
    }

    @Override
    public void createStep(int tid, long step, long parent, long pc, int type, byte[] machinecode, byte[] cpustate) {
        steps.add(new Step(tid, step, parent, pc, type, machinecode, cpustate));
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
    }

    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    @Override
    public List<Step> getSteps(long parent, long start, long count) {
        return steps.stream().filter(x -> x.parent == parent).filter(x -> x.step >= start).limit(count).collect(Collectors.toList());
    }

    @Override
    public short getArchitecture() {
        return MockArchitecture.ID;
    }

    @Override
    public long getStepCount() {
        return steps.size();
    }
}
