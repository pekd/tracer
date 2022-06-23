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
package org.graalvm.vm.memory.svm;

import static org.graalvm.vm.memory.svm.headers.Errno.E2BIG;
import static org.graalvm.vm.memory.svm.headers.Errno.EACCES;
import static org.graalvm.vm.memory.svm.headers.Errno.EADDRINUSE;
import static org.graalvm.vm.memory.svm.headers.Errno.EADDRNOTAVAIL;
import static org.graalvm.vm.memory.svm.headers.Errno.EAFNOSUPPORT;
import static org.graalvm.vm.memory.svm.headers.Errno.EAGAIN;
import static org.graalvm.vm.memory.svm.headers.Errno.EALREADY;
import static org.graalvm.vm.memory.svm.headers.Errno.EBADF;
import static org.graalvm.vm.memory.svm.headers.Errno.EBADMSG;
import static org.graalvm.vm.memory.svm.headers.Errno.EBUSY;
import static org.graalvm.vm.memory.svm.headers.Errno.ECANCELED;
import static org.graalvm.vm.memory.svm.headers.Errno.ECHILD;
import static org.graalvm.vm.memory.svm.headers.Errno.ECONNABORTED;
import static org.graalvm.vm.memory.svm.headers.Errno.ECONNREFUSED;
import static org.graalvm.vm.memory.svm.headers.Errno.ECONNRESET;
import static org.graalvm.vm.memory.svm.headers.Errno.EDEADLK;
import static org.graalvm.vm.memory.svm.headers.Errno.EDESTADDRREQ;
import static org.graalvm.vm.memory.svm.headers.Errno.EDOM;
import static org.graalvm.vm.memory.svm.headers.Errno.EDQUOT;
import static org.graalvm.vm.memory.svm.headers.Errno.EEXIST;
import static org.graalvm.vm.memory.svm.headers.Errno.EFAULT;
import static org.graalvm.vm.memory.svm.headers.Errno.EFBIG;
import static org.graalvm.vm.memory.svm.headers.Errno.EHOSTDOWN;
import static org.graalvm.vm.memory.svm.headers.Errno.EHOSTUNREACH;
import static org.graalvm.vm.memory.svm.headers.Errno.EHWPOISON;
import static org.graalvm.vm.memory.svm.headers.Errno.EIDRM;
import static org.graalvm.vm.memory.svm.headers.Errno.EILSEQ;
import static org.graalvm.vm.memory.svm.headers.Errno.EINPROGRESS;
import static org.graalvm.vm.memory.svm.headers.Errno.EINTR;
import static org.graalvm.vm.memory.svm.headers.Errno.EINVAL;
import static org.graalvm.vm.memory.svm.headers.Errno.EIO;
import static org.graalvm.vm.memory.svm.headers.Errno.EISCONN;
import static org.graalvm.vm.memory.svm.headers.Errno.EISDIR;
import static org.graalvm.vm.memory.svm.headers.Errno.ELOOP;
import static org.graalvm.vm.memory.svm.headers.Errno.EMFILE;
import static org.graalvm.vm.memory.svm.headers.Errno.EMLINK;
import static org.graalvm.vm.memory.svm.headers.Errno.EMSGSIZE;
import static org.graalvm.vm.memory.svm.headers.Errno.EMULTIHOP;
import static org.graalvm.vm.memory.svm.headers.Errno.ENAMETOOLONG;
import static org.graalvm.vm.memory.svm.headers.Errno.ENETDOWN;
import static org.graalvm.vm.memory.svm.headers.Errno.ENETRESET;
import static org.graalvm.vm.memory.svm.headers.Errno.ENETUNREACH;
import static org.graalvm.vm.memory.svm.headers.Errno.ENFILE;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOBUFS;
import static org.graalvm.vm.memory.svm.headers.Errno.ENODEV;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOENT;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOEXEC;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOLCK;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOMEM;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOMSG;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOPROTOOPT;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOSPC;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOSR;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOSYS;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOTBLK;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOTCONN;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOTDIR;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOTEMPTY;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOTRECOVERABLE;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOTSOCK;
import static org.graalvm.vm.memory.svm.headers.Errno.ENOTTY;
import static org.graalvm.vm.memory.svm.headers.Errno.ENXIO;
import static org.graalvm.vm.memory.svm.headers.Errno.EOPNOTSUPP;
import static org.graalvm.vm.memory.svm.headers.Errno.EOVERFLOW;
import static org.graalvm.vm.memory.svm.headers.Errno.EOWNERDEAD;
import static org.graalvm.vm.memory.svm.headers.Errno.EPERM;
import static org.graalvm.vm.memory.svm.headers.Errno.EPFNOSUPPORT;
import static org.graalvm.vm.memory.svm.headers.Errno.EPIPE;
import static org.graalvm.vm.memory.svm.headers.Errno.EPROTO;
import static org.graalvm.vm.memory.svm.headers.Errno.EPROTONOSUPPORT;
import static org.graalvm.vm.memory.svm.headers.Errno.EPROTOTYPE;
import static org.graalvm.vm.memory.svm.headers.Errno.ERANGE;
import static org.graalvm.vm.memory.svm.headers.Errno.EREMOTE;
import static org.graalvm.vm.memory.svm.headers.Errno.EROFS;
import static org.graalvm.vm.memory.svm.headers.Errno.ESHUTDOWN;
import static org.graalvm.vm.memory.svm.headers.Errno.ESOCKTNOSUPPORT;
import static org.graalvm.vm.memory.svm.headers.Errno.ESPIPE;
import static org.graalvm.vm.memory.svm.headers.Errno.ESRCH;
import static org.graalvm.vm.memory.svm.headers.Errno.ESTALE;
import static org.graalvm.vm.memory.svm.headers.Errno.ETIME;
import static org.graalvm.vm.memory.svm.headers.Errno.ETIMEDOUT;
import static org.graalvm.vm.memory.svm.headers.Errno.ETOOMANYREFS;
import static org.graalvm.vm.memory.svm.headers.Errno.ETXTBSY;
import static org.graalvm.vm.memory.svm.headers.Errno.EUSERS;
import static org.graalvm.vm.memory.svm.headers.Errno.EWOULDBLOCK;
import static org.graalvm.vm.memory.svm.headers.Errno.EXDEV;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.graalvm.vm.posix.api.Errno;
import org.graalvm.vm.util.log.Trace;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class ErrnoTranslator {
    private static final Logger log = Trace.create(ErrnoTranslator.class);

    private static final Map<Integer, Integer> ERRORS = new HashMap<>();
    private static boolean initialized = false;

    private static void init() {
        ERRORS.put(EPERM(), Errno.EPERM);
        ERRORS.put(ENOENT(), Errno.ENOENT);
        ERRORS.put(ESRCH(), Errno.ESRCH);
        ERRORS.put(EINTR(), Errno.EINTR);
        ERRORS.put(EIO(), Errno.EIO);
        ERRORS.put(ENXIO(), Errno.ENXIO);
        ERRORS.put(E2BIG(), Errno.E2BIG);
        ERRORS.put(ENOEXEC(), Errno.ENOEXEC);
        ERRORS.put(EBADF(), Errno.EBADF);
        ERRORS.put(ECHILD(), Errno.ECHILD);
        ERRORS.put(EAGAIN(), Errno.EAGAIN);
        ERRORS.put(ENOMEM(), Errno.ENOMEM);
        ERRORS.put(EACCES(), Errno.EACCES);
        ERRORS.put(EFAULT(), Errno.EFAULT);
        ERRORS.put(ENOTBLK(), Errno.ENOTBLK);
        ERRORS.put(EBUSY(), Errno.EBUSY);
        ERRORS.put(EEXIST(), Errno.EEXIST);
        ERRORS.put(EXDEV(), Errno.EXDEV);
        ERRORS.put(ENODEV(), Errno.ENODEV);
        ERRORS.put(ENOTDIR(), Errno.ENOTDIR);
        ERRORS.put(EISDIR(), Errno.EISDIR);
        ERRORS.put(EINVAL(), Errno.EINVAL);
        ERRORS.put(ENFILE(), Errno.ENFILE);
        ERRORS.put(EMFILE(), Errno.EMFILE);
        ERRORS.put(ENOTTY(), Errno.ENOTTY);
        ERRORS.put(ETXTBSY(), Errno.ETXTBSY);
        ERRORS.put(EFBIG(), Errno.EFBIG);
        ERRORS.put(ENOSPC(), Errno.ENOSPC);
        ERRORS.put(ESPIPE(), Errno.ESPIPE);
        ERRORS.put(EROFS(), Errno.EROFS);
        ERRORS.put(EMLINK(), Errno.EMLINK);
        ERRORS.put(EPIPE(), Errno.EPIPE);
        ERRORS.put(EDOM(), Errno.EDOM);
        ERRORS.put(ERANGE(), Errno.ERANGE);
        ERRORS.put(EDEADLK(), Errno.EDEADLK);
        ERRORS.put(ENAMETOOLONG(), Errno.ENAMETOOLONG);
        ERRORS.put(ENOLCK(), Errno.ENOLCK);
        ERRORS.put(ENOSYS(), Errno.ENOSYS);
        ERRORS.put(ENOTEMPTY(), Errno.ENOTEMPTY);
        ERRORS.put(ELOOP(), Errno.ELOOP);
        ERRORS.put(EWOULDBLOCK(), Errno.EWOULDBLOCK);
        ERRORS.put(ENOMSG(), Errno.ENOMSG);
        ERRORS.put(EIDRM(), Errno.EIDRM);
        ERRORS.put(ETIME(), Errno.ETIME);
        ERRORS.put(ENOSR(), Errno.ENOSR);
        ERRORS.put(EREMOTE(), Errno.EREMOTE);
        ERRORS.put(EPROTO(), Errno.EPROTO);
        ERRORS.put(EMULTIHOP(), Errno.EMULTIHOP);
        ERRORS.put(EBADMSG(), Errno.EBADMSG);
        ERRORS.put(EOVERFLOW(), Errno.EOVERFLOW);
        ERRORS.put(EILSEQ(), Errno.EILSEQ);
        ERRORS.put(EUSERS(), Errno.EUSERS);
        ERRORS.put(ENOTSOCK(), Errno.ENOTSOCK);
        ERRORS.put(EDESTADDRREQ(), Errno.EDESTADDRREQ);
        ERRORS.put(EMSGSIZE(), Errno.EMSGSIZE);
        ERRORS.put(EPROTOTYPE(), Errno.EPROTOTYPE);
        ERRORS.put(ENOPROTOOPT(), Errno.ENOPROTOOPT);
        ERRORS.put(EPROTONOSUPPORT(), Errno.EPROTONOSUPPORT);
        ERRORS.put(ESOCKTNOSUPPORT(), Errno.ESOCKTNOSUPPORT);
        ERRORS.put(EOPNOTSUPP(), Errno.EOPNOTSUPP);
        ERRORS.put(EPFNOSUPPORT(), Errno.EPFNOSUPPORT);
        ERRORS.put(EAFNOSUPPORT(), Errno.EAFNOSUPPORT);
        ERRORS.put(EADDRINUSE(), Errno.EADDRINUSE);
        ERRORS.put(EADDRNOTAVAIL(), Errno.EADDRNOTAVAIL);
        ERRORS.put(ENETDOWN(), Errno.ENETDOWN);
        ERRORS.put(ENETUNREACH(), Errno.ENETUNREACH);
        ERRORS.put(ENETRESET(), Errno.ENETRESET);
        ERRORS.put(ECONNABORTED(), Errno.ECONNABORTED);
        ERRORS.put(ECONNRESET(), Errno.ECONNRESET);
        ERRORS.put(ENOBUFS(), Errno.ENOBUFS);
        ERRORS.put(EISCONN(), Errno.EISCONN);
        ERRORS.put(ENOTCONN(), Errno.ENOTCONN);
        ERRORS.put(ESHUTDOWN(), Errno.ESHUTDOWN);
        ERRORS.put(ETOOMANYREFS(), Errno.ETOOMANYREFS);
        ERRORS.put(ETIMEDOUT(), Errno.ETIMEDOUT);
        ERRORS.put(ECONNREFUSED(), Errno.ECONNREFUSED);
        ERRORS.put(EHOSTDOWN(), Errno.EHOSTDOWN);
        ERRORS.put(EHOSTUNREACH(), Errno.EHOSTUNREACH);
        ERRORS.put(EALREADY(), Errno.EALREADY);
        ERRORS.put(EINPROGRESS(), Errno.EINPROGRESS);
        ERRORS.put(ESTALE(), Errno.ESTALE);
        ERRORS.put(EDQUOT(), Errno.EDQUOT);
        ERRORS.put(ECANCELED(), Errno.ECANCELED);
        ERRORS.put(EOWNERDEAD(), Errno.EOWNERDEAD);
        ERRORS.put(ENOTRECOVERABLE(), Errno.ENOTRECOVERABLE);
        ERRORS.put(EHWPOISON(), Errno.EHWPOISON);
    }

    @TruffleBoundary
    public static int translate(int errno) {
        if (!initialized) {
            init();
            initialized = true;
        }
        Integer err = ERRORS.get(errno);
        if (err != null) {
            return err;
        } else {
            log.warning(() -> "Unknown errno code: " + errno);
            return errno;
        }
    }
}
