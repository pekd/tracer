package org.graalvm.vm.posix.api.io;

import org.graalvm.vm.posix.api.PosixPointer;
import org.graalvm.vm.posix.api.Struct;

public class Statx implements Struct {
    public int stx_mask;
    public int stx_blksize;
    public long stx_attributes;
    public int stx_nlink;
    public int stx_uid;
    public int stx_gid;
    public short stx_mode;
    public long stx_ino;
    public long stx_size;
    public long stx_blocks;
    public long stx_attributes_mask;
    public StatxTimestamp stx_atime = new StatxTimestamp();
    public StatxTimestamp stx_btime = new StatxTimestamp();
    public StatxTimestamp stx_ctime = new StatxTimestamp();
    public StatxTimestamp stx_mtime = new StatxTimestamp();
    public int stx_rdev_major;
    public int stx_rdev_minor;
    public int stx_dev_major;
    public int stx_dev_minor;

    public PosixPointer read(PosixPointer ptr) {
        PosixPointer p = ptr;
        stx_mask = p.getI32();
        p = p.add(4);
        stx_blksize = p.getI32();
        p = p.add(4);
        stx_attributes = p.getI64();
        p = p.add(8);
        stx_nlink = p.getI32();
        p = p.add(4);
        stx_uid = p.getI32();
        p = p.add(4);
        stx_gid = p.getI32();
        p = p.add(4);
        stx_mode = p.getI16();
        p = p.add(4);
        stx_ino = p.getI64();
        p = p.add(8);
        stx_size = p.getI64();
        p = p.add(8);
        stx_blocks = p.getI64();
        p = p.add(8);
        stx_attributes_mask = p.getI64();
        p = p.add(8);
        p = stx_atime.read(p);
        p = stx_btime.read(p);
        p = stx_ctime.read(p);
        p = stx_mtime.read(p);
        stx_rdev_major = p.getI32();
        p = p.add(4);
        stx_rdev_minor = p.getI32();
        p = p.add(4);
        stx_dev_major = p.getI32();
        p = p.add(4);
        stx_dev_minor = p.getI32();
        return ptr.add(0x100);
    }

    public PosixPointer write(PosixPointer ptr) {
        PosixPointer p = ptr;
        p.setI32(stx_mask);
        p = p.add(4);
        p.setI32(stx_blksize);
        p = p.add(4);
        p.setI64(stx_attributes);
        p = p.add(8);
        p.setI32(stx_nlink);
        p = p.add(4);
        p.setI32(stx_uid);
        p = p.add(4);
        p.setI32(stx_gid);
        p = p.add(4);
        p.setI16(stx_mode);
        p = p.add(4);
        p.setI64(stx_ino);
        p = p.add(8);
        p.setI64(stx_size);
        p = p.add(8);
        p.setI64(stx_blocks);
        p = p.add(8);
        p.setI64(stx_attributes_mask);
        p = p.add(8);
        p = stx_atime.write(p);
        p = stx_btime.write(p);
        p = stx_ctime.write(p);
        p = stx_mtime.write(p);
        p.setI32(stx_rdev_major);
        p = p.add(4);
        p.setI32(stx_rdev_minor);
        p = p.add(4);
        p.setI32(stx_dev_major);
        p = p.add(4);
        p.setI32(stx_dev_minor);
        return ptr.add(0x100);
    }
}
