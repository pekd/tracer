package org.graalvm.vm.x86.trcview.test.storage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.trcview.storage.MemoryBackend;
import org.graalvm.vm.trcview.storage.Step;
import org.graalvm.vm.trcview.storage.StorageBackend;
import org.graalvm.vm.trcview.storage.TraceParser;
import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.x86.trcview.test.mock.MockArchitecture;
import org.graalvm.vm.x86.trcview.test.mock.MockCpuState;
import org.graalvm.vm.x86.trcview.test.mock.MockStepEvent;
import org.graalvm.vm.x86.trcview.test.mock.MockTraceReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StorageTest {
    private Architecture arch;
    private StorageBackend storage;
    private List<Event> events;

    @Before
    public void setup() {
        arch = new MockArchitecture(false, true);
        storage = new MemoryBackend();
        events = new ArrayList<>();
        Architecture.register(arch);
        storage.create("test", arch.getId());
    }

    @After
    public void teardown() {
        Architecture.unregister(arch);
    }

    private void add(Event evt) {
        events.add(evt);
    }

    private void stepEvent(long step, long pc, InstructionType type, byte[] machinecode) {
        MockCpuState state = new MockCpuState((short) 0, 0);
        state.step = step;
        state.pc = pc;
        state.data = new byte[machinecode.length + 16];
        Endianess.set64bitBE(state.data, 0, step);
        Endianess.set64bitBE(state.data, 8, pc);
        System.arraycopy(machinecode, 0, state.data, 16, machinecode.length);
        add(new MockStepEvent(state, machinecode, type));
    }

    private void run() throws IOException {
        TraceReader reader = new MockTraceReader(events, arch);
        TraceParser parser = new TraceParser(reader, null, storage);
        parser.read();
    }

    @Test
    public void call() throws IOException {
        // insert steps
        stepEvent(0, 0x1000, InstructionType.OTHER, new byte[]{0x42});
        stepEvent(1, 0x1001, InstructionType.CALL, new byte[]{0x43});
        stepEvent(2, 0x1002, InstructionType.OTHER, new byte[]{0x44});
        stepEvent(3, 0x1003, InstructionType.RET, new byte[]{0x45});
        stepEvent(4, 0x1004, InstructionType.OTHER, new byte[]{0x46});
        stepEvent(5, 0x1005, InstructionType.OTHER, new byte[]{0x47});
        stepEvent(6, 0x1006, InstructionType.CALL, new byte[]{0x48});
        stepEvent(7, 0x1007, InstructionType.CALL, new byte[]{0x49});
        stepEvent(8, 0x1008, InstructionType.OTHER, new byte[]{0x4A});
        stepEvent(9, 0x1009, InstructionType.CALL, new byte[]{0x4B});
        stepEvent(10, 0x100A, InstructionType.RET, new byte[]{0x4C});
        stepEvent(11, 0x100B, InstructionType.RET, new byte[]{0x4D});
        stepEvent(12, 0x100C, InstructionType.OTHER, new byte[]{0x4E});
        stepEvent(13, 0x100D, InstructionType.RET, new byte[]{0x4F});
        stepEvent(14, 0x100E, InstructionType.RET, new byte[]{0x50});
        stepEvent(15, 0x100F, InstructionType.OTHER, new byte[]{0x51});
        stepEvent(16, 0x1010, InstructionType.OTHER, new byte[]{0x52});
        stepEvent(17, 0x1011, InstructionType.CALL, new byte[]{0x53});
        stepEvent(18, 0x1012, InstructionType.OTHER, new byte[]{0x54});
        run();

        // now retrieve them again
        assertEquals(19, storage.getStepCount());
        List<Step> steps = storage.getSteps(-1, 0, 10);
        assertEquals(9, steps.size());

        StepEvent step = StorageBackend.getStep(steps.get(0), arch.getId());
        assertSame(arch, step.getArchitecture());
        assertEquals(0, step.getStep());
        assertEquals(0x1000, step.getPC());
        assertEquals(InstructionType.OTHER, step.getType());
        assertArrayEquals(new byte[]{0x42}, step.getMachinecode());
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* step */
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, /* pc */
                        0x42 /* machinecode */
        }, ((MockCpuState) step.getState()).data);
        assertEquals(0, step.getState().getStep());
        assertEquals(0x1000, step.getState().getPC());

        steps = storage.getSteps(1, 0, 10);
        assertEquals(2, steps.size());
    }
}
