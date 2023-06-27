package org.graalvm.vm.posix.api.linux;

import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.util.HexFormatter;

public class Prctl {
    /* Values to pass as first argument to prctl() */
    public static final int PR_SET_PDEATHSIG = 1; /* Second arg is a signal */
    public static final int PR_GET_PDEATHSIG = 2; /* Second arg is a ptr to return the signal */

    /* Get/set current->mm->dumpable */
    public static final int PR_GET_DUMPABLE = 3;
    public static final int PR_SET_DUMPABLE = 4;

    /* Get/set unaligned access control bits (if meaningful) */
    public static final int PR_GET_UNALIGN = 5;
    public static final int PR_SET_UNALIGN = 6;
    public static final int PR_UNALIGN_NOPRINT = 1; /* silently fix up unaligned user accesses */
    public static final int PR_UNALIGN_SIGBUS = 2; /* generate SIGBUS on unaligned user access */

    /*
     * Get/set whether or not to drop capabilities on setuid() away from uid 0 (as per
     * security/commoncap.c)
     */
    public static final int PR_GET_KEEPCAPS = 7;
    public static final int PR_SET_KEEPCAPS = 8;

    /* Get/set floating-point emulation control bits (if meaningful) */
    public static final int PR_GET_FPEMU = 9;
    public static final int PR_SET_FPEMU = 10;
    public static final int PR_FPEMU_NOPRINT = 1; /* silently emulate fp operations accesses */
    public static final int PR_FPEMU_SIGFPE = 2; /*
                                                  * don't emulate fp operations, send SIGFPE instead
                                                  */

    /* Get/set floating-point exception mode (if meaningful) */
    public static final int PR_GET_FPEXC = 11;
    public static final int PR_SET_FPEXC = 12;
    public static final int PR_FP_EXC_SW_ENABLE = 0x80; /* Use FPEXC for FP exception enables */
    public static final int PR_FP_EXC_DIV = 0x010000; /* floating point divide by zero */
    public static final int PR_FP_EXC_OVF = 0x020000; /* floating point overflow */
    public static final int PR_FP_EXC_UND = 0x040000; /* floating point underflow */
    public static final int PR_FP_EXC_RES = 0x080000; /* floating point inexact result */
    public static final int PR_FP_EXC_INV = 0x100000; /* floating point invalid operation */
    public static final int PR_FP_EXC_DISABLED = 0; /* FP exceptions disabled */
    public static final int PR_FP_EXC_NONRECOV = 1; /* async non-recoverable exc. mode */
    public static final int PR_FP_EXC_ASYNC = 2; /* async recoverable exception mode */
    public static final int PR_FP_EXC_PRECISE = 3; /* precise exception mode */

    /*
     * Get/set whether we use statistical process timing or accurate timestamp based process timing
     */
    public static final int PR_GET_TIMING = 13;
    public static final int PR_SET_TIMING = 14;
    public static final int PR_TIMING_STATISTICAL = 0; /*
                                                        * Normal, traditional, statistical process
                                                        * timing
                                                        */
    public static final int PR_TIMING_TIMESTAMP = 1; /* Accurate timestamp based process timing */

    public static final int PR_SET_NAME = 15; /* Set process name */
    public static final int PR_GET_NAME = 16; /* Get process name */

    /* Get/set process endian */
    public static final int PR_GET_ENDIAN = 19;
    public static final int PR_SET_ENDIAN = 20;
    public static final int PR_ENDIAN_BIG = 0;
    public static final int PR_ENDIAN_LITTLE = 1; /* True little endian mode */
    public static final int PR_ENDIAN_PPC_LITTLE = 2; /* "PowerPC" pseudo little endian */

    /* Get/set process seccomp mode */
    public static final int PR_GET_SECCOMP = 21;
    public static final int PR_SET_SECCOMP = 22;

    /* Get/set the capability bounding set (as per security/commoncap.c) */
    public static final int PR_CAPBSET_READ = 23;
    public static final int PR_CAPBSET_DROP = 24;

    /* Get/set the process' ability to use the timestamp counter instruction */
    public static final int PR_GET_TSC = 25;
    public static final int PR_SET_TSC = 26;
    public static final int PR_TSC_ENABLE = 1; /* allow the use of the timestamp counter */
    public static final int PR_TSC_SIGSEGV = 2; /* throw a SIGSEGV instead of reading the TSC */

