package org.graalvm.vm.trcview.arch.io;

import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.OctFormatter;
import org.graalvm.vm.util.io.Endianess;

public class StepFormat {
    public static final int NUMBERFMT_HEX = 0;
    public static final int NUMBERFMT_OCT = 1;

    public final int numberfmt;
    public final int addrwidth;
    public final int wordwidth;
    public final int machinecodesz;
    public final boolean be;

    public StepFormat(int numberfmt, int addrwidth, int wordwidth, int machinecodesz, boolean be) {
        this.numberfmt = numberfmt;
        this.addrwidth = addrwidth;
        this.wordwidth = wordwidth;
        this.machinecodesz = machinecodesz;
        this.be = be;
    }

    public String formatAddress(long addr) {
        if (numberfmt == NUMBERFMT_OCT) {
            return OctFormatter.tooct(addr, addrwidth);
        } else {
            return HexFormatter.tohex(addr, addrwidth);
        }
    }

    public String formatShortAddress(long addr) {
        if (numberfmt == NUMBERFMT_OCT) {
            return OctFormatter.tooct(addr);
        } else {
            return HexFormatter.tohex(addr);
        }
    }

    public String formatWord(long data) {
        if (numberfmt == NUMBERFMT_OCT) {
            return OctFormatter.tooct(data, wordwidth);
        } else {
            return HexFormatter.tohex(data, wordwidth);
        }
    }

    public String formatCode(byte[] machinecode) {
        if (machinecodesz == 1) {
            return getPrintableBytes(machinecode);
        } else if (machinecodesz == 2) {
            return getPrintableWords(machinecode);
        } else if (machinecodesz == 4) {
            return getPrintableWords4(machinecode);
        } else {
            return getPrintableBytes(machinecode);
        }
    }

    public String getPrintableBytes(byte[] machinecode) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < machinecode.length; i++) {
            buf.append(' ');
            if (numberfmt == NUMBERFMT_OCT) {
                buf.append(OctFormatter.tooct(Byte.toUnsignedInt(machinecode[i]), 3));
            } else {
                buf.append(HexFormatter.tohex(Byte.toUnsignedInt(machinecode[i]), 2));
            }
        }
        return buf.toString().substring(1);
    }

    public String getPrintableWords(byte[] machinecode) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < (machinecode.length / 2); i++) {
            short word;
            if (be) {
                word = Endianess.get16bitBE(machinecode, 2 * i);
            } else {
                word = Endianess.get16bitLE(machinecode, 2 * i);
            }
            buf.append(' ');
            if (numberfmt == NUMBERFMT_OCT) {
                buf.append(OctFormatter.tooct(Short.toUnsignedInt(word), 6));
            } else {
                buf.append(HexFormatter.tohex(Short.toUnsignedInt(word), 4));
            }
        }
        return buf.toString().substring(1);
    }

    public String getPrintableWords4(byte[] machinecode) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < (machinecode.length / 4); i++) {
            int word;
            if (be) {
                word = Endianess.get32bitBE(machinecode, 4 * i);
            } else {
                word = Endianess.get32bitLE(machinecode, 4 * i);
            }
            buf.append(' ');
            if (numberfmt == NUMBERFMT_OCT) {
                buf.append(OctFormatter.tooct(Integer.toUnsignedLong(word), 11));
            } else {
                buf.append(HexFormatter.tohex(Integer.toUnsignedLong(word), 8));
            }
        }
        return buf.toString().substring(1);
    }
}
