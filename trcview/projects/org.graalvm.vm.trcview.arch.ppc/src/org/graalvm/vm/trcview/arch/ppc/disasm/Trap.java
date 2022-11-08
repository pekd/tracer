package org.graalvm.vm.trcview.arch.ppc.disasm;

public class Trap {
    public static String decodeTO(int to) {
        switch (to) {
            case 16:
                return "lt";
            case 20:
                return "le";
            case 4:
                return "eq";
            case 12:
                return "ge";
            case 8:
                return "gt";
            case 24:
                return "ne";
            case 2:
                return "llt";
            case 6:
                return "lle";
            case 5:
                return "lge";
            case 1:
                return "lgt";
            case 31:
                return "u";
            default:
                return null;
        }
    }
}
