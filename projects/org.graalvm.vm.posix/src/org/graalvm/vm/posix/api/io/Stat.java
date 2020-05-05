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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.graalvm.vm.posix.api.PosixPointer;
import org.graalvm.vm.posix.api.Struct;
import org.graalvm.vm.posix.api.Timespec;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;

public class Stat implements Struct {
    // @formatter:off
    public static final int S_IFMT  = 00170000;
    public static final int S_IFSOCK = 0140000;
    public static final int S_IFLNK  = 0120000;
    public static final int S_IFREG  = 0100000;
    public static final int S_IFBLK  = 0060000;
    public static final int S_IFDIR  = 0040000;
    public static final int S_IFCHR  = 0020000;
    public static final int S_IFIFO  = 0010000;
    public static final int S_ISUID  = 0004000;
    public static final int S_ISGID  = 0002000;
    public static final int S_ISVTX  = 0001000;

    public static final int S_IRWXU = 00700;
    public static final int S_IRUSR = 00400;
    public static final int S_IWUSR = 00200;
    public static final int S_IXUSR = 00100;

    public static final int S_IRWXG = 00070;
    public static final int S_IRGRP = 00040;
    public static final int S_IWGRP = 00020;
    public static final int S_IXGRP = 00010;

    public static final int S_IRWXO = 00007;
    public static final int S_IROTH = 00004;
    public static final int S_IWOTH = 00002;
    public static final int S_IXOTH = 00001;

    public static final int STATX_TYPE            = 0x00000001;     /* Want/got stx_mode & S_IFMT */
    public static final int STATX_MODE            = 0x00000002;     /* Want/got stx_mode & ~S_IFMT */
    public static final int STATX_NLINK           = 0x00000004;     /* Want/got stx_nlink */
    public static final int STATX_UID             = 0x00000008;     /* Want/got stx_uid */
    public static final int STATX_GID             = 0x00000010;     /* Want/got stx_gid */
    public static final int STATX_ATIME           = 0x00000020;     /* Want/got stx_atime */
    public static final int STATX_MTIME           = 0x00000040;     /* Want/got stx_mtime */
    public static final int STATX_CTIME           = 0x00000080;     /* Want/got stx_ctime */
    public static final int STATX_INO             = 0x00000100;     /* Want/got stx_ino */
    public static final int STATX_SIZE            = 0x00000200;     /* Want/got stx_size */
    public static final int STATX_BLOCKS          = 0x00000400;     /* Want/got stx_blocks */
    public static final int STATX_BASIC_STATS     = 0x000007ff;     /* The stuff in the normal stat struct */
    public static final int STATX_BTIME           = 0x00000800;     /* Want/got stx_btime */
    public static final int STATX_ALL             = 0x00000fff;     /* All currently supported flags */
    public static final int STATX__RESERVED       = 0x80000000;     /* Reserved for future struct statx expansion */

    public static final int STATX_ATTR_COMPRESSED = 0x00000004; /* [I] File is compressed by the fs */
    public static final int STATX_ATTR_IMMUTABLE  = 0x00000010; /* [I] File is marked immutable */
    public static final int STATX_ATTR_APPEND     = 0x00000020; /* [I] File is append-only */
    public static final int STATX_ATTR_NODUMP     = 0x00000040; /* [I] File is not to be dumped */
    public static final int STATX_ATTR_ENCRYPTED  = 0x00000800; /* [I] File requires key to decrypt in fs */

    public static final int STATX_ATTR_AUTOMOUNT  = 0x00001000; /* Dir: Automount trigger */
    // @formatter:on

    public long st_dev;
    public long st_ino;
    public int st_mode;
    public int st_nlink;
    public int st_uid;
    public int st_gid;
    public long st_rdev;
    public long st_size;
    public int st_blksize;
    public long st_blocks;
    public Timespec st_atim = new Timespec();
    public Timespec st_mtim = new Timespec();
    public Timespec st_ctim = new Timespec();

