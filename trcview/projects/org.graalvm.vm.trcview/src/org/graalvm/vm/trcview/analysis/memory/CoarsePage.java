package org.graalvm.vm.trcview.analysis.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.util.io.Endianess;

public class CoarsePage extends Page {
    private final byte[] data = new byte[SIZE]; // initial data
    private final List<MemoryUpdate> updates = new ArrayList<>();
    private final List<MemoryRead> reads = new ArrayList<>();

    public CoarsePage(long address, long pc, long instructionCount, Node node, Protection prot) {
        super(address, pc, instructionCount, node, prot);
    }

    public CoarsePage(long address, byte[] data, long pc, long instructionCount, Node node, Protection prot) {
        this(address, pc, instructionCount, node, prot);
        assert data.length == 4096;
        System.arraycopy(data, 0, this.data, 0, 4096);
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public void addUpdate(long addr, byte size, long value, long instructionCount, Node node, StepEvent step, boolean be) {
        assert addr >= address && addr < (address + data.length);
        assert addr + size <= (address + data.length);
        updates.add(new MemoryUpdate(be, addr, size, value, instructionCount, node, step));
    }

    @Override
    public void addRead(long addr, byte size, long instructionCount, Node node, StepEvent step) {
        assert addr >= address && addr < (address + data.length);
        assert addr + size <= (address + data.length);
        reads.add(new MemoryRead(addr, size, instructionCount, node, step));
    }

    @Override
    public void clear(long instructionCount, Node node, StepEvent step) {
        for (int i = 0; i < 4096; i += 8) {
            addUpdate(address + i, (byte) 8, 0, instructionCount, node, step, false);
        }
    }

    @Override
    public void overwrite(byte[] update, long instructionCount, Node node, StepEvent step) {
        for (int i = 0; i < 4096; i += 8) {
            long value = Endianess.get64bitLE(update, i);
            addUpdate(address + i, (byte) 8, value, instructionCount, node, step, false);
        }
    }

    @Override
    public byte getByte(long addr, long instructionCount) throws MemoryNotMappedException {
        if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x", addr));
        }
        MemoryUpdate update = getLastUpdate(addr, instructionCount);
        if (update == null) {
            return data[(int) (addr - address)];
        } else {
            return update.getByte(addr);
        }
    }

    @Override
    public MemoryUpdate getLastUpdate(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + 4096) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        if (updates.isEmpty() || instructionCount <= firstInstructionCount) {
            // no update until now
            return null;
        } else if (updates.get(0).instructionCount > instructionCount) {
            // first update is after instructionCount
            return null;
        } else if (updates.get(0).instructionCount == instructionCount) {
            // updates start at our timestamp; find last update
            MemoryUpdate result = null;
            for (MemoryUpdate update : updates) {
                if (update.instructionCount > instructionCount) {
                    return result;
                }
                if (update.contains(addr)) {
                    result = update;
                }
            }
            return result;
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find update timestamp
        MemoryUpdate target = new MemoryUpdate(false, addr, (byte) 1, 0, instructionCount, null, null);
        int idx = Collections.binarySearch(updates, target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            // timestamp found: search first update with matching timestamp
            while (idx > 0 && updates.get(idx).instructionCount == instructionCount) {
                idx--;
            }

            assert idx >= 0;
            assert updates.get(idx).instructionCount < instructionCount : String.format("%d vs %d", updates.get(idx).instructionCount, instructionCount);

            // check updates with same timestamp
            for (int i = idx; i < updates.size(); i++) {
                MemoryUpdate update = updates.get(i);
                if (update.instructionCount > instructionCount) {
                    break;
                }
                if (update.contains(addr)) {
                    return update;
                }
            }
        } else {
            // no match: points to next update after the timestamp we're looking for
            idx = ~idx - 1;
            if (idx < 0) {
                idx = 0;
            }
        }

        assert idx >= 0;
        assert updates.get(idx).instructionCount < instructionCount : String.format("%d vs %d", updates.get(idx).instructionCount, instructionCount);

        // go backwards in time
        for (int i = idx; i >= 0; i--) {
            MemoryUpdate update = updates.get(i);
            if (update.contains(addr)) {
                return update;
            }
        }

        // no update found
        return null;
    }

