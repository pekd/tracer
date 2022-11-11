package org.graalvm.vm.trcview.arch.ppc.decode;

import static org.graalvm.vm.trcview.decode.DecoderUtils.cstr;
import static org.graalvm.vm.trcview.decode.DecoderUtils.mem;
import static org.graalvm.vm.trcview.decode.DecoderUtils.ptr;
import static org.graalvm.vm.util.HexFormatter.tohex;

import org.graalvm.vm.posix.api.Clock;
import org.graalvm.vm.posix.api.Errno;
import org.graalvm.vm.posix.api.Signal;
import org.graalvm.vm.posix.api.Time;
import org.graalvm.vm.posix.api.Unistd;
import org.graalvm.vm.posix.api.io.Fcntl;
import org.graalvm.vm.posix.api.io.Ioctls;
import org.graalvm.vm.posix.api.io.Stat;
import org.graalvm.vm.posix.api.linux.Futex;
import org.graalvm.vm.posix.api.linux.Sched;
import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.posix.api.net.Socket;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.ppc.disasm.Cr;
import org.graalvm.vm.trcview.arch.ppc.io.PowerPCCpuState;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class PowerPCSyscallDecoder extends SyscallDecoder {
    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;

    private static String whence(long whence) {
        switch ((int) whence) {
            case SEEK_SET:
                return "SEEK_SET";
            case SEEK_CUR:
                return "SEEK_CUR";
            case SEEK_END:
                return "SEEK_END";
            default:
                return Long.toString(whence);
        }
    }

    private static String hex(long x) {
        return tohex(x, 1);
    }

    public static String decode(PowerPCCpuState state, PowerPCCpuState next, TraceAnalyzer trc) {
        if (next == null) {
            return decode(state, trc);
        } else {
            String call = decode(state, trc);
            String result = decodeResult(state.getGPR(0), next);
            return call + " = " + result;
        }
    }

    private static boolean test(int x, int i) {
        return (x & (1 << (3 - i))) != 0;
    }

    public static String decodeResult(int sc, PowerPCCpuState state) {
        Cr field = new Cr(0);
        int cr = field.get(state.getCR());
        if (test(cr, Cr.SO)) {
            return Errno.getConstant(state.getGPR(3));
        } else {
            switch (sc) {
                case Syscalls.SYS_mmap:
                case Syscalls.SYS_mmap2:
                case Syscalls.SYS_brk:
                    return ptr(Integer.toUnsignedLong(state.getGPR(3)));
                default:
                    return Integer.toString(state.getGPR(3));
            }
        }
    }

    public static String decode(PowerPCCpuState state, TraceAnalyzer trc) {
        int id = state.getGPR(0);
        long a1 = Integer.toUnsignedLong(state.getGPR(3));
        long a2 = Integer.toUnsignedLong(state.getGPR(4));
        long a3 = Integer.toUnsignedLong(state.getGPR(5));
        long a4 = Integer.toUnsignedLong(state.getGPR(6));
        long a5 = Integer.toUnsignedLong(state.getGPR(7));
        long a6 = Integer.toUnsignedLong(state.getGPR(8));
        switch (id) {
            case Syscalls.SYS_read:
                return "read(" + a1 + ", " + ptr(a2) + ", " + a3 + ")";
            case Syscalls.SYS_write:
                return "write(" + a1 + ", " + mem(a2, a3, state.getStep(), trc) + ", " + a3 + ")";
            case Syscalls.SYS_open:
                return "open(" + cstr(a1, state.getStep(), trc) + ", " + Fcntl.flags((int) a2) + ", " + Stat.mode((int) a3) + ")";
            case Syscalls.SYS_close:
                return "close(" + a1 + ")";
            case Syscalls.SYS_stat64:
                return "stat64(" + cstr(a1, state.getStep(), trc) + ", " + ptr(a2) + ")";
            case Syscalls.SYS_fstat64:
                return "fstat64(" + a1 + ", " + ptr(a2) + ")";
            case Syscalls.SYS_lstat64:
                return "lstat64(" + cstr(a1, state.getStep(), trc) + ", " + ptr(a2) + ")";
            case Syscalls.SYS_poll:
                return "poll(" + ptr(a1) + ", " + a2 + ", " + a3 + ")";
            case Syscalls.SYS_lseek:
                return "lseek(" + a1 + ", " + a2 + ", " + whence(a3) + ")";
            case Syscalls.SYS__llseek:
                return "_llseek(" + a1 + ", HI(" + (a2 << 32 | a3) + "), LO(" + (a2 << 32 | a3) + "), " + ptr(a4) + ", " + whence(a5) + ")";
            case Syscalls.SYS_mmap:
                return "mmap(" + ptr(a1) + ", " + a2 + ", " + Mman.prot((int) a3) + ", " + Mman.flags((int) a4) + ", " + (int) a5 + ", " + a6 + ")";
            case Syscalls.SYS_mmap2:
                return "mmap2(" + ptr(a1) + ", " + a2 + ", " + Mman.prot((int) a3) + ", " + Mman.flags((int) a4) + ", " + (int) a5 + ", 0x" + hex(a6 << 12) + " >> 12)";
            case Syscalls.SYS_mprotect:
                return "mprotect(" + ptr(a1) + ", " + a2 + ", " + Mman.prot((int) a3) + ")";
            case Syscalls.SYS_munmap:
                return "munmap(" + ptr(a1) + ", " + a2 + ")";
            case Syscalls.SYS_brk:
                return "brk(" + ptr(a1) + ")";
            case Syscalls.SYS_rt_sigaction:
                return "rt_sigaction(" + Signal.toString((int) a1) + ", " + ptr(a2) + ", " + ptr(a3) + ")";
            case Syscalls.SYS_rt_sigprocmask:
                return "rt_sigprocmask(" + Signal.sigprocmaskHow((int) a1) + ", " + ptr(a2) + ", " + ptr(a3) + ", " + a4 + ")";
            case Syscalls.SYS_ioctl:
                return "ioctl(" + a1 + ", " + Ioctls.toString((int) a2) + " /* 0x" + tohex(a2, 8) + " */, 0x" + hex(a3) + ")";
            case Syscalls.SYS_pread64:
                return "pread64(" + a1 + ", " + ptr(a2) + ", " + a3 + ", " + a4 + ")";
            case Syscalls.SYS_pwrite64:
                return "pwrite64(" + a1 + ", " + mem(a2, a3, state.getStep(), trc) + ", " + a3 + ", " + a4 + ")";
            case Syscalls.SYS_readv:
                return "readv(" + a1 + ", " + ptr(a2) + ", " + a3 + ")";
            case Syscalls.SYS_writev:
                return "writev(" + a1 + ", " + ptr(a2) + ", " + a3 + ")";
            case Syscalls.SYS_access:
                return "access(" + cstr(a1, state.getStep(), trc) + ", " + Unistd.amode((int) a2) + ")";
            case Syscalls.SYS_dup:
                return "dup(" + a1 + ")";
            case Syscalls.SYS_dup2:
                return "dup2(" + a1 + ", " + a2 + ")";
            case Syscalls.SYS_nanosleep:
                return "nanosleep(" + ptr(a1) + ", " + ptr(a2) + ")";
            case Syscalls.SYS_getpid:
                return "getpid()";
            case Syscalls.SYS_socket:
                return "socket(" + Socket.addressFamily((int) a1) + ", " + Socket.type((int) a2) + ", " + Socket.protocol((int) a1, (int) a3) + ")";
            case Syscalls.SYS_connect:
                return "connect(" + a1 + ", " + ptr(a2) + ", " + a3 + ")";
            case Syscalls.SYS_sendto:
                return "sendto(" + a1 + ", " + mem(a2, a3, state.getStep(), trc) + ", " + a3 + ", " + Socket.sendrecvFlags((int) a4) + ", " + ptr(a5) + ", " + a6 + ")";
            case Syscalls.SYS_recvfrom:
                return "recvfrom(" + a1 + ", " + ptr(a2) + ", " + a3 + ", " + Socket.sendrecvFlags((int) a4) + ", " + ptr(a5) + ", " + ptr(a6) + ")";
            case Syscalls.SYS_recvmsg:
                return "recvmsg(" + a1 + ", " + ptr(a2) + ", " + Socket.sendrecvFlags((int) a3) + ")";
            case Syscalls.SYS_shutdown:
                return "shutdown(" + a1 + ", " + Socket.shutdownHow((int) a2) + ")";
            case Syscalls.SYS_bind:
                return "bind(" + a1 + ", " + ptr(a2) + ", " + a3 + ")";
            case Syscalls.SYS_listen:
                return "listen(" + a1 + ", " + a2 + ")";
            case Syscalls.SYS_getsockname:
                return "getsockname(" + a1 + ", " + ptr(a2) + ", " + ptr(a3) + ")";
            case Syscalls.SYS_getpeername:
                return "getpeername(" + a1 + ", " + ptr(a2) + ", " + ptr(a3) + ")";
            case Syscalls.SYS_setsockopt:
                return "setsockopt(" + a1 + ", " + Socket.sockoptLevel((int) a2) + ", " + Socket.sockoptOption((int) a2, (int) a3) + ", " + ptr(a4) + ", " + a5 + ")";
            case Syscalls.SYS_clone:
                return "clone(" + Sched.clone((int) a1) + ", " + ptr(a2) + ", " + ptr(a3) + ", " + ptr(a4) + ", " + ptr(a5) + ")";
            case Syscalls.SYS_exit:
                return "exit(" + a1 + ")";
            case Syscalls.SYS_uname:
                return "uname(" + ptr(a1) + ")";
            case Syscalls.SYS_fcntl64:
                return "fcntl64(" + a1 + ", " + Fcntl.fcntl((int) a2) + ", " + ptr(a3) + ")";
            case Syscalls.SYS_fsync:
                return "fsync(" + a1 + ")";
            case Syscalls.SYS_getdents:
                return "getdents(" + a1 + ", " + ptr(a2) + ", " + a3 + ")";
            case Syscalls.SYS_getcwd:
                return "getcwd(" + ptr(a1) + ", " + a2 + ")";
            case Syscalls.SYS_creat:
                return "creat(" + cstr(a1, state.getStep(), trc) + ", " + Stat.mode((int) a2) + ")";
            case Syscalls.SYS_unlink:
                return "unlink(" + cstr(a1, state.getStep(), trc) + ")";
            case Syscalls.SYS_readlink:
                return "readlink(" + cstr(a1, state.getStep(), trc) + ", " + ptr(a2) + ", " + a3 + ")";
            case Syscalls.SYS_gettimeofday:
                return "gettimeofday(" + ptr(a1) + ", " + ptr(a2) + ")";
            case Syscalls.SYS_sysinfo:
                return "sysinfo(" + ptr(a1) + ")";
            case Syscalls.SYS_times:
                return "times(" + ptr(a1) + ")";
            case Syscalls.SYS_getuid:
                return "getuid()";
            case Syscalls.SYS_getgid:
                return "getgid()";
            case Syscalls.SYS_setuid:
                return "setuid(" + a1 + ")";
            case Syscalls.SYS_setgid:
                return "setgid(" + a1 + ")";
            case Syscalls.SYS_geteuid:
                return "geteuid()";
            case Syscalls.SYS_getegid:
                return "getegid()";
            case Syscalls.SYS_sigaltstack:
                return "sigaltstack(" + ptr(a1) + ", " + ptr(a2) + ")";
            case Syscalls.SYS_gettid:
                return "gettid()";
            case Syscalls.SYS_time:
                return "time(" + ptr(a1) + ")";
            case Syscalls.SYS_futex:
                return "futex(" + ptr(a1) + ", " + Futex.op((int) a2) + ", " + a3 + ", 0x" + hex(a4) + ", " + ptr(a5) + ", " + a6 + ")";
            case Syscalls.SYS_getdents64:
                return "getdents64(" + a1 + ", " + ptr(a2) + ", " + a3 + ")";
            case Syscalls.SYS_set_tid_address:
                return "set_tid_address(" + ptr(a1) + ")";
            case Syscalls.SYS_timer_create:
                return "timer_create(" + Clock.getClockName((int) a1) + ", " + ptr(a2) + ", " + ptr(a3) + ")";
            case Syscalls.SYS_timer_settime:
                return "timer_settime(" + a1 + ", " + Time.timerFlags((int) a2) + ", " + ptr(a3) + ", " + ptr(a4) + ")";
            case Syscalls.SYS_timer_delete:
                return "timer_delete(" + a1 + ")";
            case Syscalls.SYS_clock_gettime:
                return "clock_gettime(" + Clock.getClockName((int) a1) + ", " + ptr(a2) + ")";
            case Syscalls.SYS_clock_getres:
                return "clock_getres(" + Clock.getClockName((int) a1) + ", " + ptr(a2) + ")";
            case Syscalls.SYS_exit_group:
                return "exit_group(" + a1 + ")";
            case Syscalls.SYS_tgkill:
                return "tgkill(" + a1 + ", " + a2 + ", " + Signal.toString((int) a3) + ")";
            case Syscalls.SYS_openat:
                return "openat(" + (int) a1 + ", " + cstr(a2, state.getStep(), trc) + ", " + Fcntl.flags((int) a3) + ", " + Stat.mode((int) a4) + ")";
            case Syscalls.SYS_fstatat64:
                return "fstatat64(" + (int) a1 + ", " + cstr(a2, state.getStep(), trc) + ", " + ptr(a3) + ", " + Fcntl.statx((int) a4) + ")";
            case Syscalls.SYS_dup3:
                return "dup3(" + a1 + ", " + a2 + ", " + a3 + ")";
            case Syscalls.SYS_prlimit64:
                return "prlimit64(" + a1 + ", " + a2 + ", " + ptr(a3) + ", " + ptr(a4) + ")";
            case Syscalls.SYS_statx:
                return "statx(" + (int) a1 + ", " + cstr(a2, state.getStep(), trc) + ", " + Fcntl.statx((int) a3) + ", " + Stat.mask((int) a4) + ", " + ptr(a5) + ")";
            default:
                return SyscallNames.getName(id);
        }
    }

    @Override
    public String decode(CpuState state, CpuState next, TraceAnalyzer trc) {
        if (!(state instanceof PowerPCCpuState) || (next != null && !(next instanceof PowerPCCpuState))) {
            return null;
        }
        return decode((PowerPCCpuState) state, (PowerPCCpuState) next, trc);
    }

    @Override
    public String decodeResult(int sc, CpuState state, TraceAnalyzer trc) {
        if (!(state instanceof PowerPCCpuState)) {
            return null;
        }
        return decodeResult(sc, (PowerPCCpuState) state);
    }

    @Override
    public String decode(CpuState state, TraceAnalyzer trc) {
        if (!(state instanceof PowerPCCpuState)) {
            return null;
        }
        return decode((PowerPCCpuState) state, trc);
    }
}
