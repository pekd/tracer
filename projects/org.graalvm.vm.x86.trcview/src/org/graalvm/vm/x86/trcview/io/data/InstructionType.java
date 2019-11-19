package org.graalvm.vm.x86.trcview.io.data;

public enum InstructionType {
    JCC,
    JMP,
    JMP_INDIRECT,
    CALL,
    RET,
    SYSCALL,
    OTHER
}