    /* Get/set securebits (as per security/commoncap.c) */
    public static final int PR_GET_SECUREBITS = 27;
    public static final int PR_SET_SECUREBITS = 28;

    /* Get/set the timerslack as used by poll/select/nanosleep A value of 0 means "use default" */
    public static final int PR_SET_TIMERSLACK = 29;
    public static final int PR_GET_TIMERSLACK = 30;

    public static final int PR_TASK_PERF_EVENTS_DISABLE = 31;
    public static final int PR_TASK_PERF_EVENTS_ENABLE = 32;

    /*
     * Set early/late kill mode for hwpoison memory corruption. This influences when the process
     * gets killed on a memory corruption.
     */
    public static final int PR_MCE_KILL = 33;
    public static final int PR_MCE_KILL_CLEAR = 0;
    public static final int PR_MCE_KILL_SET = 1;

    public static final int PR_MCE_KILL_LATE = 0;
    public static final int PR_MCE_KILL_EARLY = 1;
    public static final int PR_MCE_KILL_DEFAULT = 2;

    public static final int PR_MCE_KILL_GET = 34;

    /* Tune up process memory map specifics. */
    public static final int PR_SET_MM = 35;
    public static final int PR_SET_MM_START_CODE = 1;
    public static final int PR_SET_MM_END_CODE = 2;
    public static final int PR_SET_MM_START_DATA = 3;
    public static final int PR_SET_MM_END_DATA = 4;
    public static final int PR_SET_MM_START_STACK = 5;
    public static final int PR_SET_MM_START_BRK = 6;
    public static final int PR_SET_MM_BRK = 7;
    public static final int PR_SET_MM_ARG_START = 8;
    public static final int PR_SET_MM_ARG_END = 9;
    public static final int PR_SET_MM_ENV_START = 10;
    public static final int PR_SET_MM_ENV_END = 11;
    public static final int PR_SET_MM_AUXV = 12;
    public static final int PR_SET_MM_EXE_FILE = 13;
    public static final int PR_SET_MM_MAP = 14;
    public static final int PR_SET_MM_MAP_SIZE = 15;

    /*
     * Set specific pid that is allowed to ptrace the current task. A value of 0 mean "no process".
     */
    public static final int PR_SET_PTRACER = 0x59616d61;
    public static final long PR_SET_PTRACER_ANY = -1;

    public static final int PR_SET_CHILD_SUBREAPER = 36;
    public static final int PR_GET_CHILD_SUBREAPER = 37;

    /*
     * If no_new_privs is set, then operations that grant new privileges (i.e. execve) will either
     * fail or not grant them. This affects suid/sgid, file capabilities, and LSMs.
     *
     * Operations that merely manipulate or drop existing privileges (setresuid, capset, etc.) will
     * still work. Drop those privileges if you want them gone.
     *
     * Changing LSM security domain is considered a new privilege. So, for example, asking selinux
     * for a specific new context (e.g. with runcon) will result in execve returning -EPERM.
     *
     * See Documentation/userspace-api/no_new_privs.rst for more details.
     */
    public static final int PR_SET_NO_NEW_PRIVS = 38;
    public static final int PR_GET_NO_NEW_PRIVS = 39;

    public static final int PR_GET_TID_ADDRESS = 40;

    public static final int PR_SET_THP_DISABLE = 41;
    public static final int PR_GET_THP_DISABLE = 42;

    /*
     * Tell the kernel to start/stop helping userspace manage bounds tables.
     */
    public static final int PR_MPX_ENABLE_MANAGEMENT = 43;
    public static final int PR_MPX_DISABLE_MANAGEMENT = 44;

    public static final int PR_SET_FP_MODE = 45;
    public static final int PR_GET_FP_MODE = 46;
    public static final int PR_FP_MODE_FR = (1 << 0); /* 64b FP registers */
    public static final int PR_FP_MODE_FRE = (1 << 1); /* 32b compatibility */

    /* Control the ambient capability set */
    public static final int PR_CAP_AMBIENT = 47;
    public static final int PR_CAP_AMBIENT_IS_SET = 1;
    public static final int PR_CAP_AMBIENT_RAISE = 2;
    public static final int PR_CAP_AMBIENT_LOWER = 3;
    public static final int PR_CAP_AMBIENT_CLEAR_ALL = 4;

