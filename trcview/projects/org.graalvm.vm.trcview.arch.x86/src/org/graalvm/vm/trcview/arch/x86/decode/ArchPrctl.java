package org.graalvm.vm.trcview.arch.x86.decode;

public class ArchPrctl {
    public static final int ARCH_SET_GS = 0x1001;
    public static final int ARCH_SET_FS = 0x1002;
    public static final int ARCH_GET_FS = 0x1003;
    public static final int ARCH_GET_GS = 0x1004;

    public static final int ARCH_CET_STATUS = 0x3001;
    public static final int ARCH_CET_DISABLE = 0x3002;
    public static final int ARCH_CET_LOCK = 0x3003;
    public static final int ARCH_CET_ALLOC_SHSTK = 0x3004;
    public static final int ARCH_CET_LEGACY_BITMAP = 0x3005;
}
