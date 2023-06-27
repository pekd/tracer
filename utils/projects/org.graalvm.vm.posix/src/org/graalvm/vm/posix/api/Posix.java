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
package org.graalvm.vm.posix.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.graalvm.vm.posix.api.io.DirectoryStream;
import org.graalvm.vm.posix.api.io.Fcntl;
import org.graalvm.vm.posix.api.io.FileDescriptor;
import org.graalvm.vm.posix.api.io.FileDescriptorManager;
import org.graalvm.vm.posix.api.io.Ioctls;
import org.graalvm.vm.posix.api.io.Iovec;
import org.graalvm.vm.posix.api.io.PipeStream;
import org.graalvm.vm.posix.api.io.Poll;
import org.graalvm.vm.posix.api.io.Pollfd;
import org.graalvm.vm.posix.api.io.Stat;
import org.graalvm.vm.posix.api.io.Statx;
import org.graalvm.vm.posix.api.io.Stream;
import org.graalvm.vm.posix.api.io.tty.TTYStream;
import org.graalvm.vm.posix.api.linux.Futex;
import org.graalvm.vm.posix.api.linux.Linux;
import org.graalvm.vm.posix.api.linux.Sysinfo;
import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.posix.api.net.Mmsghdr;
import org.graalvm.vm.posix.api.net.Msghdr;
import org.graalvm.vm.posix.api.net.NetworkStream;
import org.graalvm.vm.posix.api.net.RecvResult;
import org.graalvm.vm.posix.api.net.Sockaddr;
import org.graalvm.vm.posix.api.net.Socket;
import org.graalvm.vm.posix.vfs.VFS;
import org.graalvm.vm.posix.vfs.VFSDirectory;
import org.graalvm.vm.posix.vfs.VFSEntry;
import org.graalvm.vm.posix.vfs.VFSFile;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class Posix {
    private static final Logger log = Trace.create(Posix.class);

    private boolean strace;

    private final FileDescriptorManager fds;
    private final Uname uname;
    private final VFS vfs;
    private final Clock clock;
    private final Time time;
    private final Times times;
    private final Linux linux;
    private final Info processInfo;
    private final Socket socket;
    private final Timers timers;

    private Stack sigaltstack;

    private final Sigset sigmask;

    private static AtomicInteger tidctr = new AtomicInteger(1);
    private static final ThreadLocal<Integer> tid = ThreadLocal.withInitial(() -> allocateTid());

    public static final boolean WARN_ON_FILE_DELETE = System.getProperty("posix.warn.delete") != null;

    private ThreadGroup threadGroup;
    private Map<Integer, Thread> threads;

    private boolean exitGroup;
    private volatile int exitCode;

    private String execfn;
    private String execpath;
    private MemoryMapProvider mapsProvider;
    private PosixPointer mem;

    public Posix() {
        fds = new FileDescriptorManager();
        strace = System.getProperty("posix.strace") != null;
        uname = new Uname();
        vfs = new VFS();
        clock = new Clock();
        time = new Time(clock);
        times = new Times(clock);
        linux = new Linux();
        processInfo = new Info();
        socket = new Socket();
        sigaltstack = null;
        sigmask = new Sigset();
        timers = new Timers();

        execfn = null;
        mapsProvider = null;
        mem = null;

        threadGroup = new ThreadGroup("POSIX Threads");
        threads = new HashMap<>();
        exitGroup = false;
    }

    public void setStrace(boolean value) {
        strace = value;
    }

    private static void assertI64(long x) {
        if ((int) x != x) {
            throw new AssertionError("Value truncated");
        }
    }

    private static String str(String s) {
        return s == null ? "NULL" : '"' + s + '"';
    }

    private static String str(Object o) {
        return o == null ? "NULL" : o.toString();
    }

    public VFS getVFS() {
        return vfs;
    }

    public void setStream(int filedes, InputStream in) {
        fds.setStream(filedes, new PipeStream(in));
    }

    public void setStream(int filedes, OutputStream out) {
        fds.setStream(filedes, new PipeStream(out));
    }

    public void setTTY(int filedes, InputStream in) {
        fds.setStream(filedes, new TTYStream(in));
    }

    public void setTTY(int filedes, OutputStream out) {
        fds.setStream(filedes, new TTYStream(out));
    }

    public void setTTY(int filedes, InputStream in, OutputStream out) {
        fds.setStream(filedes, new TTYStream(in, out));
    }

    public Stream getStream(int filedes) throws PosixException {
        return fds.getStream(filedes);
    }

    public FileDescriptor getFileDescriptor(int filedes) throws PosixException {
        return fds.getFileDescriptor(filedes);
    }

    private int __open(String path, int flags, int mode) throws PosixException {
        if (fds.count() >= processInfo.rlimit_nofile) {
            throw new PosixException(Errno.EMFILE);
        }
        if (path.equals("")) {
            throw new PosixException(Errno.ENOENT);
        }
        String abspath = vfs.resolve(path);
        if (BitTest.test(flags, Fcntl.O_CREAT)) {
            // create file
            if (BitTest.test(flags, Fcntl.O_DIRECTORY)) {
                // O_DIRECTORY cannot be used for write operations
                throw new PosixException(Errno.EINVAL);
            }
            String dirname = VFS.dirname(abspath);
            String basename = VFS.basename(abspath);
            VFSEntry entry = vfs.get(dirname);
            if (!(entry instanceof VFSDirectory)) {
                throw new PosixException(Errno.ENOTDIR);
            }
            VFSDirectory dir = (VFSDirectory) entry;
            long uid = getuid();
            long gid = getgid();
            long permissions = mode;
            VFSFile file = dir.mkfile(basename, uid, gid, permissions);
            Stream stream = file.open(flags, mode);
            int fd = fds.allocate(stream);
            fds.getFileDescriptor(fd).name = abspath;
            return fd;
        } else {
            VFSEntry entry = vfs.get(abspath);
            if (entry instanceof VFSDirectory) {
                if (BitTest.test(flags, Fcntl.O_WRONLY) || BitTest.test(flags, Fcntl.O_RDWR)) {
                    // directory cannot be opened in write mode
                    throw new PosixException(Errno.EISDIR);
                }
                VFSDirectory dir = (VFSDirectory) entry;
                Stream stream = dir.open(flags, mode);
                int fd = fds.allocate(stream);
                fds.getFileDescriptor(fd).name = abspath;
                return fd;
            } else if (BitTest.test(flags, Fcntl.O_DIRECTORY)) {
                throw new PosixException(Errno.ENOTDIR);
            }
            VFSFile file = (VFSFile) entry;
            Stream stream = file.open(flags, mode);
            int fd = fds.allocate(stream);
            fds.getFileDescriptor(fd).name = abspath;
            return fd;
        }
    }

    public int open(String path, int flags, int mode) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("open(%s, %s, %s)", str(path), Fcntl.flags(flags), Stat.mode(mode)));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        return __open(path, flags, mode);
    }

    public int openat(int fd, String path, int oflag, int mode) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("openat(%d, %s, %s, %s)", fd, str(path), Fcntl.flags(oflag), Stat.mode(mode)));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        if (fd == Fcntl.AT_FDCWD) {
            return __open(path, oflag, mode);
        }
        FileDescriptor dirfd = fds.getFileDescriptor(fd);
        if (!(dirfd.stream instanceof DirectoryStream)) {
            throw new PosixException(Errno.ENOTDIR);
        }
        String dir = dirfd.name;
        String resolved = VFS.resolve(path, dir);
        return __open(resolved, oflag, mode);
    }

    public int creat(String path, int mode) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("creat(%s, %s)", str(path), Stat.mode(mode)));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        return __open(path, Fcntl.O_WRONLY | Fcntl.O_CREAT | Fcntl.O_TRUNC, mode);
    }

    public int close(int filedes) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("close(%d)", filedes));
        }
        FileDescriptor stream = fds.getFileDescriptor(filedes);
        try {
            return stream.close();
        } finally {
            fds.free(filedes);
        }
    }

    public int read(int filedes, PosixPointer buf, long nbyte) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("read(%d, %s, %d)", filedes, buf, nbyte));
        }
        assertI64(nbyte);
        Stream stream = fds.getStream(filedes);
        return stream.read(buf, (int) nbyte);
    }

    public int write(int filedes, PosixPointer buf, long nbyte) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("write(%d, %s, %d)", filedes, buf, nbyte));
        }
        assertI64(nbyte);
        Stream stream = fds.getStream(filedes);
        return stream.write(buf, (int) nbyte);
    }

    public int readv(int filedes, Iovec[] iov) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("readv(%d, IOV[%d])", filedes, iov.length));
        }
        Stream stream = fds.getStream(filedes);
        return stream.readv(iov);
    }

    public int writev(int filedes, Iovec[] iov) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("writev(%d, IOV[%d])", filedes, iov.length));
        }
        Stream stream = fds.getStream(filedes);
        return stream.writev(iov);
    }

    public int pread64(int filedes, PosixPointer buf, long nbyte, long offset) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("pread64(%d, %s, %d, %d)", filedes, buf, nbyte, offset));
        }
        Stream stream = fds.getStream(filedes);
        return stream.pread(buf, (int) nbyte, offset);
    }

    public int pwrite64(int filedes, PosixPointer buf, long nbyte, long offset) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("pwrite64(%d, %s, %d, %d)", filedes, buf, nbyte, offset));
        }
        Stream stream = fds.getStream(filedes);
        return stream.pwrite(buf, (int) nbyte, offset);
    }

    public long lseek(int filedes, long offset, int whence) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("lseek(%d, %d, %d)", filedes, offset, whence));
        }
        Stream stream = fds.getStream(filedes);
        return stream.lseek(offset, whence);
    }

    // Linux specific
    public long sendfile32(int out_fd, int in_fd, PosixPointer offset, long count) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("sendfile(%d, %d, ptr, %d)", out_fd, in_fd, count));
        }
        Stream out = fds.getStream(out_fd);
        Stream in = fds.getStream(in_fd);
        long off = offset == null ? -1 : offset.getI32();
        long copied = in.sendfile(out, off, count);
        if (offset != null) {
            offset.setI32((int) (off + copied));
        }
        return copied;
    }

    public long sendfile64(int out_fd, int in_fd, PosixPointer offset, long count) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("sendfile(%d, %d, ptr, %d)", out_fd, in_fd, count));
        }
        Stream out = fds.getStream(out_fd);
        Stream in = fds.getStream(in_fd);
        long off = offset == null ? -1 : offset.getI64();
        long copied = in.sendfile(out, off, count);
        if (offset != null) {
            offset.setI64(off + copied);
        }
        return copied;
    }

    public long fcntl(int fildes, int cmd, int arg) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("fcntl(%d, %s, %d)", fildes, Fcntl.fcntl(cmd), arg));
        }
        Stream stream = fds.getStream(fildes);
        FileDescriptor fd = fds.getFileDescriptor(fildes);
        switch (cmd) {
            case Fcntl.F_GETFD:
                return fd.getFlags();
            case Fcntl.F_SETFD:
                fd.setFlags(arg);
                return 0;
            case Fcntl.F_GETFL:
                return stream.getFlags();
            case Fcntl.F_SETFL:
                stream.setFlags(arg);
                return 0;
            case Fcntl.F_DUPFD: {
                if (arg < 0) {
                    throw new PosixException(Errno.EINVAL);
                }
                int newfd = fds.allocate(stream, arg);
                FileDescriptor nfd = fds.getFileDescriptor(newfd);
                nfd.name = fd.name;
                nfd.setFlags(fd.getFlags() & ~Fcntl.FD_CLOEXEC);
                return newfd;
            }
            case Fcntl.F_DUPFD_CLOEXEC: {
                if (arg < 0) {
                    throw new PosixException(Errno.EINVAL);
                }
                int newfd = fds.allocate(stream, arg);
                FileDescriptor nfd = fds.getFileDescriptor(newfd);
                nfd.name = fd.name;
                nfd.setFlags(fd.getFlags() | Fcntl.FD_CLOEXEC);
                return newfd;
            }
            default:
                throw new PosixException(Errno.EINVAL);
        }
    }

    public int ftruncate(int fildes, long length) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("ftruncate(%d, %d)", fildes, length));
        }
        Stream stream = fds.getStream(fildes);
        stream.ftruncate(length);
        return 0;
    }

    public int uname(Utsname buf) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("uname(%s)", buf));
        }
        if (buf == null) {
            throw new PosixException(Errno.EFAULT);
        }
        uname.uname(buf);
        return 0;
    }

    public int readlink(String path, PosixPointer buf, long bufsize) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("readlink(%s, %s, %d)", str(path), buf, bufsize));
        }
        if (buf == null) {
            throw new PosixException(Errno.EFAULT);
        }
        String link = vfs.readlink(path);
        int len = link.length();
        if (len > bufsize) {
            len = (int) bufsize;
        }
        CString.memcpy(buf, link.getBytes(), len);
        return len;
    }

    public int readlinkat(int fd, String path, PosixPointer buf, long bufsize) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("readlinkat(%s, %s, %s, %d)", fd, str(path), buf, bufsize));
        }
        if (buf == null) {
            throw new PosixException(Errno.EFAULT);
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        if (fd == Fcntl.AT_FDCWD) {
            return __readlink(path, buf, bufsize);
        }

        FileDescriptor dirfd = fds.getFileDescriptor(fd);
        if (!(dirfd.stream instanceof DirectoryStream)) {
            throw new PosixException(Errno.ENOTDIR);
        }
        String dir = dirfd.name;
        String resolved = VFS.resolve(path, dir);
        return __readlink(resolved, buf, bufsize);
    }

    private int __readlink(String path, PosixPointer buf, long bufsize) throws PosixException {
        String link = vfs.readlink(path);
        int len = link.length();
        if (len > bufsize) {
            len = (int) bufsize;
        }
        CString.memcpy(buf, link.getBytes(), len);
        return len;
    }

    public int unlink(String path) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("unlink(%s)", str(path)));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        if (path.equals("")) {
            throw new PosixException(Errno.ENOENT);
        }
        VFSEntry entry = vfs.get(path);
        if (entry instanceof VFSDirectory) {
            throw new PosixException(Errno.EPERM);
        }
        if (entry instanceof VFSFile) {
            String dirname = VFS.dirname(path);
            String filename = VFS.basename(path);
            VFSDirectory dir = vfs.get(dirname);
            dir.unlink(filename);
        }
        return 0;
    }

    public int chown(String path, long owner, long group) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("chown(%s, %d, %d)", str(path), owner, group));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        VFSEntry entry = vfs.get(path);
        entry.chown(owner, group);
        return 0;
    }

    public int chmod(String path, int mode) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("chmod(%s, %s)", str(path), Stat.mode(mode)));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        VFSEntry entry = vfs.get(path);
        entry.chmod(mode);
        return 0;
    }

    public int utime(String path, @SuppressWarnings("hiding") Utimbuf times) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("utime(%s, %s)", str(path), times));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        VFSEntry entry = vfs.get(path);
        entry.utime(times);
        return 0;
    }

    public int symlink(String target, String linkpath) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("symlink(%s, %s)", str(target), str(linkpath)));
        }
        if (target == null || linkpath == null) {
            throw new PosixException(Errno.EFAULT);
        }
        try {
            vfs.get(linkpath);
            throw new PosixException(Errno.EEXIST);
        } catch (PosixException e) {
            if (e.getErrno() != Errno.ENOENT) {
                throw e;
            }
        }
        String dirname = VFS.dirname(linkpath);
        String basename = VFS.basename(linkpath);
        VFSEntry entry = vfs.get(dirname);
        if (entry instanceof VFSDirectory) {
            VFSDirectory dir = (VFSDirectory) entry;
            dir.symlink(basename, getuid(), getgid(), 0777, target);
        }
        return 0;
    }

    public int mkdir(String path, int mode) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("mkdir(%s, %s)", str(path), Stat.mode(mode)));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        try {
            vfs.get(path);
            throw new PosixException(Errno.EEXIST);
        } catch (PosixException e) {
            if (e.getErrno() == Errno.ENOENT) {
                vfs.mkdir(path, getuid(), getgid(), mode);
                return 0;
            } else {
                throw e;
            }
        }
    }

    public int chdir(String path) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("chdir(%s)", str(path)));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        vfs.chdir(path);
        return 0;
    }

    public int fchdir(int fildes) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("fchdir(%s)", fildes));
        }
        FileDescriptor fd = fds.getFileDescriptor(fildes);
        if (fd.stream instanceof DirectoryStream) {
            vfs.chdir(fd.name);
            return 0;
        } else {
            throw new PosixException(Errno.ENOTDIR);
        }
    }

    public PosixPointer getcwd(PosixPointer buf, long size) throws PosixException {
        String cwd = vfs.getcwd();
        if (size == 0) {
            throw new PosixException(Errno.EINVAL);
        }
        if (size < (cwd.length() + 1)) {
            throw new PosixException(Errno.ERANGE);
        }
        CString.strcpy(buf, cwd);
        return buf;
    }

    public int dup(int fildes) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("dup(%d)", fildes));
        }
        Stream stream = fds.getStream(fildes);
        int newfd = fds.allocate(stream);
        fds.getFileDescriptor(newfd).name = fds.getFileDescriptor(fildes).name;
        return newfd;
    }

    public int dup2(int fildes, int fildes2) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("dup2(%d, %d)", fildes, fildes2));
        }
        Stream stream = fds.getStream(fildes);
        if (fildes == fildes2) {
            return fildes;
        } else if (fds.used(fildes2)) {
            FileDescriptor old = fds.getFileDescriptor(fildes2);
            old.close();
        }
        fds.setStream(fildes2, stream);
        fds.getFileDescriptor(fildes2).name = fds.getFileDescriptor(fildes).name;
        return fildes2;
    }

    // dup3 is Linux specific
    public int dup3(int oldfd, int newfd, int flags) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("dup3(%d, %d, %s)", oldfd, newfd, Fcntl.flags(flags)));
        }
        Stream stream = fds.getStream(oldfd);
        if (oldfd == newfd) {
            throw new PosixException(Errno.EINVAL);
        }
        if (fds.used(newfd)) {
            FileDescriptor old = fds.getFileDescriptor(newfd);
            old.close();
        }
        fds.setStream(newfd, stream, flags);
        fds.getFileDescriptor(newfd).name = fds.getFileDescriptor(oldfd).name;
        return newfd;
    }

    public int access(String path, int amode) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("access(%s, %s)", str(path), Unistd.amode(amode)));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        vfs.get(path);
        // TODO: check permissions
        return 0;
    }

    public int stat(String path, Stat buf) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("stat(%s, %s)", str(path), buf));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        VFSEntry entry = vfs.get(path);
        entry.stat(buf);
        return 0;
    }

    public int lstat(String path, Stat buf) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("lstat(%s, %s)", str(path), buf));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        VFSEntry entry = vfs.get(path, false);
        entry.stat(buf);
        return 0;
    }

    public int fstat(int fildes, Stat buf) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("fstat(%d, %s)", fildes, buf));
        }
        Stream stream = fds.getStream(fildes);
        stream.stat(buf);
        return 0;
    }

    public int fstatat(int fd, String path, Stat buf, int flags) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("fstatat(%d, %s, %s, %s)", fd, str(path), buf, Fcntl.statx(flags)));
        }
        if (path == null) {
            throw new PosixException(Errno.EFAULT);
        }
        VFSEntry entry;
        boolean followSymlinks = !BitTest.test(flags, Fcntl.AT_SYMLINK_NOFOLLOW);
        if (fd == Fcntl.AT_FDCWD) {
            entry = vfs.get(path, followSymlinks);
        } else if (BitTest.test(flags, Fcntl.AT_EMPTY_PATH)) {
            if (path == null || path.length() == 0) {
                Stream stream = fds.getStream(fd);
                stream.stat(buf);
                return 0;
            } else {
                FileDescriptor dirfd = fds.getFileDescriptor(fd);
                entry = vfs.get(dirfd.name, followSymlinks);
            }
        } else {
            FileDescriptor dirfd = fds.getFileDescriptor(fd);
            if (!(dirfd.stream instanceof DirectoryStream)) {
                throw new PosixException(Errno.ENOTDIR);
            }
            String resolved = VFS.resolve(path, dirfd.name);
            entry = vfs.get(resolved, followSymlinks);
        }
        entry.stat(buf);
        return 0;
    }

    public int statx(int dir, String pathname, int flags, int mask, Statx statxbuf) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("statx(%s, %s, %s, %s, %s)", Fcntl.fd(dir), str(pathname), Fcntl.statx(flags), Stat.mask(mask), statxbuf));
        }
        if (pathname == null) {
            throw new PosixException(Errno.EFAULT);
        }
        VFSEntry entry;
        boolean followSymlinks = !BitTest.test(flags, Fcntl.AT_SYMLINK_NOFOLLOW);
        if (dir == Fcntl.AT_FDCWD) {
            entry = vfs.get(pathname, followSymlinks);
        } else if (BitTest.test(flags, Fcntl.AT_EMPTY_PATH)) {
            if (pathname == null || pathname.length() == 0) {
                Stream stream = fds.getStream(dir);
                stream.statx(mask, statxbuf);
                return 0;
            } else {
                FileDescriptor dirfd = fds.getFileDescriptor(dir);
                entry = vfs.get(dirfd.name, followSymlinks);
            }
        } else {
            FileDescriptor dirfd = fds.getFileDescriptor(dir);
            if (!(dirfd.stream instanceof DirectoryStream)) {
                throw new PosixException(Errno.ENOTDIR);
            }
            String resolved = VFS.resolve(pathname, dirfd.name);
            entry = vfs.get(resolved, followSymlinks);
        }
        entry.statx(mask, statxbuf);
        return 0;
    }

    public int ioctl(int fildes, long request, PosixPointer argp) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("ioctl(%d, %s /* 0x%08x */, %s)", fildes, Ioctls.toString((int) request), request, argp));
        }
        Stream stream = fds.getStream(fildes);
        FileDescriptor fd = fds.getFileDescriptor(fildes);
        // handle generic ioctls
        switch ((int) request) {
            case Ioctls.FIOCLEX:
                fd.setFlags(fd.getFlags() | Fcntl.FD_CLOEXEC);
                return 0;
            case Ioctls.FIONCLEX:
                fd.setFlags(fd.getFlags() & ~Fcntl.FD_CLOEXEC);
                return 0;
            default:
                return stream.ioctl(request, argp);
        }
    }

    public PosixPointer mmap(PosixPointer addr, long length, int prot, int flags, int fildes, long offset) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("mmap(%s, %d, %s, %s, %d, %d)", addr, length, Mman.prot(prot), Mman.flags(flags), fildes, offset));
        }
        if (BitTest.test(flags, Mman.MAP_ANONYMOUS)) {
            throw new PosixException(Errno.ENOMEM);
        } else if (BitTest.test(flags, Mman.MAP_PRIVATE)) {
            Stream stream = fds.getStream(fildes);
            return stream.mmap(length, prot, flags, offset);
        }
        throw new PosixException(Errno.ENOMEM);
    }

    public int munmap(PosixPointer addr, long length) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("munmap(%s, %d)", addr, length));
        }
        throw new PosixException(Errno.ENOMEM);
    }

    public int clock_getres(int clk_id, Timespec tp) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("clock_getres(%s, tp)", Clock.getClockName(clk_id)));
        }
        return clock.clock_getres(clk_id, tp);
    }

    public int clock_gettime(int clk_id, Timespec tp) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("clock_gettime(%s, tp)", Clock.getClockName(clk_id)));
        }
        return clock.clock_gettime(clk_id, tp);
    }

    public int gettimeofday(Timeval tp, @SuppressWarnings("unused") Object tzp) {
        if (strace) {
            log.log(Levels.INFO, "gettimeofday(tp, NULL)");
        }
        return clock.gettimeofday(tp);
    }

    public long times(Tms buffer) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, "times(...)");
        }
        return times.times(buffer);
    }

    public int nanosleep(Timespec rqtp, @SuppressWarnings("unused") Timespec rmtp) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("nanosleep(%s, %s)", rqtp, rmtp));
        }
        if (rqtp.tv_nsec < 0 || rqtp.tv_nsec > 999999999) {
            throw new PosixException(Errno.EINVAL);
        }
        long ms = rqtp.tv_sec * 1000 + rqtp.tv_nsec / 1000000;
        long ns = rqtp.tv_nsec % 1000000;
        try {
            Thread.sleep(ms, (int) ns);
            return 0;
        } catch (InterruptedException e) {
            // update rmtp
            throw new PosixException(Errno.EINTR);
            // return -1;
        }
    }

    public int clock_nanosleep(int clockid, int flags, Timespec request, Timespec remain) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("clock_nanosleep(%s, %s, %s, %s)", Clock.getClockName(clockid), Time.timerFlags(flags), request, remain));
        }
        return time.clock_nanosleep(clockid, flags, request, remain);
    }

    public int timer_create(int clockid, Sigevent evp, PosixPointer timerid) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("timer_create(%s, %s, %s)", Clock.getClockName(clockid), evp, timerid));
        }
        switch (clockid) {
            case Clock.CLOCK_MONOTONIC:
            case Clock.CLOCK_REALTIME:
                break;
            default:
                throw new PosixException(Errno.EINVAL);
        }
        // TODO: create timer
        int id = timers.create(new Timer(clockid, evp));
        timerid.setI32(id);
        return 0;
    }

    public int timer_settime(int timerid, int flags, Itimerspec new_value, Itimerspec old_value) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("timer_settime(%d, %s, %s, %s)", timerid, Time.timerFlags(flags), str(new_value), str(old_value)));
        }

        if (new_value == null) {
            throw new PosixException(Errno.EFAULT);
        }

        Timer timer = timers.get(timerid);

        if (old_value != null) {
            old_value.it_interval.copyFrom(timer.getInterval());
            old_value.it_value.copyFrom(timer.getValue());
        }

        timer.setInterval(new_value.it_interval);
        timer.setValue(new_value.it_value);

        return 0;
    }

    public int timer_delete(int timerid) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("timer_delete(%d)", timerid));
        }

        timers.delete(timerid);
        return 0;
    }

    // FIXME: implement properly
    public long getuid() {
        if (strace) {
            log.log(Levels.INFO, "getuid()");
        }
        return 1000;
    }

    public long setuid(long uid) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("setuid(%d)", uid));
        }
        if (uid == 1000) {
            return 0;
        }
        throw new PosixException(Errno.EPERM);
    }

    public long getgid() {
        if (strace) {
            log.log(Levels.INFO, "getgid()");
        }
        return 1000;
    }

    public long setgid(long gid) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("setgid(%d)", gid));
        }
        if (gid == 1000) {
            return 0;
        }
        throw new PosixException(Errno.EPERM);
    }

    // used for internal purposes; does not create a log message
    public static int getTid() {
        return tid.get();
    }

    public static int allocateTid() {
        return tidctr.getAndIncrement();
    }

    public static void setTid(int newtid) {
        tid.set(newtid);
    }

    public long getpid() {
        if (strace) {
            log.log(Level.INFO, "getpid()");
        }
        return 1; // TODO
    }

    public long gettid() {
        if (strace) {
            log.log(Level.INFO, "gettid()");
        }
        return getTid();
    }

    public int getrlimit(int resource, Rlimit rlp) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("getrlimit(%s, ...)", Resource.toString(resource)));
        }
        if (rlp == null) {
            throw new PosixException(Errno.EFAULT);
        }
        switch (resource) {
            case Resource.RLIMIT_CORE:
                rlp.rlim_cur = processInfo.rlimit_core;
                rlp.rlim_max = processInfo.rlimit_core;
                break;
            case Resource.RLIMIT_CPU:
                rlp.rlim_cur = processInfo.rlimit_cpu;
                rlp.rlim_max = processInfo.rlimit_cpu;
                break;
            case Resource.RLIMIT_DATA:
                rlp.rlim_cur = processInfo.rlimit_data;
                rlp.rlim_max = processInfo.rlimit_data;
                break;
            case Resource.RLIMIT_FSIZE:
                rlp.rlim_cur = processInfo.rlimit_fsize;
                rlp.rlim_max = processInfo.rlimit_fsize;
                break;
            case Resource.RLIMIT_NOFILE:
                rlp.rlim_cur = processInfo.rlimit_nofile;
                rlp.rlim_max = processInfo.rlimit_nofile;
                break;
            case Resource.RLIMIT_STACK:
                rlp.rlim_cur = processInfo.rlimit_stack;
                rlp.rlim_max = processInfo.rlimit_stack;
                break;
            case Resource.RLIMIT_AS:
                rlp.rlim_cur = processInfo.rlimit_as;
                rlp.rlim_max = processInfo.rlimit_as;
                break;
            case Resource.RLIMIT_NICE:
                rlp.rlim_cur = processInfo.rlimit_nice;
                rlp.rlim_max = processInfo.rlimit_nice;
                break;
            case Resource.RLIMIT_RTPRIO:
                rlp.rlim_cur = processInfo.rlimit_rtprio;
                rlp.rlim_max = processInfo.rlimit_rtprio;
                break;
            case Resource.RLIMIT_RTTIME:
                rlp.rlim_cur = processInfo.rlimit_rttime;
                rlp.rlim_max = processInfo.rlimit_rttime;
                break;
            case Resource.RLIMIT_RSS:
                rlp.rlim_cur = processInfo.rlimit_rss;
                rlp.rlim_max = processInfo.rlimit_rss;
                break;
            case Resource.RLIMIT_LOCKS:
                rlp.rlim_cur = processInfo.rlimit_locks;
                rlp.rlim_max = processInfo.rlimit_locks;
                break;
            case Resource.RLIMIT_MEMLOCK:
                rlp.rlim_cur = processInfo.rlimit_memlock;
                rlp.rlim_max = processInfo.rlimit_memlock;
                break;
            case Resource.RLIMIT_MSGQUEUE:
                rlp.rlim_cur = processInfo.rlimit_msgqueue;
                rlp.rlim_max = processInfo.rlimit_msgqueue;
                break;
            case Resource.RLIMIT_NPROC:
                rlp.rlim_cur = processInfo.rlimit_nproc;
                rlp.rlim_max = processInfo.rlimit_nproc;
                break;
            case Resource.RLIMIT_SIGPENDING:
                rlp.rlim_cur = processInfo.rlimit_sigpending;
                rlp.rlim_max = processInfo.rlimit_sigpending;
                break;
            default:
                throw new PosixException(Errno.EINVAL);
        }
        return 0;
    }

    public int sigaction(int sig, Sigaction act, Sigaction oact) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("sigaction(%s, ...)", Signal.toString(sig)));
        }
        if (sig < 1 || sig >= Signal._NSIG) {
            throw new PosixException(Errno.EINVAL);
        }
        if (oact != null) {
            oact.copyFrom(processInfo.signal_handlers[sig]);
        }
        if (act != null) {
            processInfo.signal_handlers[sig].copyFrom(act);
        }
        return 0;
    }

    public int sigprocmask(int how, Sigset set, Sigset oldset) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("sigprocmask(%s, %s, %s)", Signal.sigprocmaskHow(how), set, oldset));
        }
        if (oldset != null) {
            oldset.setmask(sigmask);
        }
        switch (how) {
            case Signal.SIG_BLOCK:
                if (set != null) {
                    sigmask.block(set);
                }
                break;
            case Signal.SIG_UNBLOCK:
                if (set != null) {
                    sigmask.unblock(set);
                }
                break;
            case Signal.SIG_SETMASK:
                if (set != null) {
                    sigmask.setmask(set);
                }
                break;
            default:
                throw new PosixException(Errno.EINVAL);
        }
        return 0;
    }

    public int sigaltstack(Stack ss, Stack old_ss) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("sigaltstack(%s, %s)", ss, old_ss));
        }
        if (old_ss != null) {
            if (sigaltstack == null) {
                old_ss.ss_sp = 0;
                old_ss.ss_flags = Stack.SS_DISABLE;
                old_ss.ss_size = 0;
            } else {
                old_ss.ss_sp = sigaltstack.ss_sp;
                old_ss.ss_flags = 0; // we are not running on the altstack
                old_ss.ss_size = sigaltstack.ss_size;
            }
        }
        if (ss != null) {
            if (ss.ss_size < Stack.MINSIGSTKSZ) {
                throw new PosixException(Errno.ENOMEM);
            } else if (ss.ss_flags != 0 && ss.ss_flags != Stack.SS_DISABLE) {
                throw new PosixException(Errno.EINVAL);
            }
            if (BitTest.test(ss.ss_flags, Stack.SS_DISABLE)) {
                sigaltstack = null;
            } else {
                sigaltstack = new Stack();
                sigaltstack.ss_sp = ss.ss_sp;
                sigaltstack.ss_size = ss.ss_size;
            }
        }
        return 0;
    }

    // TODO: implement
    // public int select(int nfds, FdSet readfds, FdSet writefds, FdSet errorfds, Timeval timeout)
    // throws PosixException {
    // return -1;
    // }

    public int poll(Pollfd[] pfds, int nfds, int timeout) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("poll(..., %s, %s)", nfds, timeout));
        }
        if (pfds.length < nfds) {
            throw new PosixException(Errno.EFAULT);
        }
        Stream[] streams = new Stream[nfds];
        boolean sockets = false;
        boolean other = false;
        for (int i = 0; i < nfds; i++) {
            streams[i] = fds.getStream(pfds[i].fd);
            if (streams[i] instanceof NetworkStream) {
                sockets = true;
            } else {
                other = true;
            }
        }
        if (sockets && !other) {
            Selector selector;
            try {
                selector = Selector.open();
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to create selector: " + e.getMessage(), e);
                throw new PosixException(Errno.EIO);
            }

            boolean[] wasBlocking = new boolean[nfds];
            // register channels
            for (int i = 0; i < nfds; i++) {
                NetworkStream s = (NetworkStream) streams[i];
                Pollfd fd = pfds[i];
                SelectableChannel ch = s.getChannel();
                wasBlocking[i] = ch.isBlocking();
                int ops = 0;
                if (BitTest.test(fd.events, Poll.POLLIN)) {
                    ops |= SelectionKey.OP_READ;
                }
                if (BitTest.test(fd.events, Poll.POLLOUT)) {
                    ops |= SelectionKey.OP_WRITE;
                }
                try {
                    ch.configureBlocking(false);
                    ch.register(selector, ops, i);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Failed to register channel: " + e.getMessage(), e);
                    for (SelectionKey key : selector.keys()) {
                        key.cancel();
                    }
                    for (int j = 0; j <= i; j++) {
                        NetworkStream stream = (NetworkStream) streams[j];
                        try {
                            stream.getChannel().configureBlocking(wasBlocking[j]);
                        } catch (IOException ex) {
                            log.log(Level.WARNING, "Cannot reset blocking configuration of channel: " + ex.getMessage(), ex);
                        }
                    }
                    try {
                        selector.close();
                    } catch (IOException ex) {
                        log.log(Level.WARNING, "Failed to close selector: " + ex.getMessage(), ex);
                    }
                    throw new PosixException(Errno.EINVAL);
                }
            }

            // select
            int result;
            try {
                if (timeout < 0) {
                    result = selector.select(0);
                } else if (timeout > 0) {
                    result = selector.select(timeout);
                } else {
                    result = selector.selectNow();
                }

                for (SelectionKey key : selector.selectedKeys()) {
                    int i = (int) key.attachment();
                    Pollfd fd = pfds[i];
                    fd.revents = 0;
                    if (BitTest.test(fd.events, Poll.POLLIN)) {
                        if (key.isReadable()) {
                            fd.revents |= Poll.POLLIN;
                        }

                        // if the stream can "accept", also check isAcceptable
                        NetworkStream stream = (NetworkStream) streams[i];
                        SelectableChannel ch = stream.getChannel();
                        if ((ch instanceof DatagramChannel || ch instanceof ServerSocketChannel) && key.isAcceptable()) {
                            fd.revents |= Poll.POLLIN;
                        }
                    }
                    if (BitTest.test(fd.events, Poll.POLLOUT) && key.isWritable()) {
                        fd.revents |= Poll.POLLOUT;
                    }
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Error in poll: " + e.getMessage());
                throw new PosixException(Errno.EIO);
            } finally {
                // unregister all channels again
                for (SelectionKey key : selector.keys()) {
                    key.cancel();
                }
                for (int i = 0; i < nfds; i++) {
                    NetworkStream stream = (NetworkStream) streams[i];
                    try {
                        stream.getChannel().configureBlocking(wasBlocking[i]);
                    } catch (IOException ex) {
                        log.log(Level.WARNING, "Cannot reset blocking configuration of channel: " + ex.getMessage(), ex);
                    }
                }

                // close selector
                try {
                    selector.close();
                } catch (IOException ex) {
                    log.log(Level.WARNING, "Failed to close selector: " + ex.getMessage(), ex);
                }
            }
            return result;
        } else {
            // handle poll with empty events field and timeout=0 (like e.g. in rust startup)
            boolean fail = false;
            if (timeout != 0) {
                fail = true;
            }
            for (int i = 0; i < nfds; i++) {
                if (pfds[i].events != 0) {
                    fail = true;
                    break;
                }
            }

            if (fail) {
                log.log(Level.WARNING, "Cannot poll on non-socket streams");
                throw new PosixException(Errno.EINVAL);
            } else {
                return 0;
            }
        }
    }

    public int setsockopt(int sock, int level, int option_name, int option_value) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("setsockopt(%s, %s, %s, %s)", sock, Socket.sockoptLevel(level), Socket.sockoptOption(level, option_name), option_value));
        }
        Stream stream = fds.getStream(sock);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.setsockopt(level, option_name, option_value);
        }
    }

    public Sockaddr getsockname(int sock) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("getsockname(%s)", sock));
        }
        Stream stream = fds.getStream(sock);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.getsockname();
        }
    }

    public Sockaddr getpeername(int sock) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("getpeername(%s)", sock));
        }
        Stream stream = fds.getStream(sock);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.getpeername();
        }
    }

    public int shutdown(int sock, int how) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("shutdown(%s, %s)", sock, Socket.shutdownHow(how)));
        }
        Stream stream = fds.getStream(sock);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.shutdown(how);
        }
    }

    public long send(int sock, PosixPointer buffer, long length, int flags) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("send(%s, %s, %s, %s)", sock, buffer, length, Socket.sendrecvFlags(flags)));
        }
        assertI64(length);
        Stream stream = fds.getStream(sock);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.send(buffer, length, flags);
        }
    }

    public long recv(int sock, PosixPointer buffer, long length, int flags) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("recv(%s, %s, %s, %s)", sock, buffer, length, Socket.sendrecvFlags(flags)));
        }
        assertI64(length);
        Stream stream = fds.getStream(sock);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.recv(buffer, length, flags);
        }
    }

    public long sendmsg(int sockfd, Msghdr message, int flags) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("sendmsg(%s, ..., %s)", sockfd, Socket.sendrecvFlags(flags)));
        }
        Stream stream = fds.getStream(sockfd);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.sendmsg(message, flags);
        }
    }

    public long recvmsg(int sockfd, Msghdr message, int flags) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("recvmsg(%s, ..., %s)", sockfd, Socket.sendrecvFlags(flags)));
        }
        Stream stream = fds.getStream(sockfd);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.recvmsg(message, flags);
        }
    }

    public int sendmmsg(int sockfd, Mmsghdr[] msgvec, int vlen, int flags) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("sendmmsg(%s, ..., %s, %s)", sockfd, vlen, Socket.sendrecvFlags(flags)));
        }
        Stream stream = fds.getStream(sockfd);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.sendmmsg(msgvec, vlen, flags);
        }
    }

    public long sendto(int sock, PosixPointer message, long length, int flags, PosixPointer dest_addr, int dest_len) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("sendto(%s, %s, %s, %s, %s, %s)", sock, message, length, Socket.sendrecvFlags(flags), Sockaddr.get(dest_addr, dest_len), dest_len));
        }
        assertI64(length);
        Stream stream = fds.getStream(sock);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.sendto(message, length, flags, dest_addr, dest_len);
        }
    }

    public RecvResult recvfrom(int sock, PosixPointer buffer, long length, int flags) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("recvfrom(%s, %s, %s, %s)", sock, buffer, length, Socket.sendrecvFlags(flags)));
        }
        assertI64(length);
        Stream stream = fds.getStream(sock);
        if (!(stream instanceof NetworkStream)) {
            throw new PosixException(Errno.ENOTSOCK);
        } else {
            NetworkStream nstream = (NetworkStream) stream;
            return nstream.recvfrom(buffer, length, flags);
        }
    }

    public int socket(int domain, int type, int protocol) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("socket(%s, %s, %s)", Socket.addressFamily(domain), Socket.type(type), Socket.protocol(domain, protocol)));
        }
        if (fds.count() >= processInfo.rlimit_nofile) {
            throw new PosixException(Errno.EMFILE);
        }
        Stream stream = socket.socket(domain, type, protocol);
        int fd = fds.allocate(stream);
        return fd;
    }

    public int connect(int sockfd, PosixPointer address, int addressLen) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("connect(%d, %s, %d)", sockfd, Sockaddr.get(address, addressLen), addressLen));
        }
        Stream stream = fds.getStream(sockfd);
        if (stream instanceof NetworkStream) {
            NetworkStream sock = (NetworkStream) stream;
            return sock.connect(address, addressLen);
        } else {
            throw new PosixException(Errno.ENOTSOCK);
        }
    }

    public int bind(int sockfd, PosixPointer address, int addressLen) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("bind(%d, %s, %d)", sockfd, Sockaddr.get(address, addressLen), addressLen));
        }
        Stream stream = fds.getStream(sockfd);
        if (stream instanceof NetworkStream) {
            NetworkStream sock = (NetworkStream) stream;
            return sock.bind(address, addressLen);
        } else {
            throw new PosixException(Errno.ENOTSOCK);
        }
    }

    public int listen(int sockfd, int backlog) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("listen(%d, %d)", sockfd, backlog));
        }
        Stream stream = fds.getStream(sockfd);
        if (stream instanceof NetworkStream) {
            NetworkStream sock = (NetworkStream) stream;
            return sock.listen(backlog);
        } else {
            throw new PosixException(Errno.ENOTSOCK);
        }
    }

    // Linux specific functions
    public long getrandom(PosixPointer buf, long buflen, int flags) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("getrandom(%s, %s, %s)", buf, buflen, org.graalvm.vm.posix.api.linux.Random.toString(flags)));
        }
        return linux.getrandom(buf, buflen, flags);
    }

    public int sysinfo(Sysinfo info) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, "sysinfo(...)");
        }
        return linux.sysinfo(info);
    }

    public long getdents(int fd, PosixPointer dirp, long count, int size) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("getdents(%d, %s, %d)", fd, dirp, count));
        }
        Stream s = fds.getStream(fd);
        if (s instanceof DirectoryStream) {
            DirectoryStream ds = (DirectoryStream) s;
            return ds.getdents(dirp, count, size);
        } else {
            throw new PosixException(Errno.ENOTDIR);
        }
    }

    public int futex(PosixPointer uaddr, int futex_op, int val, PosixPointer timeout, PosixPointer uaddr2, int val3) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("futex(%s, %s, %d, %s, %s, %d)", uaddr, Futex.op(futex_op), val, timeout, uaddr2, val3));
        }
        return linux.futex(uaddr, futex_op, val, timeout, uaddr2, val3);
    }

    // for internal purposes: like set_tid_address, but without strace
    public void setTidAddress(PosixPointer tidptr) {
        linux.set_tid_address(tidptr);
    }

    public long set_tid_address(PosixPointer tidptr) {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("set_tid_address(%s)", tidptr));
        }
        return linux.set_tid_address(tidptr);
    }

    public long set_robust_list(PosixPointer head, long len) throws PosixException {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("set_robust_list(%s, %s)", head, len));
        }
        return linux.set_robust_list(head, len);
    }

    private void threadCleanup(String func) {
        PosixPointer clearChildTid = linux.getClearChildTid();
        if (clearChildTid != null) {
            try {
                clearChildTid.setI32(0);
            } catch (Exception e) {
                return; // ignore; futex points to invalid memory
            }
            try {
                linux.futex(clearChildTid, Futex.FUTEX_WAKE, 1, null, null, 0);
            } catch (PosixException e) {
                if (strace) {
                    log.log(Levels.INFO, () -> String.format("%s: futex operation failed: %s", func, Errno.toString(e.errno)));
                }
            }
        }
        threadKilled(getTid(), Thread.currentThread());
    }

    public void exit(int code) {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("exit(%s)", code));
        }
        threadCleanup("exit(" + code + ")");
        throw new ProcessExitException(code);
    }

    public void exit_group(int code) {
        if (strace) {
            log.log(Levels.INFO, () -> String.format("exit_group(%s)", code));
        }
        exitCode = code;
        exitGroup = true;
        Thread currentThread = Thread.currentThread();
        synchronized (threads) {
            threads.forEach((threadId, t) -> {
                if (t != currentThread) {
                    t.interrupt();
                }
            });
        }
        threadCleanup("exit_group(" + code + ")");
        throw new ProcessExitException(code);
    }

    public int sched_yield() {
        if (strace) {
            log.log(Levels.INFO, "sched_yield()");
        }
        Thread.yield();
        return 0;
    }

    // these functions are not part of the POSIX interface
    public boolean isExitGroup() {
        return exitGroup;
    }

    public void handleExitGroup() {
        if (exitGroup) {
            threadCleanup("exit_group(" + exitCode + ")");
            throw new ProcessExitException(exitCode);
        }
    }

    public void joinAllThreads() {
        Thread self = Thread.currentThread();

        // check thread group
        Thread[] list = new Thread[256];
        while (true) {
            int cnt = threadGroup.enumerate(list);
            if (cnt > 0) {
                for (int i = 0; i < cnt; i++) {
                    if (list[i] != self) {
                        try {
                            list[i].join();
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                }
            } else {
                break;
            }
        }
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public void addThread(int id, Thread thread) {
        synchronized (threads) {
            if (threads.get(id) != null && threads.get(id) != thread) {
                throw new IllegalArgumentException("thread already registered");
            }
            threads.put(id, thread);
        }
    }

    public void threadKilled(int id, Thread thread) {
        synchronized (threads) {
            if (threads.get(id) != null && threads.get(id) != thread) {
                throw new IllegalArgumentException("thread was not registered");
            }
            threads.remove(id);
        }
    }

    public boolean hasThread(int id) {
        return threads.get(id) != null;
    }

    public Set<Integer> getTids() {
        return threads.keySet();
    }

    public int getThreadCount() {
        return threads.size();
    }

    public Stack getSigaltstack() {
        return sigaltstack;
    }

    public void setExecfn(String execfn) {
        this.execfn = execfn;
    }

    public String getExecfn() {
        return execfn;
    }

    public void setExecpath(String execpath) {
        this.execpath = execpath;
    }

    public String getExecpath() {
        return execpath;
    }

    public void setMemoryMapProvider(MemoryMapProvider provider) {
        mapsProvider = provider;
    }

    public MemoryMapProvider getMemoryMapProvider() {
        return mapsProvider;
    }

    public PosixPointer getMemory() {
        return mem;
    }

    public void setMemory(PosixPointer mem) {
        this.mem = mem;
    }

    public Info getProcessInfo() {
        return processInfo;
    }

    public Uname getUname() {
        return uname;
    }
}
