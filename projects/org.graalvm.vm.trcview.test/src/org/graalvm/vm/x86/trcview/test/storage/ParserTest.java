package org.graalvm.vm.x86.trcview.test.storage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.EofEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.trcview.storage.Step;
import org.graalvm.vm.trcview.storage.StorageBackend;
import org.graalvm.vm.trcview.storage.TraceParser;
import org.graalvm.vm.x86.trcview.test.mock.MockArchitecture;
import org.graalvm.vm.x86.trcview.test.mock.MockCpuState;
import org.graalvm.vm.x86.trcview.test.mock.MockStepEvent;
import org.graalvm.vm.x86.trcview.test.mock.MockStorageBackend;
import org.graalvm.vm.x86.trcview.test.mock.MockTraceReader;
import org.graalvm.vm.x86.trcview.test.mock.MockTrapEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParserTest {
    private Architecture arch;
    private MockStorageBackend storage;
    private List<Event> events;
    private List<Step> steps;
    private StepEvent lastStep;

    @Before
    public void setup() {
        arch = new MockArchitecture(false, true);
        storage = new MockStorageBackend();
        events = new ArrayList<>();
        steps = new ArrayList<>();
        Architecture.register(arch);
    }

    @After
    public void teardown() {
        Architecture.unregister(arch);
    }

    private void add(Event evt) {
        events.add(evt);
        if (evt instanceof StepEvent) {
            lastStep = (StepEvent) evt;
        }
    }

    private void step(long step, long parent, long pc, int type, byte[] machinecode, byte[] cpustate) {
        steps.add(new Step(0, step, parent, pc, type, machinecode, cpustate));
    }

    private void stepEvent(long step, long pc, InstructionType type, byte[] machinecode) {
        MockCpuState state = new MockCpuState((short) 0, 0);
        state.step = step;
        state.pc = pc;
        add(new MockStepEvent(state, machinecode, type));
    }

    private void trapEvent() {
        add(new MockTrapEvent((short) 0, 0, lastStep));
    }

    private static String str(byte[] code) {
        return "{" + IntStream.range(0, code.length).map(i -> Byte.toUnsignedInt(code[i])).mapToObj(x -> String.format("%02x", x)).collect(Collectors.joining(" ")) + "}";
    }

    private static String str(Step step) {
        return String.format("Step[id=%d, parent=%d, pc=0x%x, type=%d, machinecode=%s, state=%s]", step.step, step.parent, step.pc, step.type, str(step.machinecode), str(step.cpustate));
    }

    private void check() {
        List<Step> actsteps = storage.getSteps();
        assertEquals(steps.size(), actsteps.size());
        for (int i = 0; i < actsteps.size(); i++) {
            Step actstep = actsteps.get(i);
            Step refstep = steps.get(i);
            String msg = String.format("step %d: Ref %s vs Act %s", i, str(refstep), str(actstep));
            assertEquals(str(refstep), str(actstep));

            assertEquals(msg, refstep.step, actstep.step);
            assertEquals(msg, refstep.parent, actstep.parent);
            assertEquals(msg, refstep.pc, actstep.pc);
            assertEquals(msg, refstep.type, actstep.type);
            assertArrayEquals(msg, refstep.machinecode, actstep.machinecode);
            assertArrayEquals(msg, refstep.cpustate, actstep.cpustate);
        }
    }

    private void run() throws IOException {
        TraceReader reader = new MockTraceReader(events, arch);
        TraceParser parser = new TraceParser(reader, null, storage);
        parser.read();
        check();
    }

    @Test
    public void empty() throws IOException {
        add(new EofEvent());
        run();
    }

    @Test
    public void basicBlock() throws IOException {
        stepEvent(0, 0x1000, InstructionType.OTHER, new byte[]{0x42});
        stepEvent(1, 0x1001, InstructionType.OTHER, new byte[]{0x43});
        stepEvent(2, 0x1002, InstructionType.OTHER, new byte[]{0x44});
        step(0, -1, 0x1000, StorageBackend.TYPE_OTHER, new byte[]{0x42}, new byte[]{});
        step(1, -1, 0x1001, StorageBackend.TYPE_OTHER, new byte[]{0x43}, new byte[]{});
        step(2, -1, 0x1002, StorageBackend.TYPE_OTHER, new byte[]{0x44}, new byte[]{});
        run();
    }

    @Test
    public void call() throws IOException {
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
        step(0, -1, 0x1000, StorageBackend.TYPE_OTHER, new byte[]{0x42}, new byte[]{});
        step(1, -1, 0x1001, StorageBackend.TYPE_CALL, new byte[]{0x43}, new byte[]{});
        step(2, 1, 0x1002, StorageBackend.TYPE_OTHER, new byte[]{0x44}, new byte[]{});
        step(3, 1, 0x1003, StorageBackend.TYPE_RET, new byte[]{0x45}, new byte[]{});
        step(4, -1, 0x1004, StorageBackend.TYPE_OTHER, new byte[]{0x46}, new byte[]{});
        step(5, -1, 0x1005, StorageBackend.TYPE_OTHER, new byte[]{0x47}, new byte[]{});
        step(6, -1, 0x1006, StorageBackend.TYPE_CALL, new byte[]{0x48}, new byte[]{});
        step(7, 6, 0x1007, StorageBackend.TYPE_CALL, new byte[]{0x49}, new byte[]{});
        step(8, 7, 0x1008, StorageBackend.TYPE_OTHER, new byte[]{0x4A}, new byte[]{});
        step(9, 7, 0x1009, StorageBackend.TYPE_CALL, new byte[]{0x4B}, new byte[]{});
        step(10, 9, 0x100A, StorageBackend.TYPE_RET, new byte[]{0x4C}, new byte[]{});
        step(11, 7, 0x100B, StorageBackend.TYPE_RET, new byte[]{0x4D}, new byte[]{});
        step(12, 6, 0x100C, StorageBackend.TYPE_OTHER, new byte[]{0x4E}, new byte[]{});
        step(13, 6, 0x100D, StorageBackend.TYPE_RET, new byte[]{0x4F}, new byte[]{});
        step(14, -1, 0x100E, StorageBackend.TYPE_RET, new byte[]{0x50}, new byte[]{});
        step(15, -1, 0x100F, StorageBackend.TYPE_OTHER, new byte[]{0x51}, new byte[]{});
        step(16, -1, 0x1010, StorageBackend.TYPE_OTHER, new byte[]{0x52}, new byte[]{});
        step(17, -1, 0x1011, StorageBackend.TYPE_CALL, new byte[]{0x53}, new byte[]{});
        step(18, 17, 0x1012, StorageBackend.TYPE_OTHER, new byte[]{0x54}, new byte[]{});
        run();
    }

    @Test
    public void syscall() throws IOException {
        stepEvent(0, 0x1000, InstructionType.OTHER, new byte[]{0x42});
        stepEvent(1, 0x1001, InstructionType.CALL, new byte[]{0x43});
        stepEvent(2, 0x1002, InstructionType.OTHER, new byte[]{0x44});
        stepEvent(3, 0x1003, InstructionType.SYSCALL, new byte[]{0x45});
        trapEvent();
        stepEvent(4, 0x1004, InstructionType.OTHER, new byte[]{0x46});
        stepEvent(5, 0x1005, InstructionType.OTHER, new byte[]{0x47});
        stepEvent(6, 0x1006, InstructionType.RTI, new byte[]{0x48});
        stepEvent(7, 0x1007, InstructionType.OTHER, new byte[]{0x49});
        stepEvent(8, 0x1008, InstructionType.RET, new byte[]{0x4A});
        stepEvent(9, 0x1009, InstructionType.OTHER, new byte[]{0x4B});
        step(0, -1, 0x1000, StorageBackend.TYPE_OTHER, new byte[]{0x42}, new byte[]{});
        step(1, -1, 0x1001, StorageBackend.TYPE_CALL, new byte[]{0x43}, new byte[]{});
        step(2, 1, 0x1002, StorageBackend.TYPE_OTHER, new byte[]{0x44}, new byte[]{});
        step(3, 1, 0x1003, StorageBackend.TYPE_SYSCALL, new byte[]{0x45}, new byte[]{});
        step(4, 3, 0x1004, StorageBackend.TYPE_OTHER, new byte[]{0x46}, new byte[]{});
        step(5, 3, 0x1005, StorageBackend.TYPE_OTHER, new byte[]{0x47}, new byte[]{});
        step(6, 3, 0x1006, StorageBackend.TYPE_RTI, new byte[]{0x48}, new byte[]{});
        step(7, 1, 0x1007, StorageBackend.TYPE_OTHER, new byte[]{0x49}, new byte[]{});
        step(8, 1, 0x1008, StorageBackend.TYPE_RET, new byte[]{0x4A}, new byte[]{});
        step(9, -1, 0x1009, StorageBackend.TYPE_OTHER, new byte[]{0x4B}, new byte[]{});
        run();
    }

    @Test
    public void syscallNoTrap() throws IOException {
        stepEvent(0, 0x1000, InstructionType.OTHER, new byte[]{0x42});
        stepEvent(1, 0x1001, InstructionType.CALL, new byte[]{0x43});
        stepEvent(2, 0x1002, InstructionType.OTHER, new byte[]{0x44});
        stepEvent(3, 0x1003, InstructionType.SYSCALL, new byte[]{0x45});
        stepEvent(4, 0x1004, InstructionType.OTHER, new byte[]{0x46});
        stepEvent(5, 0x1005, InstructionType.OTHER, new byte[]{0x47});
        stepEvent(6, 0x1006, InstructionType.RTI, new byte[]{0x48});
        stepEvent(7, 0x1007, InstructionType.OTHER, new byte[]{0x49});
        stepEvent(8, 0x1008, InstructionType.RET, new byte[]{0x4A});
        stepEvent(9, 0x1009, InstructionType.OTHER, new byte[]{0x4B});
        step(0, -1, 0x1000, StorageBackend.TYPE_OTHER, new byte[]{0x42}, new byte[]{});
        step(1, -1, 0x1001, StorageBackend.TYPE_CALL, new byte[]{0x43}, new byte[]{});
        step(2, 1, 0x1002, StorageBackend.TYPE_OTHER, new byte[]{0x44}, new byte[]{});
        step(3, 1, 0x1003, StorageBackend.TYPE_SYSCALL, new byte[]{0x45}, new byte[]{});
        step(4, 3, 0x1004, StorageBackend.TYPE_OTHER, new byte[]{0x46}, new byte[]{});
        step(5, 3, 0x1005, StorageBackend.TYPE_OTHER, new byte[]{0x47}, new byte[]{});
        step(6, 3, 0x1006, StorageBackend.TYPE_RTI, new byte[]{0x48}, new byte[]{});
        step(7, 1, 0x1007, StorageBackend.TYPE_OTHER, new byte[]{0x49}, new byte[]{});
        step(8, 1, 0x1008, StorageBackend.TYPE_RET, new byte[]{0x4A}, new byte[]{});
        step(9, -1, 0x1009, StorageBackend.TYPE_OTHER, new byte[]{0x4B}, new byte[]{});
        run();
    }

    @Test
    public void trap() throws IOException {
        stepEvent(0, 0x1000, InstructionType.OTHER, new byte[]{0x42});
        stepEvent(1, 0x1001, InstructionType.OTHER, new byte[]{0x43});
        trapEvent();
        stepEvent(2, 0x1002, InstructionType.OTHER, new byte[]{0x44});
        stepEvent(3, 0x1003, InstructionType.OTHER, new byte[]{0x45});
        stepEvent(4, 0x1004, InstructionType.RTI, new byte[]{0x46});
        stepEvent(5, 0x1005, InstructionType.OTHER, new byte[]{0x47});
        stepEvent(6, 0x1006, InstructionType.OTHER, new byte[]{0x48});
        step(0, -1, 0x1000, StorageBackend.TYPE_OTHER, new byte[]{0x42}, new byte[]{});
        step(1, -1, 0x1001, StorageBackend.TYPE_OTHER, new byte[]{0x43}, new byte[]{});
        step(1, -1, 0x1001, StorageBackend.TYPE_TRAP, new byte[]{0x43}, new byte[]{});
        step(2, 1, 0x1002, StorageBackend.TYPE_OTHER, new byte[]{0x44}, new byte[]{});
        step(3, 1, 0x1003, StorageBackend.TYPE_OTHER, new byte[]{0x45}, new byte[]{});
        step(4, 1, 0x1004, StorageBackend.TYPE_RTI, new byte[]{0x46}, new byte[]{});
        step(5, -1, 0x1005, StorageBackend.TYPE_OTHER, new byte[]{0x47}, new byte[]{});
        step(6, -1, 0x1006, StorageBackend.TYPE_OTHER, new byte[]{0x48}, new byte[]{});
        run();
    }
}
