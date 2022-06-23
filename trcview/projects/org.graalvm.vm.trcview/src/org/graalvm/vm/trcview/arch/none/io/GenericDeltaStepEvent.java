package org.graalvm.vm.trcview.arch.none.io;

import java.io.IOException;
import java.util.Arrays;

import org.graalvm.vm.util.io.WordInputStream;

public class GenericDeltaStepEvent extends GenericStepEvent {
    private final byte[] mask;
    private final byte[] data;
    private final GenericStepEvent last;

    protected GenericDeltaStepEvent(GenericStateDescription description, int tid, long step, long pc, byte type, byte[] machinecode, String[] disassembly, byte[] mask, byte[] data,
                    GenericStepEvent last) {
        super(description, tid, step, pc, type, machinecode, disassembly);
        this.mask = mask;
        this.data = data;
        this.last = last;
    }

    @Override
    public byte[] getData() {
        byte[] previous = last.getData();
        byte[] current = Arrays.copyOf(previous, previous.length);
        int off = 0;
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < 8; j++) {
                if ((mask[i] & (1 << j)) != 0) {
                    int pos = i * 8 + j;
                    current[pos] = data[off++];
                }
            }
        }
        return current;
    }

    public static GenericDeltaStepEvent parse(WordInputStream in, int tid, GenericStateDescription description, GenericStepEvent last) throws IOException {
        long step = in.read64bit();
        long pc = in.read64bit();

        int masksize = description.getSize() / 8;
        if ((description.getSize() % 8) != 0) {
            masksize++;
        }
        byte[] mask = new byte[masksize];
        in.read(mask);
        int bytes = 0;
        for (int i = 0; i < mask.length; i++) {
            bytes += Integer.bitCount(Byte.toUnsignedInt(mask[i]));
        }
        byte[] data = new byte[bytes];
        in.read(data);

        String[] disassembly = description.readStrings(in);
        byte[] machinecode = read8(in);
        byte type = (byte) in.read8bit();

        return new GenericDeltaStepEvent(description, tid, step, pc, type, machinecode, disassembly, mask, data, last);
    }
}
