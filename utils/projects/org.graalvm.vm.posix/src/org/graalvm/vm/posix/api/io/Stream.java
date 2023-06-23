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
package org.graalvm.vm.posix.api.io;

import org.graalvm.vm.posix.api.Errno;
import org.graalvm.vm.posix.api.MemoryFaultException;
import org.graalvm.vm.posix.api.PosixException;
import org.graalvm.vm.posix.api.PosixPointer;
import org.graalvm.vm.posix.api.io.tty.Kd;

public abstract class Stream {
    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;

    protected long statusFlags;

    private int refcnt = 0;

    public void addref() {
        refcnt++;
    }

    public void delref() throws PosixException {
        assert refcnt > 0;
        refcnt--;
        if (refcnt == 0) {
            close();
        }
    }

    public int read(PosixPointer buf, int length) throws PosixException {
        if (buf == null) {
            if (length == 0) {
                return 0;
            } else {
                throw new PosixException(Errno.EFAULT);
            }
        }

        if (buf.hasMemory(length)) {
            long off = buf.getOffset();
            if (off != (int) off) {
                throw new PosixException(Errno.EOVERFLOW);
            }
            return read(buf.getMemory(), (int) off, length);
        } else {
            byte[] b = new byte[length];
            int val = read(b, 0, length);
            PosixPointer p = buf;
            for (int i = 0; i < val; i++) {
                p.setI8(b[i]);
                p = p.add(1);
            }
            return val;
        }
    }

    public int write(PosixPointer buf, int length) throws PosixException {
        if (buf == null) {
            if (length == 0) {
                return 0;
            } else {
                throw new PosixException(Errno.EFAULT);
            }
        }

        if (buf.hasMemory(length)) {
            long off = buf.getOffset();
            if (off != (int) off) {
                throw new PosixException(Errno.EOVERFLOW);
            }
            return write(buf.getMemory(), (int) off, length);
        } else {
            byte[] b = new byte[length];
            PosixPointer p = buf;
            int i = 0;
            try {
                for (i = 0; i < length; i++) {
                    b[i] = p.getI8();
                    p = p.add(1);
                }
            } catch (MemoryFaultException e) {
                // if a fault happens, stop writing there
                // only throw exception if no data could be read
                if (i == 0) {
                    throw new PosixException(Errno.EFAULT);
                } else {
                    return write(b, 0, i);
                }
            }
            return write(b, 0, length);
        }
    }

    public int pread(PosixPointer buf, int length, long offset) throws PosixException {
        if (buf == null) {
            if (length == 0) {
                return 0;
            } else {
                throw new PosixException(Errno.EFAULT);
            }
        }

        if (buf.hasMemory(length)) {
            long off = buf.getOffset();
            if (off != (int) off) {
                throw new PosixException(Errno.EOVERFLOW);
            }
            return pread(buf.getMemory(), (int) off, length, offset);
        } else {
            byte[] b = new byte[length];
            int val = pread(b, 0, length, offset);
            PosixPointer p = buf;
            for (int i = 0; i < val; i++) {
                p.setI8(b[i]);
                p = p.add(1);
            }
            return val;
        }
    }

    public int pwrite(PosixPointer buf, int length, long offset) throws PosixException {
        if (buf == null) {
            if (length == 0) {
                return 0;
            } else {
                throw new PosixException(Errno.EFAULT);
            }
        }

        if (buf.hasMemory(length)) {
            long off = buf.getOffset();
            if (off != (int) off) {
                throw new PosixException(Errno.EOVERFLOW);
            }
            return pwrite(buf.getMemory(), (int) off, length, offset);
        } else {
            byte[] b = new byte[length];
            PosixPointer p = buf;
            for (int i = 0; i < length; i++) {
                b[i] = p.getI8();
                p = p.add(1);
            }
            return pwrite(b, 0, length, offset);
        }
    }

    public abstract int read(byte[] buf, int offset, int length) throws PosixException;

    public abstract int write(byte[] buf, int offset, int length) throws PosixException;

    public abstract int pread(byte[] buf, int offset, int length, long fileOffset) throws PosixException;

    public abstract int pwrite(byte[] buf, int offset, int length, long fileOffset) throws PosixException;

    public abstract int close() throws PosixException;

    public int readv(Iovec[] iov) throws PosixException {
        if (iov == null) {
            throw new PosixException(Errno.EFAULT);
        }

        int total = 0;
        for (Iovec v : iov) {
            if (v.iov_len == 0) {
                continue;
            }
            int bytes = read(v.iov_base, v.iov_len);
            total += bytes;
            if (bytes < v.iov_len) {
                return total;
            }
        }
        return total;
    }

    public int writev(Iovec[] iov) throws PosixException {
        if (iov == null) {
            throw new PosixException(Errno.EFAULT);
        }

        int total = 0;
        for (Iovec v : iov) {
            if (v.iov_len == 0) {
                continue;
            }
            int bytes = write(v.iov_base, v.iov_len);
            total += bytes;
            if (bytes < v.iov_len) {
                return total;
            }
        }
        return total;
    }

    public abstract long lseek(long offset, int whence) throws PosixException;

    public abstract void stat(Stat buf) throws PosixException;

    public abstract void statx(int mask, Statx buf) throws PosixException;

    public abstract void ftruncate(long size) throws PosixException;

    @SuppressWarnings("unused")
    public long getFlags() throws PosixException {
        return statusFlags;
    }

    @SuppressWarnings("unused")
    public void setFlags(int flags) throws PosixException {
        statusFlags = flags;
    }

    @SuppressWarnings("unused")
    public PosixPointer mmap(long size, int prot, int flags, long off) throws PosixException {
        throw new PosixException(Errno.ENOMEM);
    }

    public int ioctl(long request, @SuppressWarnings("unused") PosixPointer argp) throws PosixException {
        switch ((int) request) {
            case Ioctls.TCGETS:
            case Kd.KDGKBTYPE:
                throw new PosixException(Errno.ENOTTY);
        }
        throw new PosixException(Errno.EINVAL);
    }

    public long sendfile(Stream out, long offset, long count) throws PosixException {
        byte[] buf = new byte[8192];
        long bytes = 0;
        long oldoff = 0;
        if (offset != -1) {
            oldoff = lseek(0, SEEK_CUR);
        }
        while (bytes < count) {
            if (bytes + buf.length <= count) {
                int n = read(buf, 0, buf.length);
                if (n == 0) {
                    break;
                }
                assert n <= buf.length;
                out.write(buf, 0, n);
                bytes += n;
                if (n != buf.length) {
                    break;
                }
            } else {
                long cnt = count - bytes;
                assert cnt < buf.length;
                int n = read(buf, 0, (int) cnt);
                if (n == 0) {
                    break;
                }
                out.write(buf, 0, n);
                bytes += n;
                break;
            }
        }
        if (offset != -1) {
            lseek(oldoff, SEEK_SET);
        }
        return bytes;
    }
}
