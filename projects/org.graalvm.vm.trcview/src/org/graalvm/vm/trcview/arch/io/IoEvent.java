package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.trcview.decode.DecoderUtils;

public class IoEvent extends Event {
    private final int channel;
    private final boolean input;
    private final String value;
    private final long step;

    public IoEvent(int tid, long step, int channel, boolean input, String value) {
        super(tid);
        this.step = step;
        this.channel = channel;
        this.input = input;
        this.value = value;
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
