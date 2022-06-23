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
package org.graalvm.vm.memory.svm.headers;

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.constant.CConstant;
import org.graalvm.nativeimage.impl.InternalPlatform;

/**
 * Definitions manually translated from the C header file sys/errno.h.
 */
@CContext(ErrnoDirectives.class)
public class Errno {
    /** Operation not permitted. */
    @CConstant
    public static native int EPERM();

    /** No such file or directory. */
    @CConstant
    public static native int ENOENT();

    /** No such process. */
    @CConstant
    public static native int ESRCH();

    /** Interrupted system call. */
    @CConstant
    public static native int EINTR();

    /** I/O error. */
    @CConstant
    public static native int EIO();

    /** No such device or address. */
    @CConstant
    public static native int ENXIO();

    /** Argument list too long. */
    @CConstant
    public static native int E2BIG();

    /** Exec format error. */
    @CConstant
    public static native int ENOEXEC();

    /** Bad file number. */
    @CConstant
    public static native int EBADF();

    /** No child processes. */
    @CConstant
    public static native int ECHILD();

    /** Try again. */
    @CConstant
    public static native int EAGAIN();

    /** Out of memory. */
    @CConstant
    public static native int ENOMEM();

    /** Permission denied. */
    @CConstant
    public static native int EACCES();

    /** Bad address. */
    @CConstant
    public static native int EFAULT();

    /** Block device required. */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENOTBLK();

    /** Device or resource busy. */
    @CConstant
    public static native int EBUSY();

    /** File exists. */
    @CConstant
    public static native int EEXIST();

    /** Cross-device link. */
    @CConstant
    public static native int EXDEV();

    /** No such device. */
    @CConstant
    public static native int ENODEV();

    /** Not a directory. */
    @CConstant
    public static native int ENOTDIR();

    /** Is a directory. */
    @CConstant
    public static native int EISDIR();

    /** Invalid argument. */
    @CConstant
    public static native int EINVAL();

    /** File table overflow. */
    @CConstant
    public static native int ENFILE();

    /** Too many open files. */
    @CConstant
    public static native int EMFILE();

    /** Not a typewriter. */
    @CConstant
    public static native int ENOTTY();

    /** Text file busy. */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ETXTBSY();

    /** File too large. */
    @CConstant
    public static native int EFBIG();

    /** No space left on device. */
    @CConstant
    public static native int ENOSPC();

    /** Illegal seek. */
    @CConstant
    public static native int ESPIPE();

    /** Read-only file system. */
    @CConstant
    public static native int EROFS();

    /** Too many links. */
    @CConstant
    public static native int EMLINK();

    /** Broken pipe. */
    @CConstant
    public static native int EPIPE();

    /** Math argument out of domain of func. */
    @CConstant
    public static native int EDOM();

    /** Math result not representable. */
    @CConstant
    public static native int ERANGE();

    /** Linux has no ENOTSUP error code. */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENOTSUP();

    /** Resource deadlock would occur */
    @CConstant
    public static native int EDEADLK();

    /** File name too long */
    @CConstant
    public static native int ENAMETOOLONG();

    /** No record locks available */
    @CConstant
    public static native int ENOLCK();

    /** Function not implemented */
    @CConstant
    public static native int ENOSYS();

    /** Directory not empty */
    @CConstant
    public static native int ENOTEMPTY();

    /** Too many symbolic links encountered */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ELOOP();

    /** Operation would block */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EWOULDBLOCK();

    /** No message of desired type */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENOMSG();

    /** Identifier removed */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EIDRM();

    /** Channel number out of range */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ECHRNG();

    /** Level 2 not synchronized */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EL2NSYNC();

    /** Level 3 halted */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EL3HLT();

    /** Level 3 reset */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EL3RST();

    /** Link number out of range */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ELNRNG();

    /** Protocol driver not attached */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EUNATCH();

    /** No CSI structure available */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENOCSI();

    /** Level 2 halted */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EL2HLT();

    /** Invalid exchange */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EBADE();

    /** Invalid request descriptor */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EBADR();

    /** Exchange full */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EXFULL();

    /** No anode */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENOANO();

    /** Invalid request code */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EBADRQC();

    /** Invalid slot */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EBADSLT();

    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EDEADLOCK();

    /** Bad font file format */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EBFONT();

    /** Device not a stream */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENOSTR();

    /** No data available */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENODATA();

    /** Timer expired */
    @CConstant
    public static native int ETIME();

