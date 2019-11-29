package org.graalvm.vm.trcview.arch.io;

public enum InstructionType {
    JCC,
    JMP,
    JMP_INDIRECT,
    CALL,
    RET,
    SYSCALL,
    RTI,
    OTHER
}