    @Override
    public PosixPointer write32(PosixPointer ptr) {
        PosixPointer p = ptr;
        p.setI32((int) st_dev);
        p = p.add(4);
        p.setI32((int) st_ino);
        p = p.add(4);
        p.setI32(st_mode);
        p = p.add(4);
        p.setI32(st_nlink);
        p = p.add(4);
        p.setI32(st_uid);
        p = p.add(4);
        p.setI32(st_gid);
        p = p.add(4);
        p.setI32((int) st_rdev);
        p = p.add(4);
        // unsigned long __pad1
        p = p.add(4);
        p.setI32((int) st_size);
        p = p.add(4);
        p.setI32(st_blksize);
        p = p.add(4);
        // int __pad2
        p = p.add(4);
        p.setI32((int) st_blocks);
        p = p.add(4);
        p.setI32((int) st_atim.tv_sec);
        p = p.add(4);
        p.setI32((int) st_atim.tv_nsec);
        p = p.add(4);
        p.setI32((int) st_mtim.tv_sec);
        p = p.add(4);
        p.setI32((int) st_mtim.tv_nsec);
        p = p.add(4);
        p.setI32((int) st_ctim.tv_sec);
        p = p.add(4);
        p.setI32((int) st_ctim.tv_nsec);
        return p.add(12); // 2x unused
    }

    @Override
    public PosixPointer write64(PosixPointer ptr) {
        PosixPointer p = ptr;
        p.setI64(st_dev);
        p = p.add(8);
        p.setI64(st_ino);
        p = p.add(8);
        p.setI64(st_nlink);
        p = p.add(8);
        p.setI32(st_mode);
        p = p.add(4);
        p.setI32(st_uid);
        p = p.add(4);
        p.setI32(st_gid);
        p = p.add(4);
        p = p.add(4); // int __pad0
        p.setI64(st_rdev);
        p = p.add(8);
        p.setI64(st_size);
        p = p.add(8);
        p.setI64(st_blksize);
        p = p.add(8);
        p.setI64(st_blocks);
        p = p.add(8);
        p.setI64(st_atim.tv_sec);
        p = p.add(8);
        p.setI64(st_atim.tv_nsec);
        p = p.add(8);
        p.setI64(st_mtim.tv_sec);
        p = p.add(8);
        p.setI64(st_mtim.tv_nsec);
        p = p.add(8);
        p.setI64(st_ctim.tv_sec);
        p = p.add(8);
        p.setI64(st_ctim.tv_nsec);
        return p.add(32);
    }

    public PosixPointer write3264(PosixPointer ptr) {
        PosixPointer p = ptr;
        p.setI64(st_dev);
        p = p.add(8);
        p.setI64(st_ino);
        p = p.add(8);
        p.setI32(st_mode);
        p = p.add(4);
        p.setI32(st_nlink);
        p = p.add(4);
        p.setI32(st_uid);
        p = p.add(4);
        p.setI32(st_gid);
        p = p.add(4);
        p.setI64(st_rdev);
        p = p.add(8);
        // unsigned long long __pad1
        p = p.add(8);
        p.setI64(st_size);
        p = p.add(8);
        p.setI32(st_blksize);
        p = p.add(4);
        // int __pad2
        p = p.add(4);
        p.setI64(st_blocks);
        p = p.add(8);
        p.setI32((int) st_atim.tv_sec);
        p = p.add(4);
        p.setI32((int) st_atim.tv_nsec);
        p = p.add(4);
        p.setI32((int) st_mtim.tv_sec);
        p = p.add(4);
        p.setI32((int) st_mtim.tv_nsec);
        p = p.add(4);
        p.setI32((int) st_ctim.tv_sec);
        p = p.add(4);
        p.setI32((int) st_ctim.tv_nsec);
        return p.add(12); // 2x unused
    }

