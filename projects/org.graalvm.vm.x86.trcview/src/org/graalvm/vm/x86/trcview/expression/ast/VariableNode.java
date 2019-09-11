package org.graalvm.vm.x86.trcview.expression.ast;

import org.graalvm.vm.x86.trcview.expression.EvaluationException;
import org.graalvm.vm.x86.trcview.expression.ExpressionContext;
import org.graalvm.vm.x86.trcview.expression.UnknownVariableException;

public class VariableNode extends Expression {
    public final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public long evaluate(ExpressionContext ctx) throws EvaluationException {
        switch (name) {
            case "$":
                return ctx.step.getPC();
            case "al":
                return ctx.step.getState().getState().rax & 0xFF;
            case "ax":
                return ctx.step.getState().getState().rax & 0xFFFF;
            case "eax":
                return ctx.step.getState().getState().rax & 0xFFFFFFFFL;
            case "rax":
                return ctx.step.getState().getState().rax;
            case "bl":
                return ctx.step.getState().getState().rbx & 0xFF;
            case "bx":
                return ctx.step.getState().getState().rbx & 0xFFFF;
            case "ebx":
                return ctx.step.getState().getState().rbx & 0xFFFFFFFFL;
            case "rbx":
                return ctx.step.getState().getState().rbx;
            case "cl":
                return ctx.step.getState().getState().rcx & 0xFF;
            case "cx":
                return ctx.step.getState().getState().rcx & 0xFFFF;
            case "ecx":
                return ctx.step.getState().getState().rcx & 0xFFFFFFFFL;
            case "rcx":
                return ctx.step.getState().getState().rcx;
            case "dl":
                return ctx.step.getState().getState().rdx & 0xFF;
            case "dx":
                return ctx.step.getState().getState().rdx & 0xFFFF;
            case "edx":
                return ctx.step.getState().getState().rdx & 0xFFFFFFFFL;
            case "rdx":
                return ctx.step.getState().getState().rdx;
            case "bpl":
                return ctx.step.getState().getState().rbp & 0xFF;
            case "bp":
                return ctx.step.getState().getState().rbp & 0xFFFF;
            case "ebp":
                return ctx.step.getState().getState().rbp & 0xFFFFFFFFL;
            case "rbp":
                return ctx.step.getState().getState().rsp;
            case "spl":
                return ctx.step.getState().getState().rsp & 0xFF;
            case "sp":
                return ctx.step.getState().getState().rsp & 0xFFFF;
            case "esp":
                return ctx.step.getState().getState().rsp & 0xFFFFFFFFL;
            case "rsp":
                return ctx.step.getState().getState().rsp;
            case "ip":
                return ctx.step.getState().getState().rip & 0xFFFF;
            case "eip":
                return ctx.step.getState().getState().rip & 0xFFFFFFFFL;
            case "rip":
                return ctx.step.getState().getState().rip;
            case "sil":
                return ctx.step.getState().getState().rsi & 0xFF;
            case "si":
                return ctx.step.getState().getState().rsi & 0xFFFF;
            case "esi":
                return ctx.step.getState().getState().rsi & 0xFFFFFFFFL;
            case "rsi":
                return ctx.step.getState().getState().rsi;
            case "dil":
                return ctx.step.getState().getState().rdi & 0xFF;
            case "di":
                return ctx.step.getState().getState().rdi & 0xFFFF;
            case "edi":
                return ctx.step.getState().getState().rdi & 0xFFFFFFFFL;
            case "rdi":
                return ctx.step.getState().getState().rdi;
            case "r8b":
                return ctx.step.getState().getState().r8 & 0xFF;
            case "r8w":
                return ctx.step.getState().getState().r8 & 0xFFFF;
            case "r8d":
                return ctx.step.getState().getState().r8 & 0xFFFFFFFFL;
            case "r8":
                return ctx.step.getState().getState().r8;
            case "r9b":
                return ctx.step.getState().getState().r9 & 0xFF;
            case "r9w":
                return ctx.step.getState().getState().r9 & 0xFFFF;
            case "r9d":
                return ctx.step.getState().getState().r9 & 0xFFFFFFFFL;
            case "r9":
                return ctx.step.getState().getState().r9;
            case "r10b":
                return ctx.step.getState().getState().r10 & 0xFF;
            case "r10w":
                return ctx.step.getState().getState().r10 & 0xFFFF;
            case "r10d":
                return ctx.step.getState().getState().r10 & 0xFFFFFFFFL;
            case "r10":
                return ctx.step.getState().getState().r10;
            case "r11b":
                return ctx.step.getState().getState().r11 & 0xFF;
            case "r11w":
                return ctx.step.getState().getState().r11 & 0xFFFF;
            case "r11d":
                return ctx.step.getState().getState().r11 & 0xFFFFFFFFL;
            case "r11":
                return ctx.step.getState().getState().r11;
            case "r12b":
                return ctx.step.getState().getState().r12 & 0xFF;
            case "r12w":
                return ctx.step.getState().getState().r12 & 0xFFFF;
            case "r12d":
                return ctx.step.getState().getState().r12 & 0xFFFFFFFFL;
            case "r12":
                return ctx.step.getState().getState().r12;
            case "r13b":
                return ctx.step.getState().getState().r13 & 0xFF;
            case "r13w":
                return ctx.step.getState().getState().r13 & 0xFFFF;
            case "r13d":
                return ctx.step.getState().getState().r13 & 0xFFFFFFFFL;
            case "r13":
                return ctx.step.getState().getState().r13;
            case "r14b":
                return ctx.step.getState().getState().r14 & 0xFF;
            case "r14w":
                return ctx.step.getState().getState().r14 & 0xFFFF;
            case "r14d":
                return ctx.step.getState().getState().r14 & 0xFFFFFFFFL;
            case "r14":
                return ctx.step.getState().getState().r14;
            case "r15b":
                return ctx.step.getState().getState().r15 & 0xFF;
            case "r15w":
                return ctx.step.getState().getState().r15 & 0xFFFF;
            case "r15d":
                return ctx.step.getState().getState().r15 & 0xFFFFFFFFL;
            case "r15":
                return ctx.step.getState().getState().r15;
            case "flags":
                return ctx.step.getState().getState().getRFL() & 0xFFFF;
            case "eflags":
                return ctx.step.getState().getState().getRFL() & 0xFFFFFFFFL;
            case "rflags":
                return ctx.step.getState().getState().getRFL();
            default:
                throw new UnknownVariableException(name);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
