package org.graalvm.vm.trcview.arch.none.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;

public class GenericFullStepEvent extends GenericStepEvent {
    private final byte[] data;

    protected GenericFullStepEvent(GenericStateDescription description, int tid, long step, long pc, byte type, byte[] machinecode, String[] disassembly, byte[] data) {
        super(description, tid, step, pc, type, machinecode, disassembly);
        this.data = data;
    }

    public GenericFullStepEvent(GenericStepEvent event) {
        this(event.getDescription(), event.getTid(), event.getStep(), event.getPC(), event.getRawType(), event.getMachinecode(), event.getDisassemblyComponents(), event.getData());
    }

    @Override
    public byte[] getData() {
        return data;
    }

    public static GenericFullStepEvent parse(WordInputStream in, int tid, GenericStateDescription description) throws IOException {
        long step = in.read64bit();
        long pc = in.read64bit();
        byte[] state = new byte[description.getSize()];
        in.read(state);
        String[] disassembly = description.readStrings(in);
        byte[] machinecode = read8(in);
        byte type = (byte) in.read8bit();
        return new GenericFullStepEvent(description, tid, step, pc, type, machinecode, disassembly, state);
    }
}