    /** Out of streams resources */
    @CConstant
    public static native int ENOSR();

    /** Machine is not on the network */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENONET();

    /** Package not installed */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENOPKG();

    /** Object is remote */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EREMOTE();

    /** Link has been severed */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENOLINK();

    /** Advertise error */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EADV();

    /** Srmount error */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ESRMNT();

    /** Communication error on send */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ECOMM();

    /** Protocol error */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EPROTO();

    /** Multihop attempted */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EMULTIHOP();

    /** RFS specific error */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EDOTDOT();

    /** Not a data message */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EBADMSG();

    /** Value too large for defined data type */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EOVERFLOW();

    /** Name not unique on network */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENOTUNIQ();

    /** File descriptor in bad state */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EBADFD();

    /** Remote address changed */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EREMCHG();

    /** Can not access a needed shared library */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ELIBACC();

    /** Accessing a corrupted shared library */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ELIBBAD();

    /** .lib section in a.out corrupted */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ELIBSCN();

    /** Attempting to link in too many shared libraries */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ELIBMAX();

    /** Cannot exec a shared library directly */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ELIBEXEC();

    /** Illegal byte sequence */
    @CConstant
    public static native int EILSEQ();

    /** Interrupted system call should be restarted */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ERESTART();

    /** Streams pipe error */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ESTRPIPE();

    /** Too many users */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EUSERS();

    /** Socket operation on non-socket */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENOTSOCK();

    /** Destination address required */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EDESTADDRREQ();

    /** Message too long */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EMSGSIZE();

    /** Protocol wrong type for socket */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EPROTOTYPE();

    /** Protocol not available */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENOPROTOOPT();

    /** Protocol not supported */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EPROTONOSUPPORT();

    /** Socket type not supported */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ESOCKTNOSUPPORT();

    /** Operation not supported on transport endpoint */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EOPNOTSUPP();

    /** Protocol family not supported */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EPFNOSUPPORT();

    /** Address family not supported by protocol */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EAFNOSUPPORT();

    /** Address already in use */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EADDRINUSE();

    /** Cannot assign requested address */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EADDRNOTAVAIL();

    /** Network is down */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENETDOWN();

    /** Network is unreachable */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENETUNREACH();

    /** Network dropped connection because of reset */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENETRESET();

    /** Software caused connection abort */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ECONNABORTED();

    /** Connection reset by peer */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ECONNRESET();

    /** No buffer space available */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENOBUFS();

    /** Transport endpoint is already connected */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EISCONN();

    /** Transport endpoint is not connected */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENOTCONN();

    /** Cannot send after transport endpoint shutdown */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ESHUTDOWN();

    /** Too many references: cannot splice */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ETOOMANYREFS();

    /** Connection timed out */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ETIMEDOUT();

    /** Connection refused */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ECONNREFUSED();

    /** Host is down */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EHOSTDOWN();

    /** No route to host */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EHOSTUNREACH();

    /** Operation already in progress */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EALREADY();

    /** Operation now in progress */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EINPROGRESS();

    /** Stale NFS file handle */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ESTALE();

    /** Structure needs cleaning */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EUCLEAN();

    /** Not a XENIX named type file */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENOTNAM();

    /** No XENIX semaphores available */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENAVAIL();

    /** Is a named type file */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EISNAM();

    /** Remote I/O error */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EREMOTEIO();

    /** Quota exceeded */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EDQUOT();

    /** No medium found */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENOMEDIUM();

    /** Wrong medium type */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EMEDIUMTYPE();

    /** Operation Canceled */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ECANCELED();

    /** Required key not available */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ENOKEY();

    /** Key has expired */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EKEYEXPIRED();

    /** Key has been revoked */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EKEYREVOKED();

    /** Key was rejected by service */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int EKEYREJECTED();

    /* for robust mutexes */
    /** Owner died */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int EOWNERDEAD();

    /** State not recoverable */
    @CConstant
    @Platforms({Platform.LINUX.class, Platform.DARWIN.class, InternalPlatform.LINUX_JNI.class, InternalPlatform.DARWIN_JNI.class})
    public static native int ENOTRECOVERABLE();

    /** Operation not possible due to RF-kill */
    @CConstant
    @Platforms(Platform.LINUX.class)
    public static native int ERFKILL();

    /** Memory page has hardware error */
    @CConstant
    public static native int EHWPOISON();
}
