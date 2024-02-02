package org.graalvm.vm.trcview.disasm;

import org.graalvm.vm.trcview.arch.Disassembler;

public class AssemblerInstruction {
    private String mnemonic;
    private Operand[] operands;
    private long pc;
    private Disassembler disasm;

    public AssemblerInstruction(String mnemonic) {
        this.mnemonic = mnemonic;
        operands = new Operand[0];
    }

    public AssemblerInstruction(String mnemonic, Token[] tokens) {
        this.mnemonic = mnemonic;
        this.operands = new Operand[]{new Operand(tokens)};
    }

    public AssemblerInstruction(String mnemonic, Operand... operands) {
        this.mnemonic = mnemonic;
        this.operands = operands;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public Operand[] getOperands() {
        for (Operand o : operands) {
            o.setPC(pc);
            o.setDisassembler(disasm);
        }
        return operands;
    }

    public String[] getComponents() {
        Operand[] ops = getOperands();
        String[] result = new String[ops.length + 1];
        for (int i = 0; i < ops.length; i++) {
            result[i + 1] = ops[i].toString();
        }
        result[0] = mnemonic;
        return result;
    }

    public void setPC(long pc) {
        this.pc = pc;
    }

    public void setDisassembler(Disassembler disasm) {
        this.disasm = disasm;
    }

    @Override
    public String toString() {
        Operand[] ops = getOperands();
        if (ops.length == 0) {
            return mnemonic;
        } else {
            String[] c = new String[ops.length];
            for (int i = 0; i < c.length; i++) {
                c[i] = ops[i].toString();
            }
            return mnemonic + " " + String.join(", ", c);
        }
    }
}