    /* arm64 Scalable Vector Extension controls */
    /* Flag values must be kept in sync with ptrace NT_ARM_SVE interface */
    public static final int PR_SVE_SET_VL = 50; /* set task vector length */
    public static final int PR_SVE_SET_VL_ONEXEC = 1 << 18; /* defer effect until exec */
    public static final int PR_SVE_GET_VL = 51; /* get task vector length */
    /* Bits common to PR_SVE_SET_VL and PR_SVE_GET_VL */
    public static final int PR_SVE_VL_LEN_MASK = 0xffff;
    public static final int PR_SVE_VL_INHERIT = 1 << 17; /* inherit across exec */

    /* Per task speculation control */
    public static final int PR_GET_SPECULATION_CTRL = 52;
    public static final int PR_SET_SPECULATION_CTRL = 53;
    /* Speculation control variants */
    public static final int PR_SPEC_STORE_BYPASS = 0;
    public static final int PR_SPEC_INDIRECT_BRANCH = 1;
    /* Return and control values for PR_SET/GET_SPECULATION_CTRL */
    public static final int PR_SPEC_NOT_AFFECTED = 0;
    public static final int PR_SPEC_PRCTL = 1 << 0;
    public static final int PR_SPEC_ENABLE = 1 << 1;
    public static final int PR_SPEC_DISABLE = 1 << 2;
    public static final int PR_SPEC_FORCE_DISABLE = 1 << 3;
    public static final int PR_SPEC_DISABLE_NOEXEC = 1 << 4;

    /* Reset arm64 pointer authentication keys */
    public static final int PR_PAC_RESET_KEYS = 54;
    public static final int PR_PAC_APIAKEY = 1 << 0;
    public static final int PR_PAC_APIBKEY = 1 << 1;
    public static final int PR_PAC_APDAKEY = 1 << 2;
    public static final int PR_PAC_APDBKEY = 1 << 3;
    public static final int PR_PAC_APGAKEY = 1 << 4;

    /* Tagged user address controls for arm64 */
    public static final int PR_SET_TAGGED_ADDR_CTRL = 55;
    public static final int PR_GET_TAGGED_ADDR_CTRL = 56;
    public static final int PR_TAGGED_ADDR_ENABLE = 1 << 0;
    /* MTE tag check fault modes */
    public static final int PR_MTE_TCF_NONE = 0;
    public static final int PR_MTE_TCF_SYNC = 1 << 1;
    public static final int PR_MTE_TCF_ASYNC = 1 << 2;
    public static final int PR_MTE_TCF_MASK = PR_MTE_TCF_SYNC | PR_MTE_TCF_ASYNC;
    /* MTE tag inclusion mask */
    public static final int PR_MTE_TAG_SHIFT = 3;
    public static final int PR_MTE_TAG_MASK = 0xffff << PR_MTE_TAG_SHIFT;
    /* Unused; kept only for source compatibility */
    public static final int PR_MTE_TCF_SHIFT = 1;

    /* Control reclaim behavior when allocating memory */
    public static final int PR_SET_IO_FLUSHER = 57;
    public static final int PR_GET_IO_FLUSHER = 58;

    /* Dispatch syscalls to a userspace handler */
    public static final int PR_SET_SYSCALL_USER_DISPATCH = 59;
    public static final int PR_SYS_DISPATCH_OFF = 0;
    public static final int PR_SYS_DISPATCH_ON = 1;
    /* The control values for the user space selector when dispatch is enabled */
    public static final int SYSCALL_DISPATCH_FILTER_ALLOW = 0;
    public static final int SYSCALL_DISPATCH_FILTER_BLOCK = 1;

    /* Set/get enabled arm64 pointer authentication keys */
    public static final int PR_PAC_SET_ENABLED_KEYS = 60;
    public static final int PR_PAC_GET_ENABLED_KEYS = 61;

