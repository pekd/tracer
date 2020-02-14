package org.graalvm.vm.trcview.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MemoryBackend extends StorageBackend {
    private String name;
    private short arch;

    private Map<Long, List<MemoryAccess>> reads = new HashMap<>();
    private Map<Long, List<MemoryAccess>> writes = new HashMap<>();
    private Map<Long, List<Step>> steps = new HashMap<>();
    private NavigableMap<Long, List<Long>> pcidx = new TreeMap<>();
    private NavigableMap<Long, Step> stepidx = new TreeMap<>();

    private long cpustateBytes = 0;
    private long compressedCpustateBytes = 0;
    private long cpustateCount = 0;

    private long lastParent = -2;

    private static final long DELTA_CHAIN_LENGTH = 10_000;
    private long deltaChainLength = 0;
    private byte[] lastCpuState = null;

    public void close() throws IOException {
        System.out.println("average cpu state length: " + (cpustateBytes / cpustateCount) + "; compressed: " + (compressedCpustateBytes / cpustateCount));
    }

    @Override
    public List<TraceMetadata> list() {
        return Collections.emptyList();
    }

    @SuppressWarnings("hiding")
    @Override
    public void connect(String name) {
        this.name = name;
    }

    @SuppressWarnings("hiding")
    @Override
    public void create(String name, short arch) {
        this.name = name;
        this.arch = arch;
    }

    @Override
    public void createStep(int tid, long step, long parent, long pc, int type, byte[] machinecode, byte[] cpustate) {
        cpustateBytes += cpustate.length;
        cpustateCount++;

        byte[] state;
        if (lastCpuState == null || lastParent != parent) {
            deltaChainLength = 0;
            state = DeltaCompressor.keyframe(cpustate);
        } else if (deltaChainLength >= DELTA_CHAIN_LENGTH) {
            deltaChainLength = 0;
            state = DeltaCompressor.keyframe(cpustate);
        } else {
            deltaChainLength++;
            state = DeltaCompressor.compress(lastCpuState, cpustate);
        }
        lastCpuState = cpustate;
        compressedCpustateBytes += state.length;
        lastParent = parent;

        // ignore TID for now
        Step s = new Step(tid, step, parent, pc, type, machinecode, state);

        List<Step> steplist = steps.get(parent);
        if (steplist == null) {
            steplist = new ArrayList<>();
            steps.put(parent, steplist);
        }
        steplist.add(s);

        List<Long> pclist = pcidx.get(pc);
        if (pclist == null) {
            pclist = new ArrayList<>();
            pcidx.put(pc, pclist);
        }
        pclist.add(step);

        stepidx.put(step, s);
    }

    @Override
    public void createRead(int tid, long step, long address, int size, long value) {
        long val = value;
        for (int i = 0; i < size; i++) {
            List<MemoryAccess> accesses = reads.get(address + i);
            if (accesses == null) {
                accesses = new ArrayList<>();
                reads.put(address + i, accesses);
            }
            MemoryAccess access = new MemoryAccess(tid, step, address + i, (byte) val, address, (byte) size);
            accesses.add(access);
            val >>= 8;
        }
    }

    @Override
    public void createRead(int tid, long step, long address, int size, long hi, long lo) {
        createRead(tid, step, address, size, lo);
        createRead(tid, step, address + 8, size, hi);
    }

    @Override
    public void createWrite(int tid, long step, long address, int size, long value) {
        long val = value;
        for (int i = 0; i < size; i++) {
            List<MemoryAccess> accesses = writes.get(address + i);
            if (accesses == null) {
                accesses = new ArrayList<>();
                writes.put(address + i, accesses);
            }
            MemoryAccess access = new MemoryAccess(tid, step, address + i, (byte) val, address, (byte) size);
            accesses.add(access);
            val >>= 8;
        }
    }

    @Override
    public void createWrite(int tid, long step, long address, int size, long hi, long lo) {
        createWrite(tid, step, address, size, lo);
        createWrite(tid, step, address + 8, size, hi);
    }

    @Override
    public void flush() {
        // nothing
    }

    @Override
    public List<Step> getSteps(long parent, long start, long count) {
        List<Step> steplist = steps.get(parent);
        if (steplist == null) {
            return Collections.emptyList();
        } else {
            List<Step> filtered = steplist.stream().filter(s -> s.step >= start).limit(count).collect(Collectors.toList());
            List<Step> result = new ArrayList<>();

            if (filtered.isEmpty()) {
                return Collections.emptyList();
            }

            byte[] last = null;
            if (!DeltaCompressor.isKeyframe(filtered.get(0).cpustate)) {
                // find previous keyframe
                Step s = stepidx.floorEntry(filtered.get(0).step - 1).getValue();
                while (!DeltaCompressor.isKeyframe(s.cpustate)) {
                    s = stepidx.floorEntry(s.step - 1).getValue();
                }
                while (s.step < start) {
                    last = DeltaCompressor.decompress(last, s.cpustate);
                    s = stepidx.ceilingEntry(s.step + 1).getValue();
                }
            }
            for (Step step : filtered) {
                byte[] cpustate = DeltaCompressor.decompress(last, step.cpustate);
                result.add(new Step(step.tid, step.step, step.parent, step.pc, step.type, step.machinecode, cpustate));
                last = cpustate;
            }
            return result;
        }
    }

    @Override
    public long getStepCount(long parent) {
        List<Step> steplist = steps.get(parent);
        if (steplist == null) {
            return 0;
        } else {
            return steplist.size();
        }
    }

    @Override
    public MemoryAccess getRead(long address, long step) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MemoryAccess getWrite(long address, long step) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public short getArchitecture() {
        return arch;
    }

    @Override
    public long getStepCount() {
        return stepidx.size();
    }

    @Override
    public String toString() {
        return "MemoryBackend[" + name + "]";
    }
}
