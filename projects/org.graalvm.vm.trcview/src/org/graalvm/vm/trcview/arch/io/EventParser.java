package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;

public abstract class EventParser {
    public abstract <T extends Event> T parse(WordInputStream in, byte id, int tid) throws IOException;

    public abstract <T extends StepEvent> T parseStep(WordInputStream in, int tid, long step, long pc, InstructionType type, byte[] machinecode) throws IOException;
}
