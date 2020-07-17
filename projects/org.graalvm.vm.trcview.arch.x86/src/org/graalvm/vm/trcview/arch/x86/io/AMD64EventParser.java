package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.EventParser;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.io.WordInputStream;

public class AMD64EventParser extends EventParser {
    @Override
    public <T extends Event> T parse(WordInputStream in, byte id, int tid) throws IOException {
        return null;
    }

    @Override
    public <T extends StepEvent> T parseStep(WordInputStream in, int tid, long step, long pc, InstructionType type, byte[] machinecode) throws IOException {
        return null;
    }

    @Override
    public <T extends InterruptEvent> T parseTrap(WordInputStream in, int tid, long step, long pc, InstructionType type, byte[] machinecode) throws IOException {
        return null;
    }
}
