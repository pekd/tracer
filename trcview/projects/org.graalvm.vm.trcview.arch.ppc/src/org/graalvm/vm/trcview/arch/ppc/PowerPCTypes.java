package org.graalvm.vm.trcview.arch.ppc;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.NameAlreadyUsedException;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.analysis.type.UserDefinedType;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;

public class PowerPCTypes {
    private static void add(UserTypeDatabase db, UserDefinedType type) {
        try {
            db.add(type);
        } catch (NameAlreadyUsedException e) {
            // swallow
        }
    }

    private static void timespec(UserTypeDatabase db) {
        Struct statx_timestamp = new Struct("timespec");
        statx_timestamp.add("tv_sec", new Type(DataType.S64));
        statx_timestamp.add("tv_nsec", new Type(DataType.U32));
        statx_timestamp.add("pad", new Type(DataType.U32));
        add(db, statx_timestamp);
    }

    private static void stat(UserTypeDatabase db) {
        Struct stat = new Struct("stat");
        stat.add("st_dev", new Type(DataType.U32));
        stat.add("st_ino", new Type(DataType.U32));
        stat.add("st_mode", new Type(DataType.U32, Representation.OCT));
        stat.add("st_nlink", new Type(DataType.U32));
        stat.add("st_uid", new Type(DataType.U32));
        stat.add("st_gid", new Type(DataType.U32));
        stat.add("st_rdev", new Type(DataType.U32));
        stat.add("__pad1", new Type(DataType.U32));
        stat.add("st_size", new Type(DataType.U32));
        stat.add("st_blksize", new Type(DataType.U32));
        stat.add("__pad2", new Type(DataType.U32));
        stat.add("st_blocks", new Type(DataType.U32));
        stat.add("st_atim", new Type((Struct) db.get("timespec")));
        stat.add("st_mtim", new Type((Struct) db.get("timespec")));
        stat.add("st_ctim", new Type((Struct) db.get("timespec")));
        stat.add("__pad3", new Type(DataType.U32));
        stat.add("__pad4", new Type(DataType.U32));
        add(db, stat);
    }

    private static void stat64(UserTypeDatabase db) {
        Struct stat64 = new Struct("stat64");
        stat64.add("st_dev", new Type(DataType.U64));
        stat64.add("st_ino", new Type(DataType.U64));
        stat64.add("st_mode", new Type(DataType.U32, Representation.OCT));
        stat64.add("st_nlink", new Type(DataType.U32));
        stat64.add("st_uid", new Type(DataType.U32));
        stat64.add("st_gid", new Type(DataType.U32));
        stat64.add("st_rdev", new Type(DataType.U64));
        stat64.add("__pad1", new Type(DataType.U64));
        stat64.add("st_size", new Type(DataType.U64));
        stat64.add("st_blksize", new Type(DataType.U32));
        stat64.add("__pad2", new Type(DataType.U32));
        stat64.add("st_blocks", new Type(DataType.U64));
        stat64.add("st_atim", new Type((Struct) db.get("timespec")));
        stat64.add("st_mtim", new Type((Struct) db.get("timespec")));
        stat64.add("st_ctim", new Type((Struct) db.get("timespec")));
        stat64.add("__pad3", new Type(DataType.U32));
        stat64.add("__pad4", new Type(DataType.U32));
        add(db, stat64);
    }

    private static void statx_timestamp(UserTypeDatabase db) {
        Struct statx_timestamp = new Struct("statx_timestamp");
        statx_timestamp.add("tv_sec", new Type(DataType.S64));
        statx_timestamp.add("tv_nsec", new Type(DataType.U32));
        statx_timestamp.add("pad", new Type(DataType.U32));
        add(db, statx_timestamp);
    }

    private static void statx(UserTypeDatabase db) {
        Struct statx = new Struct("statx");
        statx.add("stx_mask", new Type(DataType.U32, Representation.HEX));
        statx.add("stx_blksize", new Type(DataType.U32));
        statx.add("stx_attributes", new Type(DataType.U64, Representation.HEX));
        statx.add("stx_nlink", new Type(DataType.U32));
        statx.add("stx_uid", new Type(DataType.U32));
        statx.add("stx_gid", new Type(DataType.U32));
        statx.add("stx_mode", new Type(DataType.U16, Representation.OCT));
        statx.add("pad", new Type(DataType.U16));
        statx.add("stx_ino", new Type(DataType.U64));
        statx.add("stx_size", new Type(DataType.U64));
        statx.add("stx_blocks", new Type(DataType.U64));
        statx.add("stx_attributes_mask", new Type(DataType.U64, Representation.HEX));
        statx.add("stx_atime", new Type((Struct) db.get("statx_timestamp")));
        statx.add("stx_btime", new Type((Struct) db.get("statx_timestamp")));
        statx.add("stx_ctime", new Type((Struct) db.get("statx_timestamp")));
        statx.add("stx_mtime", new Type((Struct) db.get("statx_timestamp")));
        statx.add("stx_rdev_major", new Type(DataType.U32));
        statx.add("stx_rdev_minor", new Type(DataType.U32));
        statx.add("stx_dev_major", new Type(DataType.U32));
        statx.add("stx_dev_minor", new Type(DataType.U32));
        statx.add("stx_mnt_id", new Type(DataType.U64));
        statx.add("stx_dio_mem_align", new Type(DataType.U32));
        statx.add("stx_dio_offset_align", new Type(DataType.U32));
        add(db, statx);
    }

    public static void add(UserTypeDatabase db) {
        timespec(db);
        stat(db);
        stat64(db);
        statx_timestamp(db);
        statx(db);
    }
}
