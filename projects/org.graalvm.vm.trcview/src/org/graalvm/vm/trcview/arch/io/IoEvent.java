package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;

import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordOutputStream;

public class IoEvent extends Event {
    private final int channel;
    private final boolean input;
    private final String value;
    private final long step;

    public IoEvent(short arch, int tid, long step, int channel, boolean input, String value) {
        super(arch, CONIO, tid);
        this.step = step;
        this.channel = channel;
        this.input = input;
        this.value = value;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write8bit((byte) (input ? 1 : 0));
        out.write32bit(channel);
        out.write64bit(step);
        IO.writeString(out, value);
    }

    public long getStep() {
        return step;
    }

    public int getChannel() {
        return channel;
    }

    public boolean isInput() {
        return input;
    }

    public boolean isOutput() {
        return !input;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "IoEvent[channel=" + channel + ";" + (input ? "input" : "output") + ";value=" + DecoderUtils.str(value) + "]";
    }
}
