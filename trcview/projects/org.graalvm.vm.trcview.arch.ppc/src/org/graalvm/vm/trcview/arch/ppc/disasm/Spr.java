package org.graalvm.vm.trcview.arch.ppc.disasm;

import java.util.HashMap;
import java.util.Map;

public class Spr {
    private static final Map<Integer, String> NAMES;

    static {
        NAMES = new HashMap<>();
        NAMES.put(1, "XER");
        NAMES.put(8, "LR");
        NAMES.put(9, "CTR");
        NAMES.put(18, "DSISR");
        NAMES.put(19, "DAR");
        NAMES.put(22, "DEC");
        NAMES.put(25, "SDR");
        NAMES.put(26, "SRR0");
        NAMES.put(27, "SRR1");
        NAMES.put(268, "TL");
        NAMES.put(269, "TU");
        NAMES.put(284, "TL_W");
        NAMES.put(285, "TU_W");
        NAMES.put(287, "PVR");
        NAMES.put(272, "SPRG0");
        NAMES.put(273, "SPRG1");
        NAMES.put(274, "SPRG2");
        NAMES.put(275, "SPRG3");
        NAMES.put(282, "EAR");
        NAMES.put(528, "IBAT0U");
        NAMES.put(529, "IBAT0L");
        NAMES.put(530, "IBAT1U");
        NAMES.put(531, "IBAT1L");
        NAMES.put(532, "IBAT2U");
        NAMES.put(533, "IBAT2L");
        NAMES.put(534, "IBAT3U");
        NAMES.put(535, "IBAT3L");
        NAMES.put(536, "DBAT0U");
        NAMES.put(537, "DBAT0L");
        NAMES.put(538, "DBAT1U");
        NAMES.put(539, "DBAT1L");
        NAMES.put(540, "DBAT2U");
        NAMES.put(541, "DBAT2L");
        NAMES.put(542, "DBAT3U");
        NAMES.put(543, "DBAT3L");
        NAMES.put(560, "IBAT4U");
        NAMES.put(561, "IBAT4L");
        NAMES.put(562, "IBAT5U");
        NAMES.put(563, "IBAT5L");
        NAMES.put(564, "IBAT6U");
        NAMES.put(565, "IBAT6L");
        NAMES.put(566, "IBAT7U");
        NAMES.put(567, "IBAT7L");
        NAMES.put(568, "DBAT4U");
        NAMES.put(569, "DBAT4L");
        NAMES.put(570, "DBAT5U");
        NAMES.put(571, "DBAT5L");
        NAMES.put(572, "DBAT6U");
        NAMES.put(573, "DBAT6L");
        NAMES.put(574, "DBAT7U");
        NAMES.put(575, "DBAT7L");
        NAMES.put(912, "GQR0");
        NAMES.put(1008, "HID0");
        NAMES.put(1009, "HID1");
        NAMES.put(920, "HID2");
        NAMES.put(1011, "HID4");
        NAMES.put(921, "WPAR");
        NAMES.put(922, "DMAU");
        NAMES.put(923, "DMAL");
        NAMES.put(924, "ECID_U");
        NAMES.put(925, "ECID_M");
        NAMES.put(926, "ECID_L");
        NAMES.put(1017, "L2CR");
        NAMES.put(936, "UMMCR0");
        NAMES.put(952, "MMCR0");
        NAMES.put(953, "PMC1");
        NAMES.put(954, "PMC2");
        NAMES.put(940, "UMMCR1");
        NAMES.put(956, "MMCR1");
        NAMES.put(957, "PMC3");
        NAMES.put(958, "PMC4");
    }

    public static String toString(int spr) {
        String name = NAMES.get(spr);
        if (name == null) {
            return Integer.toString(spr);
        } else {
            return name;
        }
    }
}
