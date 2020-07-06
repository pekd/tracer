package org.graalvm.vm.trcview.arch.pdp11.device;

public class KD11 {
    public static String getTrapName(short trap) {
        switch (trap) {
            case 0004:
                return "Bus error / timeout";
            case 0010:
                return "Reserved instruction";
            case 0014:
                return "BPT trap instruction / T bit";
            case 0020:
                return "IOT executed";
            case 024:
                return "Power fail";
            case 030:
                return "EMT executed";
            case 0034:
                return "TRAP executed";
            case 0060:
                return "Console input device";
            case 0064:
                return "Console output device";
            case 0100:
                return "External event line interrupt";
            case 0244:
                return "FIS trap";
            default:
                return null;
        }
    }
}