    /* Request the scheduler to share a core */
    public static final int PR_SCHED_CORE = 62;
    public static final int PR_SCHED_CORE_GET = 0;
    public static final int PR_SCHED_CORE_CREATE = 1; /* create unique core_sched cookie */
    public static final int PR_SCHED_CORE_SHARE_TO = 2; /* push core_sched cookie to pid */
    public static final int PR_SCHED_CORE_SHARE_FROM = 3; /* pull core_sched cookie to pid */
    public static final int PR_SCHED_CORE_MAX = 4;
    public static final int PR_SCHED_CORE_SCOPE_THREAD = 0;
    public static final int PR_SCHED_CORE_SCOPE_THREAD_GROUP = 1;
    public static final int PR_SCHED_CORE_SCOPE_PROCESS_GROUP = 2;

    /* arm64 Scalable Matrix Extension controls */
    /* Flag values must be in sync with SVE versions */
    public static final int PR_SME_SET_VL = 63; /* set task vector length */
    public static final int PR_SME_SET_VL_ONEXEC = 1 << 18; /* defer effect until exec */
    public static final int PR_SME_GET_VL = 64; /* get task vector length */
    /* Bits common to PR_SME_SET_VL and PR_SME_GET_VL */
    public static final int PR_SME_VL_LEN_MASK = 0xffff;
    public static final int PR_SME_VL_INHERIT = 1 << 17; /* inherit across exec */

    /* Memory deny write / execute */
    public static final int PR_SET_MDWE = 65;
    public static final int PR_MDWE_REFUSE_EXEC_GAIN = 1;

    public static final int PR_GET_MDWE = 66;

    public static final int PR_SET_VMA = 0x53564d41;
    public static final int PR_SET_VMA_ANON_NAME = 0;

    private static final Map<Integer, String> PRCTL_OPTIONS = new HashMap<>();

