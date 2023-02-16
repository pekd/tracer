/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.vm.trcview.arch.x86.decode;

public class SyscallNames {
    public static final String[] NAMES = {
                    /* 000 */ "read",
                    /* 001 */ "write",
                    /* 002 */ "open",
                    /* 003 */ "close",
                    /* 004 */ "stat",
                    /* 005 */ "fstat",
                    /* 006 */ "lstat",
                    /* 007 */ "poll",
                    /* 008 */ "lseek",
                    /* 009 */ "mmap",
                    /* 010 */ "mprotect",
                    /* 011 */ "munmap",
                    /* 012 */ "brk",
                    /* 013 */ "rt_sigaction",
                    /* 014 */ "rt_sigprocmask",
                    /* 015 */ "rt_sigreturn",
                    /* 016 */ "ioctl",
                    /* 017 */ "pread64",
                    /* 018 */ "pwrite64",
                    /* 019 */ "readv",
                    /* 020 */ "writev",
                    /* 021 */ "access",
                    /* 022 */ "pipe",
                    /* 023 */ "select",
                    /* 024 */ "sched_yield",
                    /* 025 */ "mremap",
                    /* 026 */ "msync",
                    /* 027 */ "mincore",
                    /* 028 */ "madvise",
                    /* 029 */ "shmget",
                    /* 030 */ "shmat",
                    /* 031 */ "shmctl",
                    /* 032 */ "dup",
                    /* 033 */ "dup2",
                    /* 034 */ "pause",
                    /* 035 */ "nanosleep",
                    /* 036 */ "getitimer",
                    /* 037 */ "alarm",
                    /* 038 */ "setitimer",
                    /* 039 */ "getpid",
                    /* 040 */ "sendfile",
                    /* 041 */ "socket",
                    /* 042 */ "connect",
                    /* 043 */ "accept",
                    /* 044 */ "sendto",
                    /* 045 */ "recvfrom",
                    /* 046 */ "sendmsg",
                    /* 047 */ "recvmsg",
                    /* 048 */ "shutdown",
                    /* 049 */ "bind",
                    /* 050 */ "listen",
                    /* 051 */ "getsockname",
                    /* 052 */ "getpeername",
                    /* 053 */ "socketpair",
                    /* 054 */ "setsockopt",
                    /* 055 */ "getsockopt",
                    /* 056 */ "clone",
                    /* 057 */ "fork",
                    /* 058 */ "vfork",
                    /* 059 */ "execve",
                    /* 060 */ "exit",
                    /* 061 */ "wait4",
                    /* 062 */ "kill",
                    /* 063 */ "uname",
                    /* 064 */ "semget",
                    /* 065 */ "semop",
                    /* 066 */ "semctl",
                    /* 067 */ "shmdt",
                    /* 068 */ "msgget",
                    /* 069 */ "msgsnd",
                    /* 070 */ "msgrcv",
                    /* 071 */ "msgctl",
                    /* 072 */ "fcntl",
                    /* 073 */ "flock",
                    /* 074 */ "fsync",
                    /* 075 */ "fdatasync",
                    /* 076 */ "truncate",
                    /* 077 */ "ftruncate",
                    /* 078 */ "getdents",
                    /* 079 */ "getcwd",
                    /* 080 */ "chdir",
                    /* 081 */ "fchdir",
                    /* 082 */ "rename",
                    /* 083 */ "mkdir",
                    /* 084 */ "rmdir",
                    /* 085 */ "creat",
                    /* 086 */ "link",
                    /* 087 */ "unlink",
                    /* 088 */ "symlink",
                    /* 089 */ "readlink",
                    /* 090 */ "chmod",
                    /* 091 */ "fchmod",
                    /* 092 */ "chown",
                    /* 093 */ "fchown",
                    /* 094 */ "lchown",
                    /* 095 */ "umask",
                    /* 096 */ "gettimeofday",
                    /* 097 */ "getrlimit",
                    /* 098 */ "getrusage",
                    /* 099 */ "sysinfo",
                    /* 100 */ "times",
                    /* 101 */ "ptrace",
                    /* 102 */ "getuid",
                    /* 103 */ "syslog",
                    /* 104 */ "getgid",
                    /* 105 */ "setuid",
                    /* 106 */ "setgid",
                    /* 107 */ "geteuid",
                    /* 108 */ "getegid",
                    /* 109 */ "setpgid",
                    /* 110 */ "getppid",
                    /* 111 */ "getpgrp",
                    /* 112 */ "setsid",
                    /* 113 */ "setreuid",
                    /* 114 */ "setregid",
                    /* 115 */ "getgroups",
                    /* 116 */ "setgroups",
                    /* 117 */ "setresuid",
                    /* 118 */ "getresuid",
                    /* 119 */ "setresgid",
                    /* 120 */ "getresgid",
                    /* 121 */ "getpgid",
                    /* 122 */ "setfsuid",
                    /* 123 */ "setfsgid",
                    /* 124 */ "getsid",
                    /* 125 */ "capget",
                    /* 126 */ "capset",
                    /* 127 */ "rt_sigpending",
                    /* 128 */ "rt_sigtimedwait",
                    /* 129 */ "rt_sigqueueinfo",
                    /* 130 */ "rt_sigsuspend",
                    /* 131 */ "sigaltstack",
                    /* 132 */ "utime",
                    /* 133 */ "mknod",
                    /* 134 */ "uselib",
                    /* 135 */ "personality",
                    /* 136 */ "ustat",
                    /* 137 */ "statfs",
                    /* 138 */ "fstatfs",
                    /* 139 */ "sysfs",
                    /* 140 */ "getpriority",
                    /* 141 */ "setpriority",
                    /* 142 */ "sched_setparam",
                    /* 143 */ "sched_getparam",
                    /* 144 */ "sched_setscheduler",
                    /* 145 */ "sched_getscheduler",
                    /* 146 */ "sched_get_priority_max",
                    /* 147 */ "sched_get_priority_min",
                    /* 148 */ "sched_rr_get_interval",
                    /* 149 */ "mlock",
                    /* 150 */ "munlock",
                    /* 151 */ "mlockall",
                    /* 152 */ "munlockall",
                    /* 153 */ "vhangup",
                    /* 154 */ "modify_ldt",
                    /* 155 */ "pivot_root",
                    /* 156 */ "_sysctl",
                    /* 157 */ "prctl",
                    /* 158 */ "arch_prctl",
                    /* 159 */ "adjtimex",
                    /* 160 */ "setrlimit",
                    /* 161 */ "chroot",
                    /* 162 */ "sync",
                    /* 163 */ "acct",
                    /* 164 */ "settimeofday",
                    /* 165 */ "mount",
                    /* 166 */ "umount2",
                    /* 167 */ "swapon",
                    /* 168 */ "swapoff",
                    /* 169 */ "reboot",
                    /* 170 */ "sethostname",
                    /* 171 */ "setdomainname",
                    /* 172 */ "iopl",
                    /* 173 */ "ioperm",
                    /* 174 */ "create_module",
                    /* 175 */ "init_module",
                    /* 176 */ "delete_module",
                    /* 177 */ "get_kernel_syms",
                    /* 178 */ "query_module",
                    /* 179 */ "quotactl",
                    /* 180 */ "nfsservctl",
                    /* 181 */ "getpmsg",
                    /* 182 */ "putpmsg",
                    /* 183 */ "afs_syscall",
                    /* 184 */ "tuxcall",
                    /* 185 */ "security",
                    /* 186 */ "gettid",
                    /* 187 */ "readahead",
                    /* 188 */ "setxattr",
                    /* 189 */ "lsetxattr",
                    /* 190 */ "fsetxattr",
                    /* 191 */ "getxattr",
                    /* 192 */ "lgetxattr",
                    /* 193 */ "fgetxattr",
                    /* 194 */ "listxattr",
                    /* 195 */ "llistxattr",
                    /* 196 */ "flistxattr",
                    /* 197 */ "removexattr",
                    /* 198 */ "lremovexattr",
                    /* 199 */ "fremovexattr",
                    /* 200 */ "tkill",
                    /* 201 */ "time",
                    /* 202 */ "futex",
                    /* 203 */ "sched_setaffinity",
                    /* 204 */ "sched_getaffinity",
                    /* 205 */ "set_thread_area",
                    /* 206 */ "io_setup",
                    /* 207 */ "io_destroy",
                    /* 208 */ "io_getevents",
                    /* 209 */ "io_submit",
                    /* 210 */ "io_cancel",
                    /* 211 */ "get_thread_area",
                    /* 212 */ "lookup_dcookie",
                    /* 213 */ "epoll_create",
                    /* 214 */ "epoll_ctl_old",
                    /* 215 */ "epoll_wait_old",
                    /* 216 */ "remap_file_pages",
                    /* 217 */ "getdents64",
                    /* 218 */ "set_tid_address",
                    /* 219 */ "restart_syscall",
                    /* 220 */ "semtimedop",
                    /* 221 */ "fadvise64",
                    /* 222 */ "timer_create",
                    /* 223 */ "timer_settime",
                    /* 224 */ "timer_gettime",
                    /* 225 */ "timer_getoverrun",
                    /* 226 */ "timer_delete",
                    /* 227 */ "clock_settime",
                    /* 228 */ "clock_gettime",
                    /* 229 */ "clock_getres",
                    /* 230 */ "clock_nanosleep",
                    /* 231 */ "exit_group",
                    /* 232 */ "epoll_wait",
                    /* 233 */ "epoll_ctl",
                    /* 234 */ "tgkill",
                    /* 235 */ "utimes",
                    /* 236 */ "vserver",
                    /* 237 */ "mbind",
                    /* 238 */ "set_mempolicy",
                    /* 239 */ "get_mempolicy",
                    /* 240 */ "mq_open",
                    /* 241 */ "mq_unlink",
                    /* 242 */ "mq_timedsend",
                    /* 243 */ "mq_timedreceive",
                    /* 244 */ "mq_notify",
                    /* 245 */ "mq_getsetattr",
                    /* 246 */ "kexec_load",
                    /* 247 */ "waitid",
                    /* 248 */ "add_key",
                    /* 249 */ "request_key",
                    /* 250 */ "keyctl",
                    /* 251 */ "ioprio_set",
                    /* 252 */ "ioprio_get",
                    /* 253 */ "inotify_init",
                    /* 254 */ "inotify_add_watch",
                    /* 255 */ "inotify_rm_watch",
                    /* 256 */ "migrate_pages",
                    /* 257 */ "openat",
                    /* 258 */ "mkdirat",
                    /* 259 */ "mknodat",
                    /* 260 */ "fchownat",
                    /* 261 */ "futimesat",
                    /* 262 */ "newfstatat",
                    /* 263 */ "unlinkat",
                    /* 264 */ "renameat",
                    /* 265 */ "linkat",
                    /* 266 */ "symlinkat",
                    /* 267 */ "readlinkat",
                    /* 268 */ "fchmodat",
                    /* 269 */ "faccessat",
                    /* 270 */ "pselect6",
                    /* 271 */ "ppoll",
                    /* 272 */ "unshare",
                    /* 273 */ "set_robust_list",
                    /* 274 */ "get_robust_list",
                    /* 275 */ "splice",
                    /* 276 */ "tee",
                    /* 277 */ "sync_file_range",
                    /* 278 */ "vmsplice",
                    /* 279 */ "move_pages",
                    /* 280 */ "utimensat",
                    /* 281 */ "epoll_pwait",
                    /* 282 */ "signalfd",
                    /* 283 */ "timerfd_create",
                    /* 284 */ "eventfd",
                    /* 285 */ "fallocate",
                    /* 286 */ "timerfd_settime",
                    /* 287 */ "timerfd_gettime",
                    /* 288 */ "accept4",
                    /* 289 */ "signalfd4",
                    /* 290 */ "eventfd2",
                    /* 291 */ "epoll_create1",
                    /* 292 */ "dup3",
                    /* 293 */ "pipe2",
                    /* 294 */ "inotify_init1",
                    /* 295 */ "preadv",
                    /* 296 */ "pwritev",
                    /* 297 */ "rt_tgsigqueueinfo",
                    /* 298 */ "perf_event_open",
                    /* 299 */ "recvmmsg",
                    /* 300 */ "fanotify_init",
                    /* 301 */ "fanotify_mark",
                    /* 302 */ "prlimit64",
                    /* 303 */ "name_to_handle_at",
                    /* 304 */ "open_by_handle_at",
                    /* 305 */ "clock_adjtime",
                    /* 306 */ "syncfs",
                    /* 307 */ "sendmmsg",
                    /* 308 */ "setns",
                    /* 309 */ "getcpu",
                    /* 310 */ "process_vm_readv",
                    /* 311 */ "process_vm_writev",
                    /* 312 */ "kcmp",
                    /* 313 */ "finit_module",
                    /* 314 */ "sched_setattr",
                    /* 315 */ "sched_getattr",
                    /* 316 */ "renameat2",
                    /* 317 */ "seccomp",
                    /* 318 */ "getrandom",
                    /* 319 */ "memfd_create",
                    /* 320 */ "kexec_file_load",
                    /* 321 */ "bpf",
                    /* 322 */ "execveat",
                    /* 323 */ "userfaultfd",
                    /* 324 */ "membarrier",
                    /* 325 */ "mlock2",
                    /* 326 */ "copy_file_range",
                    /* 327 */ "preadv2",
                    /* 328 */ "pwritev2",
                    /* 329 */ "pkey_mprotect",
                    /* 330 */ "pkey_alloc",
                    /* 331 */ "pkey_free",
                    /* 332 */ "statx"
    };

    public static String getName(long id) {
        if (id < 0 || id >= NAMES.length) {
            return null;
        } else {
            return NAMES[(int) id];
        }
    }
}
