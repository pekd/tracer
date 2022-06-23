package org.graalvm.vm.trcview.arch.custom.io;

import org.graalvm.vm.trcview.arch.custom.format.FieldFormatter;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.util.io.Endianess;

public class StateDescription {
    public final int pcOffset;
    public final int pcSize;
    public final int stepOffset;
    public final int stepSize;
    public final Struct struct;
    public final FieldFormatter formatter;
    public final boolean bigEndian;

    public StateDescription(int pcOffset, int pcSize, int stepOffset, int stepSize, Struct struct, String format, boolean bigEndian) {
        this.pcOffset = pcOffset;
        this.pcSize = pcSize;
        this.stepOffset = stepOffset;
        this.stepSize = stepSize;
        this.struct = struct;
        this.formatter = new FieldFormatter(format);
        this.bigEndian = bigEndian;
        checkSize(pcSize);
        checkSize(stepSize);
    }

    private static void checkSize(int size) {
        switch (size) {
            case 1:
            case 2:
            case 4:
            case 8:
                return;
            default:
                throw new IllegalArgumentException("invalid size");
        }
    }

    private static long getBE(int offset, int size, byte[] data) {
        switch (size) {
            case 1:
                return data[offset];
            case 2:
                return Endianess.get16bitBE(data, offset);
            case 4:
                return Endianess.get32bitBE(data, offset);
            case 8:
                return Endianess.get64bitBE(data, offset);
            default:
                return 0;
        }
    }

    private static long getLE(int offset, int size, byte[] data) {
        switch (size) {
            case 1:
                return data[offset];
            case 2:
                return Endianess.get16bitLE(data, offset);
            case 4:
                return Endianess.get32bitLE(data, offset);
            case 8:
                return Endianess.get64bitLE(data, offset);
            default:
                return 0;
        }
    }

    private long get(int offset, int size, byte[] data) {
        if (bigEndian) {
            return getBE(offset, size, data);
        } else {
            return getLE(offset, size, data);
        }
    }

    public long getPC(byte[] data) {
        return get(pcOffset, pcSize, data);
    }

    public long getStep(byte[] data) {
        return get(stepOffset, stepSize, data);
    }
}
