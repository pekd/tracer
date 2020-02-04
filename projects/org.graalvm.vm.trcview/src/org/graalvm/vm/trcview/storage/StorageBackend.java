package org.graalvm.vm.trcview.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.BEOutputStream;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public abstract class StorageBackend implements Closeable {
    public static final int TYPE_OTHER = 0;
    public static final int TYPE_JCC = 1;
    public static final int TYPE_JMP = 2;
    public static final int TYPE_JMP_INDIRECT = 3;
    public static final int TYPE_CALL = 4;
    public static final int TYPE_RET = 5;
    public static final int TYPE_SYSCALL = 6;
    public static final int TYPE_RTI = 7;
    public static final int TYPE_TRAP = 8;

    private static final Logger log = Trace.create(StorageBackend.class);

    public static final int getInstructionType(InstructionType type) {
        switch (type) {
            default:
            case OTHER:
                return TYPE_OTHER;
            case JCC:
                return TYPE_JCC;
            case JMP:
                return TYPE_JMP;
            case JMP_INDIRECT:
                return TYPE_JMP_INDIRECT;
            case CALL:
                return TYPE_CALL;
            case RET:
                return TYPE_RET;
            case SYSCALL:
                return TYPE_SYSCALL;
            case RTI:
                return TYPE_RTI;
        }
    }

    public static final InstructionType getInstructionType(int type) {
        switch (type) {
            default:
            case TYPE_OTHER:
                return InstructionType.OTHER;
            case TYPE_JCC:
                return InstructionType.JCC;
            case TYPE_JMP:
                return InstructionType.JMP;
            case TYPE_JMP_INDIRECT:
                return InstructionType.JMP_INDIRECT;
            case TYPE_CALL:
                return InstructionType.CALL;
            case TYPE_RET:
                return InstructionType.RET;
            case TYPE_SYSCALL:
                return InstructionType.SYSCALL;
            case TYPE_RTI:
                return InstructionType.RTI;
        }
    }

    public static byte[] serialize(Event state) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            WordOutputStream wos = new BEOutputStream(bos);
            state.writeRaw(wos);
            wos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            log.log(Levels.FATAL, "Error while serializing event: " + e.getMessage(), e);
            return null;
        }
    }

    public static final <T extends StepEvent> T getStep(Step step, short archId) {
        Architecture arch = Architecture.getArchitecture(archId);
        try (WordInputStream in = new BEInputStream(new ByteArrayInputStream(step.cpustate))) {
            return arch.getEventParser().parseStep(in, step.tid, step.step, step.pc, getInstructionType(step.type), step.machinecode);
        } catch (IOException e) {
            log.log(Levels.WARNING, "Error while parsing step event: " + e.getMessage(), e);
            return null;
        }
    }

    public abstract List<TraceMetadata> list();

    public abstract void connect(String name);

    public abstract void create(String name, short arch);

    public abstract void createStep(int tid, long step, long parent, long pc, int type, byte[] machinecode, byte[] cpustate);

    public abstract void flush();

    public abstract List<Step> getSteps(long parent, long start, long count);

    public abstract short getArchitecture();

    public abstract long getStepCount();
}