    static {
        PRCTL_OPTIONS.put(PR_CAP_AMBIENT, "PR_CAP_AMBIENT");
        PRCTL_OPTIONS.put(PR_CAPBSET_READ, "PR_CAPBSET_READ");
        PRCTL_OPTIONS.put(PR_CAPBSET_DROP, "PR_CAPBSET_DROP");
        PRCTL_OPTIONS.put(PR_SET_CHILD_SUBREAPER, "PR_SET_CHILD_SUBREAPER");
        PRCTL_OPTIONS.put(PR_GET_CHILD_SUBREAPER, "PR_GET_CHILD_SUBREAPER");
        PRCTL_OPTIONS.put(PR_SET_DUMPABLE, "PR_SET_DUMPABLE");
        PRCTL_OPTIONS.put(PR_GET_DUMPABLE, "PR_GET_DUMPABLE");
        PRCTL_OPTIONS.put(PR_SET_ENDIAN, "PR_SET_ENDIAN");
        PRCTL_OPTIONS.put(PR_SET_FP_MODE, "PR_SET_FP_MODE");
        PRCTL_OPTIONS.put(PR_GET_FP_MODE, "PR_GET_FP_MODE");
        PRCTL_OPTIONS.put(PR_SET_FPEMU, "PR_SET_FPEMU");
        PRCTL_OPTIONS.put(PR_GET_FPEMU, "PR_GET_FPEMU");
        PRCTL_OPTIONS.put(PR_SET_FPEXC, "PR_SET_FPEXC");
        PRCTL_OPTIONS.put(PR_GET_FPEXC, "PR_GET_FPEXC");
        PRCTL_OPTIONS.put(PR_SET_IO_FLUSHER, "PR_SET_IO_FLUSHER");
        PRCTL_OPTIONS.put(PR_GET_IO_FLUSHER, "PR_GET_IO_FLUSHER");
        PRCTL_OPTIONS.put(PR_SET_KEEPCAPS, "PR_SET_KEEPCAPS");
        PRCTL_OPTIONS.put(PR_GET_KEEPCAPS, "PR_GET_KEEPCAPS");
        PRCTL_OPTIONS.put(PR_MCE_KILL, "PR_MCE_KILL");
        PRCTL_OPTIONS.put(PR_MCE_KILL_GET, "PR_MCE_KILL_GET");
        PRCTL_OPTIONS.put(PR_SET_MM, "PR_SET_MM");
        PRCTL_OPTIONS.put(PR_SET_VMA, "PR_SET_VMA");
        PRCTL_OPTIONS.put(PR_MPX_ENABLE_MANAGEMENT, "PR_MPX_ENABLE_MANAGEMENT");
        PRCTL_OPTIONS.put(PR_MPX_DISABLE_MANAGEMENT, "PR_MPX_DISABLE_MANAGEMENT");
        PRCTL_OPTIONS.put(PR_SET_NAME, "PR_SET_NAME");
        PRCTL_OPTIONS.put(PR_GET_NAME, "PR_GET_NAME");
        PRCTL_OPTIONS.put(PR_SET_NO_NEW_PRIVS, "PR_SET_NO_NEW_PRIVS");
        PRCTL_OPTIONS.put(PR_GET_NO_NEW_PRIVS, "PR_GET_NO_NEW_PRIVS");
        PRCTL_OPTIONS.put(PR_PAC_RESET_KEYS, "PR_PAC_RESET_KEYS");
        PRCTL_OPTIONS.put(PR_SET_PDEATHSIG, "PR_SET_PDEATHSIG");
        PRCTL_OPTIONS.put(PR_GET_PDEATHSIG, "PR_GET_PDEATHSIG");
        PRCTL_OPTIONS.put(PR_SET_PTRACER, "PR_SET_PTRACER");
        PRCTL_OPTIONS.put(PR_SET_SECCOMP, "PR_SET_SECCOMP");
        PRCTL_OPTIONS.put(PR_GET_SECCOMP, "PR_GET_SECCOMP");
        PRCTL_OPTIONS.put(PR_SET_SECUREBITS, "PR_SET_SECUREBITS");
        PRCTL_OPTIONS.put(PR_GET_SECUREBITS, "PR_GET_SECUREBITS");
        PRCTL_OPTIONS.put(PR_GET_SPECULATION_CTRL, "PR_GET_SPECULATION_CTRL");
        PRCTL_OPTIONS.put(PR_SET_SPECULATION_CTRL, "PR_SET_SPECULATION_CTRL");
        PRCTL_OPTIONS.put(PR_SVE_SET_VL, "PR_SVE_SET_VL");
        PRCTL_OPTIONS.put(PR_SVE_GET_VL, "PR_SVE_GET_VL");
        PRCTL_OPTIONS.put(PR_SET_SYSCALL_USER_DISPATCH, "PR_SET_SYSCALL_USER_DISPATCH");
        PRCTL_OPTIONS.put(PR_SET_TAGGED_ADDR_CTRL, "PR_SET_TAGGED_ADDR_CTRL");
        PRCTL_OPTIONS.put(PR_GET_TAGGED_ADDR_CTRL, "PR_GET_TAGGED_ADDR_CTRL");
        PRCTL_OPTIONS.put(PR_TASK_PERF_EVENTS_DISABLE, "PR_TASK_PERF_EVENTS_DISABLE");
        PRCTL_OPTIONS.put(PR_TASK_PERF_EVENTS_ENABLE, "PR_TASK_PERF_EVENTS_ENABLE");
        PRCTL_OPTIONS.put(PR_SET_THP_DISABLE, "PR_SET_THP_DISABLE");
        PRCTL_OPTIONS.put(PR_GET_THP_DISABLE, "PR_GET_THP_DISABLE");
        PRCTL_OPTIONS.put(PR_GET_TID_ADDRESS, "PR_GET_TID_ADDRESS");
        PRCTL_OPTIONS.put(PR_SET_TIMERSLACK, "PR_SET_TIMERSLACK");
        PRCTL_OPTIONS.put(PR_GET_TIMERSLACK, "PR_GET_TIMERSLACK");
        PRCTL_OPTIONS.put(PR_SET_TIMING, "PR_SET_TIMING");
        PRCTL_OPTIONS.put(PR_GET_TIMING, "PR_GET_TIMING");
        PRCTL_OPTIONS.put(PR_SET_TSC, "PR_SET_TSC");
        PRCTL_OPTIONS.put(PR_GET_TSC, "PR_GET_TSC");
        PRCTL_OPTIONS.put(PR_SET_UNALIGN, "PR_SET_UNALIGN");
        PRCTL_OPTIONS.put(PR_GET_UNALIGN, "PR_GET_UNALIGN");
    }

    public static String prctlOption(int option) {
        String name = PRCTL_OPTIONS.get(option);
        if (name != null) {
            return name;
        } else {
            return "0x" + HexFormatter.tohex(Integer.toUnsignedLong(option));
        }
    }
}
