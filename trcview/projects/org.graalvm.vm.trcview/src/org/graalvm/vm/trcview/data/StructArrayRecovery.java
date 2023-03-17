package org.graalvm.vm.trcview.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.NameAlreadyUsedException;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class StructArrayRecovery {
    private static final Logger log = Trace.create(StructArrayRecovery.class);

    private NavigableMap<Long, Array> arrays = new TreeMap<>();

    private final TraceAnalyzer trc;

    public StructArrayRecovery(TraceAnalyzer trc) {
        this.trc = trc;
    }

    public void defineArray(Type type, int elementSize, long first, long last) {
        // TODO: find overlapping arrays

        Array array = null;
        Entry<Long, Array> floor = arrays.floorEntry(first);
        if (floor != null) {
            Array a = floor.getValue();
            if (a.end < first) {
                // not found
            } else {
                // old array overlaps with new one
                // TODO: extend array size
                array = a;
                log.info("overlap: start");
            }
        }

        if (array == null) {
            Entry<Long, Array> ceil = arrays.ceilingEntry(first);
            if (ceil != null) {
                long addr = ceil.getKey();
                if (addr < last) {
                    // old array overlaps with new one
                    // TODO: extend array size
                    array = ceil.getValue();
                    if (elementSize != array.elementSize) {
                        // element size mismatch
                        log.log(Levels.WARNING, () -> String.format("Element size mismatch for array at 0x%x-0x%x: %d vs %d", first, last, elementSize, ceil.getValue().elementSize));
                        array = null;
                    } else {
                        long diff = first - array.start;
                        long offset = diff % elementSize;
                        if (offset == 0) {
                            array.start = first;
                        }
                    }
                } else {
                    // not found
                }
            }
        }

        if (array == null) {
            array = new Array(first, last, elementSize);
            arrays.put(first, array);

            log.info("array of structs (new): " + array);
        } else {
            log.info("array of structs (old): " + array + "; " + String.format("0x%x-0x%x", first, last));
            // resize
            array.end = last + array.elementSize;
        }

        long offset = (first - array.start) % elementSize;
        if (offset < 0) {
            log.info(String.format("offset %d => redefining array start location", offset));
            array.resizeStart(-offset);
            array.start = first;
            offset = 0;
        }

        log.info(String.format("offset: %s [0x%x, 0x%x]", offset, array.start, first));
        array.define(offset, type);
    }

    public void transfer() {
        int id = 0;

        UserTypeDatabase types = trc.getTypeDatabase();
        TypedMemory mem = trc.getTypedMemory();

        Map<Struct, Struct> structs = new HashMap<>();

        for (Array array : arrays.values()) {
            Struct struct = array.struct.createStruct("struct_" + id);
            if (structs.containsKey(struct)) {
                struct = structs.get(struct);
            } else {
                structs.put(struct, struct);
                id++;

                try {
                    types.add(struct);
                } catch (NameAlreadyUsedException e) {
                    log.log(Levels.ERROR, "Name collision for struct name " + struct.getName());
                    continue;
                }
            }

            Type type = new Type(struct, false, array.getElements());
            mem.setRecoveredType(array.start, type);
        }
    }

    private class Array {
        private Record struct;
        private long start;
        private long end;
        private int elementSize;

        public Array(long start, long end, int elementSize) {
            this.start = start;
            this.end = end + elementSize;
            this.elementSize = elementSize;
            struct = new Record(elementSize);
        }

        public void define(long offset, Type type) {
            log.log(Levels.INFO, "offset " + offset + " has type " + type);
            struct.addField(offset, type);
        }

        public void resizeStart(long offset) {
            start -= offset;
            struct.reorder(offset);
        }

        public long size() {
            return end - start;
        }

        public int getElements() {
            return (int) (size() / elementSize);
        }

        @Override
        public String toString() {
            return String.format("Array[0x%x-0x%x:%s, size=%s]", start, end, elementSize, size());
        }
    }

    private class Record {
        private int size;
        private Map<Long, Type> fields = new HashMap<>();

        public Record(int size) {
            assert size > 0;
            this.size = size;
        }

        public void reorder(long offset) {
            Map<Long, Type> reordered = new HashMap<>();
            for (Entry<Long, Type> entry : fields.entrySet()) {
                long off = (entry.getKey() + offset) % size;
                assert off + entry.getValue().getSize() <= size;
                reordered.put(off, entry.getValue());
            }
            fields = reordered;
        }

        public void addField(long offset, Type field) {
            // check if this field is already defined
            long off = offset % size;
            if (fields.get(off) != null) {
                return;
            }

            if (offset + field.getSize() > size) {
                log.log(Levels.WARNING, "Cannot add field to array of structs: offset=" + offset + ", field.size=" + field.getSize() + ", size=" + size);
            } else {
                fields.put(off, field);
            }
        }

        public Struct createStruct(String name) {
            StepFormat fmt = trc.getArchitecture().getFormat();
            Struct struct = new Struct(name);
            long[] offsets = fields.keySet().stream().mapToLong(x -> x).sorted().toArray();
            long lastOffset = 0;
            for (long offset : offsets) {
                if (lastOffset < offset) {
                    // add padding
                    long pad = offset - lastOffset;
                    assert pad > 0;
                    struct.add("pad_" + fmt.formatShortAddress(lastOffset), new Type(DataType.U8, false, (int) pad));
                }
                Type field = fields.get(offset);
                lastOffset = offset + field.getSize();
                struct.add("field_" + fmt.formatShortAddress(offset), field);
            }
            if (lastOffset != size) {
                // add padding
                long pad = size - lastOffset;
                assert pad > 0;
                struct.add("pad_" + fmt.formatShortAddress(lastOffset), new Type(DataType.U8, false, (int) pad));
            }
            return struct;
        }

        @Override
        public String toString() {
            return "Struct[size=" + size + "]";
        }
    }
}
