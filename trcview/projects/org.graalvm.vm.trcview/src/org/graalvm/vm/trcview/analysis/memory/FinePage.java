package org.graalvm.vm.trcview.analysis.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.util.io.Endianess;

public class FinePage extends Page {
    @SuppressWarnings("unchecked") private final ArrayList<MemoryUpdate>[] updates = new ArrayList[SIZE];
    @SuppressWarnings("unchecked") private final ArrayList<MemoryRead>[] reads = new ArrayList[SIZE];
    private final byte[] data = new byte[SIZE]; // initial data

    public FinePage(long address, long pc, long instructionCount, Node node, Protection prot) {
        super(address, pc, instructionCount, node, prot);
    }

    public FinePage(long address, byte[] data, long pc, long instructionCount, Node node, Protection prot) {
        this(address, pc, instructionCount, node, prot);
        assert data.length == SIZE;
        System.arraycopy(data, 0, this.data, 0, SIZE);
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public void addUpdate(long addr, byte size, long value, long instructionCount, Node node, StepEvent step, boolean be) {
        assert addr >= address && addr < (address + data.length);
        assert addr + size <= (address + data.length);
        addUpdate(new MemoryUpdate(be, addr, size, value, instructionCount, node, step));
    }

    @Override
    public void addRead(long addr, byte size, long instructionCount, Node node, StepEvent step) {
        assert addr >= address && addr < (address + data.length);
        assert addr + size <= (address + data.length);
        addRead(new MemoryRead(addr, size, instructionCount, node, step));
    }

    public void addUpdate(MemoryUpdate update) {
        long addr = update.address;
        assert addr >= address && addr < (address + data.length);
        assert addr + update.size <= (address + data.length);
        int off = (int) (addr - address);
        for (int i = 0; i < update.size && off + i < updates.length; i++) {
            if (updates[off + i] == null) {
                updates[off + i] = new ArrayList<>();
            }
            updates[off + i].add(update);
        }
        // does this event correspond to a step event?
        if (update.step != null && update.node != null && update.node instanceof MemoryEvent) {
            // only add the memory event to the step event if it is from the same thread
            if (update.step.getTid() == update.node.getTid()) {
                update.step.addWrite((MemoryEvent) update.node);
            }
        }
    }

    public void addRead(MemoryRead read) {
        long addr = read.address;
        assert addr >= address && addr < (address + data.length);
        assert addr + read.size <= (address + data.length);
        int off = (int) (addr - address);
        for (int i = 0; i < read.size && off + i < reads.length; i++) {
            if (reads[off + i] == null) {
                reads[off + i] = new ArrayList<>();
            }
            reads[off + i].add(read);
        }
        // does this event correspond to a step event?
        if (read.step != null && read.node != null && read.node instanceof MemoryEvent) {
            // only add the memory event to the step event if it is from the same thread
            if (read.step.getTid() == read.node.getTid()) {
                read.step.addRead((MemoryEvent) read.node);
            }
        }
    }

    @Override
    public void clear(long instructionCount, Node node, StepEvent step) {
        for (int i = 0; i < SIZE; i += 8) {
            addUpdate(address + i, (byte) 8, 0, instructionCount, node, step, false);
        }
    }

    @Override
    public void overwrite(byte[] update, long instructionCount, Node node, StepEvent step) {
        for (int i = 0; i < SIZE; i += 8) {
            long value = Endianess.get64bitLE(update, i);
            addUpdate(address + i, (byte) 8, value, instructionCount, node, step, false);
        }
    }

    @Override
    public byte getByte(long addr, long instructionCount) throws MemoryNotMappedException {
        MemoryUpdate update = getLastUpdate(addr, instructionCount);
        if (update == null) {
            return data[(int) (addr - address)];
        } else {
            return update.getByte(addr);
        }
    }

    @Override
    public MemoryUpdate getLastUpdate(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + SIZE) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (updates[off] == null) {
            return null;
        }

        if (updates[off].isEmpty()) {
            // no update until now
            return null;
        } else if (updates[off].get(0).instructionCount > instructionCount) {
            // first update is after instructionCount
            return null;
        } else if (updates[off].get(0).instructionCount == instructionCount) {
            // updates start at our timestamp; find last update
            MemoryUpdate result = null;
            for (MemoryUpdate update : updates[off]) {
                if (update.instructionCount > instructionCount) {
                    return result;
                } else {
                    result = update;
                }
            }
            return result;
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find update timestamp
        MemoryUpdate target = new MemoryUpdate(false, addr, (byte) 1, 0, instructionCount, null, null);
        int idx = Collections.binarySearch(updates[off], target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            assert updates[off].get(idx).instructionCount <= instructionCount;
            return updates[off].get(idx);
        } else {
            idx = ~idx;
            if (idx == 0) {
                return null;
            } else {
                assert updates[off].get(idx - 1).instructionCount <= instructionCount;
                return updates[off].get(idx - 1);
            }
        }
    }

    @Override
    public MemoryUpdate getNextUpdate(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + SIZE) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (updates[off] == null) {
            return null;
        }

        if (updates[off].isEmpty()) {
            // no updates
            return null;
        } else if (updates[off].get(0).instructionCount >= instructionCount) {
            // first update is after instructionCount
            return updates[off].get(0);
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find write timestamp
        MemoryUpdate target = new MemoryUpdate(false, addr, (byte) 1, 0, instructionCount, null, null);
        int idx = Collections.binarySearch(updates[off], target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            return updates[off].get(idx);
        } else {
            idx = ~idx;
            if (idx >= updates[off].size()) {
                return null;
            } else {
                return updates[off].get(idx);
            }
        }
    }

    @Override
    public MemoryRead getLastRead(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + SIZE) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (reads[off] == null) {
            return null;
        }

        if (reads[off].isEmpty() || instructionCount == firstInstructionCount) {
            // no read until now
            return null;
        } else if (reads[off].get(0).instructionCount > instructionCount) {
            // first read is after instructionCount
            return null;
        } else if (reads[off].get(0).instructionCount == instructionCount) {
            // reads start at our timestamp; find last update
            MemoryRead result = null;
            for (MemoryRead read : reads[off]) {
                if (read.instructionCount > instructionCount) {
                    return result;
                }
                if (read.contains(addr)) {
                    result = read;
                }
            }
            return result;
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find read timestamp
        MemoryRead target = new MemoryRead(addr, (byte) 1, instructionCount, null, null);
        int idx = Collections.binarySearch(reads[off], target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            return reads[off].get(idx);
        } else {
            idx = ~idx;
            if (idx == 0) {
                return null;
            } else {
                return reads[off].get(idx - 1);
            }
        }
    }

    @Override
    public MemoryRead getNextRead(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + SIZE) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (reads[off] == null) {
            return null;
        }

        if (reads[off].isEmpty()) {
            // no reads
            return null;
        } else if (reads[off].get(0).instructionCount >= instructionCount) {
            // first read is after instructionCount
            return reads[off].get(0);
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find read timestamp
        MemoryRead target = new MemoryRead(addr, (byte) 1, instructionCount, null, null);
        int idx = Collections.binarySearch(reads[off], target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            return reads[off].get(idx);
        } else {
            idx = ~idx;
            if (idx >= reads[off].size()) {
                return null;
            } else {
                return reads[off].get(idx);
            }
        }
    }

    @Override
    public List<MemoryUpdate> getPreviousUpdates(long addr, long instructionCount, long max) throws MemoryNotMappedException {
        if (addr < address || addr >= address + SIZE) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (updates[off] == null) {
            return Collections.emptyList();
        }

        if (updates[off].isEmpty()) {
            // no update until now
            return Collections.emptyList();
        } else if (updates[off].get(0).instructionCount > instructionCount) {
            // first update is after instructionCount
            return Collections.emptyList();
        } else if (updates[off].get(0).instructionCount == instructionCount) {
            // updates start at our timestamp; find last update
            List<MemoryUpdate> result = new ArrayList<>();
            for (MemoryUpdate update : updates[off]) {
                if (update.instructionCount > instructionCount) {
                    return result;
                } else {
                    result.add(update);
                }
            }
            return result;
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find update timestamp
        MemoryUpdate target = new MemoryUpdate(false, addr, (byte) 1, 0, instructionCount, null, null);
        int idx = Collections.binarySearch(updates[off], target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            assert updates[off].get(idx).instructionCount <= instructionCount;
            List<MemoryUpdate> result = new ArrayList<>();
            for (int i = 0; i < max && i < idx; i++) {
                result.add(updates[off].get(idx - i));
            }
            return result;
        } else {
            idx = ~idx;
            if (idx == 0) {
                return Collections.emptyList();
            } else {
                assert updates[off].get(idx - 1).instructionCount <= instructionCount;
                List<MemoryUpdate> result = new ArrayList<>();
                for (int i = 0; i < max && i < (idx - 1); i++) {
                    result.add(updates[off].get(idx - 1 - i));
                }
                return result;
            }
        }
    }

    @Override
    public List<MemoryRead> getReads(long addr) throws MemoryNotMappedException {
        if (addr < address || addr >= address + SIZE) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (reads[off] == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(reads[off]);
    }

    @Override
    public List<MemoryUpdate> getUpdates(long addr) throws MemoryNotMappedException {
        if (addr < address || addr >= address + SIZE) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (updates[off] == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(updates[off]);
    }

    @Override
    public long getWord(long addr, long instructionCount) throws MemoryNotMappedException {
        // TODO: this could be implemented more efficiently
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result >>>= 8;
            result |= Byte.toUnsignedLong(getByte(addr + i, instructionCount)) << 56;
        }
        return result;
    }

    @Override
    public byte getLastByte(long addr) throws MemoryNotMappedException {
        if (addr < address || addr >= address + SIZE) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        int off = (int) (addr - address);
        if (updates[off] == null || updates[off].isEmpty()) {
            return data[off];
        } else {
            MemoryUpdate update = updates[off].get(updates[off].size() - 1);
            return update.getByte(addr);
        }
    }

    @Override
    public void trim() {
        for (ArrayList<MemoryUpdate> update : updates) {
            if (update != null) {
                update.trimToSize();
            }
        }
        for (ArrayList<MemoryRead> read : reads) {
            if (read != null) {
                read.trimToSize();
            }
        }
    }
}