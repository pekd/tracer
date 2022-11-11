package org.graalvm.vm.trcview.arch.ppc.decode;

public class Syscalls {
    public static final int SYS_exit = 1;
    public static final int SYS_read = 3;
    public static final int SYS_write = 4;
    public static final int SYS_open = 5;
    public static final int SYS_close = 6;
    public static final int SYS_waitpid = 7;
    public static final int SYS_creat = 8;
    public static final int SYS_unlink = 10;
    public static final int SYS_chdir = 12;
    public static final int SYS_time = 13;
    public static final int SYS_chmod = 15;
    public static final int SYS_lseek = 19;
    public static final int SYS_getpid = 20;
    public static final int SYS_mount = 21;
    public static final int SYS_umount = 22;
    public static final int SYS_setuid = 23;
    public static final int SYS_getuid = 24;
    public static final int SYS_alarm = 27;
    public static final int SYS_utime = 30;
    public static final int SYS_access = 33;
    public static final int SYS_mkdir = 39;
    public static final int SYS_dup = 41;
    public static final int SYS_times = 43;
    public static final int SYS_brk = 45;
    public static final int SYS_setgid = 46;
    public static final int SYS_getgid = 47;
    public static final int SYS_geteuid = 49;
    public static final int SYS_getegid = 50;
    public static final int SYS_umount2 = 52;
    public static final int SYS_ioctl = 54;
    public static final int SYS_umask = 60;
    public static final int SYS_dup2 = 63;
    public static final int SYS_getppid = 64;
    public static final int SYS_getrusage = 77;
    public static final int SYS_gettimeofday = 78;
    public static final int SYS_settimeofday = 79;
    public static final int SYS_symlink = 83;
    public static final int SYS_readlink = 85;
    public static final int SYS_mmap = 90;
    public static final int SYS_munmap = 91;
    public static final int SYS_ftruncate = 93;
    public static final int SYS_socketcall = 102;
    public static final int SYS_wait4 = 114;
    public static final int SYS_sysinfo = 116;
    public static final int SYS_fsync = 118;
    public static final int SYS_clone = 120;
    public static final int SYS_uname = 122;
    public static final int SYS_mprotect = 125;
    public static final int SYS__llseek = 140;
    public static final int SYS_getdents = 141;
    public static final int SYS_readv = 145;
    public static final int SYS_writev = 146;
    public static final int SYS_nanosleep = 162;
    public static final int SYS_poll = 167;
    public static final int SYS_rt_sigaction = 173;
    public static final int SYS_rt_sigprocmask = 174;
    public static final int SYS_rt_sigpending = 175;
    public static final int SYS_pread64 = 179;
    public static final int SYS_pwrite64 = 180;
    public static final int SYS_chown = 181;
    public static final int SYS_getcwd = 182;
    public static final int SYS_sigaltstack = 185;
    public static final int SYS_sendfile = 186;
    public static final int SYS_ugetrlimit = 190;
    public static final int SYS_mmap2 = 192;
    public static final int SYS_stat64 = 195; // 32-bit only
    public static final int SYS_lstat64 = 196; // 32-bit only
    public static final int SYS_fstat64 = 197; // 32-bit only
    public static final int SYS_getdents64 = 202;
    public static final int SYS_fcntl64 = 204; // 32-bit only
    public static final int SYS_gettid = 207;
    public static final int SYS_futex = 221;
    public static final int SYS_sendfile64 = 226; // 32-bit only
    public static final int SYS_set_tid_address = 232;
    public static final int SYS_exit_group = 234;
    public static final int SYS_timer_create = 240;
    public static final int SYS_timer_settime = 241;
    public static final int SYS_timer_delete = 244;
    public static final int SYS_clock_gettime = 246;
    public static final int SYS_clock_getres = 247;
    public static final int SYS_fadvise64_64 = 254; // 32-bit only
    public static final int SYS_tgkill = 250;
    public static final int SYS_openat = 286;
    public static final int SYS_fstatat64 = 291; // 32-bit only
    public static final int SYS_dup3 = 316;
    public static final int SYS_prlimit64 = 325;
    public static final int SYS_socket = 326;
    public static final int SYS_bind = 327;
    public static final int SYS_connect = 328;
    public static final int SYS_listen = 329;
    public static final int SYS_getsockname = 331;
    public static final int SYS_getpeername = 332;
    public static final int SYS_send = 334;
    public static final int SYS_sendto = 335;
    public static final int SYS_recv = 336;
    public static final int SYS_recvfrom = 337;
    public static final int SYS_shutdown = 338;
    public static final int SYS_setsockopt = 339;
    public static final int SYS_recvmsg = 342;
    public static final int SYS_sendmmsg = 349;
    public static final int SYS_statx = 383;
}
