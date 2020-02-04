package org.graalvm.vm.x86.trcview.test.mock;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.EventParser;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.io.WordInputStream;

public class MockEventParser extends EventParser {

    @Override
    public <T extends Event> T parse(WordInputStream in, byte id, int tid) throws IOException {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends StepEvent> T parseStep(WordInputStream in, int tid, long step, long pc, InstructionType type, byte[] machinecode) {
        return (T) MockStepEvent.create(tid, step, pc, type, machinecode);
    }
}