    public static String mode(int mode) {
        List<String> flags = new ArrayList<>();
        int fmt = mode & S_IFMT;
        switch (fmt) {
            case S_IFSOCK:
                flags.add("S_IFSOCK");
                break;
            case S_IFLNK:
                flags.add("S_IFLNK");
                break;
            case S_IFREG:
                flags.add("S_IFREG");
                break;
            case S_IFBLK:
                flags.add("S_IFBLK");
                break;
            case S_IFDIR:
                flags.add("S_IFDIR");
                break;
            case S_IFCHR:
                flags.add("S_IFCHR");
                break;
            case S_IFIFO:
                flags.add("S_IFIFO");
                break;
            case 0:
                break;
            default:
                flags.add(String.format("0x%x", fmt));
        }

        if (BitTest.test(mode, S_ISUID)) {
            flags.add("S_ISUID");
        }
        if (BitTest.test(mode, S_ISGID)) {
            flags.add("S_ISGID");
        }
        if (BitTest.test(mode, S_ISVTX)) {
            flags.add("S_ISVTX");
        }

        if (BitTest.test(mode, S_IRWXU)) {
            flags.add("S_IRWXU");
        } else {
            if (BitTest.test(mode, S_IRUSR)) {
                flags.add("S_IRUSR");
            }
            if (BitTest.test(mode, S_IWUSR)) {
                flags.add("S_IWUSR");
            }
            if (BitTest.test(mode, S_IXUSR)) {
                flags.add("S_IXUSR");
            }
        }

        if (BitTest.test(mode, S_IRWXG)) {
            flags.add("S_IRWXG");
        } else {
            if (BitTest.test(mode, S_IRGRP)) {
                flags.add("S_IRGRP");
            }
            if (BitTest.test(mode, S_IWGRP)) {
                flags.add("S_IWGRP");
            }
            if (BitTest.test(mode, S_IXGRP)) {
                flags.add("S_IXGRP");
            }
        }

        if (BitTest.test(mode, S_IRWXO)) {
            flags.add("S_IRWXO");
        } else {
            if (BitTest.test(mode, S_IROTH)) {
                flags.add("S_IROTH");
            }
            if (BitTest.test(mode, S_IWOTH)) {
                flags.add("S_IWOTH");
            }
            if (BitTest.test(mode, S_IXOTH)) {
                flags.add("S_IXOTH");
            }
        }

        if (flags.size() == 0) {
            return "0";
        } else {
            return flags.stream().collect(Collectors.joining("|"));
        }
    }

    public static String mask(int mask) {
        List<String> flags = new ArrayList<>();
        int remainder = mask;
        if ((mask & STATX_ALL) == STATX_ALL) {
            flags.add("STATX_ALL");
            remainder &= ~STATX_ALL;
        } else if ((mask & STATX_BASIC_STATS) == STATX_BASIC_STATS) {
            flags.add("STATX_BASIC_STATS");
            remainder &= ~STATX_BASIC_STATS;
        }

        if (BitTest.test(remainder, STATX_TYPE)) {
            flags.add("STATX_TYPE");
            remainder &= ~STATX_TYPE;
        }
        if (BitTest.test(remainder, STATX_MODE)) {
            flags.add("STATX_MODE");
            remainder &= ~STATX_MODE;
        }
        if (BitTest.test(remainder, STATX_NLINK)) {
            flags.add("STATX_NLINK");
            remainder &= ~STATX_NLINK;
        }
        if (BitTest.test(remainder, STATX_UID)) {
            flags.add("STATX_UID");
            remainder &= ~STATX_UID;
        }
        if (BitTest.test(remainder, STATX_GID)) {
            flags.add("STATX_GID");
            remainder &= ~STATX_GID;
        }
        if (BitTest.test(remainder, STATX_ATIME)) {
            flags.add("STATX_ATIME");
            remainder &= ~STATX_ATIME;
        }
        if (BitTest.test(remainder, STATX_MTIME)) {
            flags.add("STATX_MTIME");
            remainder &= ~STATX_MTIME;
        }
        if (BitTest.test(remainder, STATX_CTIME)) {
            flags.add("STATX_CTIME");
            remainder &= ~STATX_CTIME;
        }
        if (BitTest.test(remainder, STATX_INO)) {
            flags.add("STATX_INO");
            remainder &= ~STATX_INO;
        }
        if (BitTest.test(remainder, STATX_SIZE)) {
            flags.add("STATX_SIZE");
            remainder &= ~STATX_SIZE;
        }
        if (BitTest.test(remainder, STATX_BLOCKS)) {
            flags.add("STATX_BLOCKS");
            remainder &= ~STATX_BLOCKS;
        }
        if (BitTest.test(remainder, STATX__RESERVED)) {
            flags.add("STATX__RESERVED");
            remainder &= ~STATX__RESERVED;
        }
        if (remainder != 0) {
            flags.add("0x" + HexFormatter.tohex(Integer.toUnsignedLong(remainder)));
        }
        if (flags.isEmpty()) {
            return "0";
        } else {
            return String.join("|", flags);
        }
    }
}
