package org.graalvm.vm.posix.api.io;

import java.util.Date;

import org.graalvm.vm.posix.api.PosixPointer;
import org.graalvm.vm.posix.api.Struct;

public class StatxTimestamp implements Struct {
    public long tv_sec;
    public int tv_nsec;

    public StatxTimestamp() {
    }

    public StatxTimestamp(long tv_sec, int tv_nsec) {
        this.tv_sec = tv_sec;
        this.tv_nsec = tv_nsec;
    }

    public StatxTimestamp(Date date) {
        long msec = date.getTime();
        tv_sec = msec / 1000;
        tv_nsec = (int) ((msec % 1000) * 1000000);
    }

    public StatxTimestamp(StatxTimestamp ts) {
        copyFrom(ts);
    }

    public long toMillis() {
        return tv_sec * 1000 + (tv_nsec / 1000000);
    }

    public void copyFrom(StatxTimestamp ts) {
        tv_sec = ts.tv_sec;
        tv_nsec = ts.tv_nsec;
    }

    @Override
    public PosixPointer read(PosixPointer ptr) {
        PosixPointer p = ptr;
        tv_sec = p.getI64();
        p = p.add(8);
        tv_nsec = p.getI32();
        return p.add(8);
    }

    @Override
    public PosixPointer write(PosixPointer ptr) {
        PosixPointer p = ptr;
        p.setI64(tv_sec);
        p = p.add(8);
        p.setI32(tv_nsec);
        return p.add(8);
    }

    @Override
    public String toString() {
        return String.format("{tv_sec=%d,tv_nsec=%d}", tv_sec, tv_nsec);
    }
}