    @Override
    public MemoryUpdate getNextUpdate(long addr, long instructionCount) throws MemoryNotMappedException {
        return null;
    }

    @Override
    public long getWord(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + 4096) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException(String.format("no memory mapped to 0x%x", addr));
        }

        byte bytes = 0;
        long value = Endianess.get64bitLE(data, (int) (addr - address));
        if (updates.isEmpty() || instructionCount == firstInstructionCount) {
            // no update until now
            return value;
        } else if (updates.get(0).instructionCount > instructionCount) {
            // first update is after instructionCount
            return value;
        } else if (updates.get(0).instructionCount == instructionCount) {
            // updates start at our timestamp; find last update
            for (MemoryUpdate update : updates) {
                if (update.instructionCount > instructionCount) {
                    return value;
                }
                for (int i = 0; i < 8; i++) {
                    if (update.contains(addr + i)) {
                        bytes |= 1 << i;
                        value = (value & ~(0xFFL << (i * 8))) | (Byte.toUnsignedLong(update.getByte(addr + i)) << (i * 8));
                    }
                }
            }
            return value;
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find update timestamp
        MemoryUpdate target = new MemoryUpdate(false, addr, (byte) 1, 0, instructionCount, null, null);
        int idx = Collections.binarySearch(updates, target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            // timestamp found: search first update with matching timestamp
            while (idx > 0 && updates.get(idx).instructionCount == instructionCount) {
                idx--;
            }

            assert idx >= 0;
            assert updates.get(idx).instructionCount < instructionCount : String.format("%d vs %d", updates.get(idx).instructionCount, instructionCount);

            // check updates with same timestamp
            for (MemoryUpdate update : updates) {
                if (update.instructionCount > instructionCount) {
                    break;
                }
                for (int i = 0; i < 8; i++) {
                    if (update.contains(addr + i)) {
                        bytes |= 1 << i;
                        value = (value & ~(0xFFL << (i * 8))) | (Byte.toUnsignedLong(update.getByte(addr + i)) << (i * 8));
                    }
                }
            }

            if (bytes == -1) {
                return value;
            }
        } else {
            // no match: points to next update after the timestamp we're looking for
            idx = ~idx - 1;
            if (idx < 0) {
                idx = 0;
            }
        }

        assert idx >= 0;
        assert updates.get(idx).instructionCount < instructionCount : String.format("%d vs %d", updates.get(idx).instructionCount, instructionCount);

        // go backwards in time
        for (int i = idx; i >= 0; i--) {
            MemoryUpdate update = updates.get(i);
            for (int j = 0; j < 8; j++) {
                if (update.contains(addr + j)) {
                    bytes |= 1 << j;
                    value = (value & ~(0xFFL << (j * 8))) | (Byte.toUnsignedLong(update.getByte(addr + j)) << (j * 8));
                }
            }
            if (bytes == -1) {
                return value;
            }
        }

        return value;
    }

    @Override
    public byte getLastByte(long addr) throws MemoryNotMappedException {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public MemoryRead getLastRead(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + 4096) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        if (reads.isEmpty() || instructionCount <= firstInstructionCount) {
            // no read until now
            return null;
        } else if (reads.get(0).instructionCount > instructionCount) {
            // first read is after instructionCount
            return null;
        } else if (reads.get(0).instructionCount == instructionCount) {
            // reads start at our timestamp; find last read
            MemoryRead result = null;
            for (MemoryRead read : reads) {
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

        // find update timestamp
        MemoryRead target = new MemoryRead(addr, (byte) 1, instructionCount, null, null);
        int idx = Collections.binarySearch(reads, target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            // timestamp found: search first read with matching timestamp
            while (idx > 0 && reads.get(idx).instructionCount == instructionCount) {
                idx--;
            }

            assert idx >= 0;
            assert reads.get(idx).instructionCount < instructionCount : String.format("%d vs %d", reads.get(idx).instructionCount, instructionCount);

            // check reads with same timestamp
            for (int i = idx; i < reads.size(); i++) {
                MemoryRead read = reads.get(i);
                if (read.instructionCount > instructionCount) {
                    break;
                }
                if (read.contains(addr)) {
                    return read;
                }
            }
        } else {
            // no match: points to next read after the timestamp we're looking for
            idx = ~idx - 1;
            if (idx < 0) {
                idx = 0;
            }
        }

        assert idx >= 0;
        assert reads.get(idx).instructionCount < instructionCount : String.format("%d vs %d", reads.get(idx).instructionCount, instructionCount);

        // go backwards in time
        for (int i = idx; i >= 0; i--) {
            MemoryRead read = reads.get(i);
            if (read.contains(addr)) {
                return read;
            }
        }

        // no update found
        return null;
    }

    @Override
    public MemoryRead getNextRead(long addr, long instructionCount) throws MemoryNotMappedException {
        if (addr < address || addr >= address + 4096) {
            throw new AssertionError(String.format("wrong page for address 0x%x", addr));
        }

        if (reads.isEmpty()) {
            // no read
            return null;
        } else if (reads.get(0).instructionCount >= instructionCount) {
            // reads start at our timestamp; find next read
            for (MemoryRead read : reads) {
                if (read.contains(addr)) {
                    return read;
                }
            }
            return null;
        } else if (instructionCount < firstInstructionCount) {
            throw new MemoryNotMappedException("memory is not mapped at this time");
        }

        // find update timestamp
        MemoryRead target = new MemoryRead(addr, (byte) 1, instructionCount, null, null);
        int idx = Collections.binarySearch(reads, target, (a, b) -> {
            return Long.compareUnsigned(a.instructionCount, b.instructionCount);
        });

        if (idx > 0) {
            // timestamp found: search first read with matching timestamp
            while (idx > 0 && reads.get(idx).instructionCount == instructionCount) {
                idx--;
            }
            idx++;

            if (idx >= reads.size()) {
                return null;
            }

            assert idx >= 0;
            assert reads.get(idx).instructionCount == instructionCount : String.format("%d vs %d", reads.get(idx).instructionCount, instructionCount);

            // check reads with same timestamp
            for (int i = idx; i < reads.size(); i++) {
                MemoryRead read = reads.get(i);
                if (read.contains(addr)) {
                    return read;
                }
            }
        } else {
            // no match: points to next read after the timestamp we're looking for
            idx = ~idx;
        }

        if (idx >= reads.size()) {
            return null;
        }

        assert idx >= 0;
        assert reads.get(idx).instructionCount >= instructionCount : String.format("%d vs %d", reads.get(idx).instructionCount, instructionCount);

        for (int i = idx; i < reads.size(); i++) {
            MemoryRead read = reads.get(i);
            if (read.contains(addr)) {
                return read;
            }
        }

        // no update found
        return null;
    }

    @Override
    public List<MemoryUpdate> getPreviousUpdates(long addr, long instructionCount, long max) throws MemoryNotMappedException {
        return Collections.emptyList();
    }

    @Override
    public List<MemoryRead> getReads(long addr) throws MemoryNotMappedException {
        return Collections.emptyList();
    }

    @Override
    public List<MemoryUpdate> getUpdates(long addr) throws MemoryNotMappedException {
        return Collections.emptyList();
    }

    public int getSize() {
        return (updates.size() + reads.size()) / 2;
    }

    public FinePage transformToFine() {
        FinePage cell = new FinePage(address, data, firstPC, firstInstructionCount, firstNode, firstProtection);
        for (MemoryUpdate update : updates) {
            cell.addUpdate(update);
        }
        for (MemoryRead read : reads) {
            cell.addRead(read);
        }
        return cell;
    }
}
