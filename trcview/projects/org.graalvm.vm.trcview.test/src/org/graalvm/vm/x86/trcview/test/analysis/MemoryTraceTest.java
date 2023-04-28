package org.graalvm.vm.x86.trcview.test.analysis;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.graalvm.vm.trcview.analysis.memory.MemorySegment;
import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.analysis.memory.Protection;
import org.junit.Before;
import org.junit.Test;

public class MemoryTraceTest {
    private MemoryTrace mem;

    @Before
    public void setup() {
        mem = new MemoryTrace(false);

        mem.mmap(0xf6eff000L, 0x100000, new Protection(true, true, false), "[stack]", 0x0, 0x0, null, null);
        mem.mmap(0x40000000L, 0x1000, new Protection(true, false, false), "/bin/ls", 0x0, 0x0, null, null);
        mem.mmap(0x40000000L, 0x3b000, new Protection(true, false, true), "/bin/ls", 0x0, 0x0, null, null);
        mem.mmap(0x4004f000L, 0x3000, new Protection(true, true, true), "/bin/ls", 0x0, 0x0, null, null);
        mem.mmap(0xf8000000L, 0x28000, new Protection(true, false, true), "/lib/ld.so.1", 0x0, 0x0, null, null);
        mem.mmap(0xf803f5e0L, 0x3000, new Protection(true, true, true), "/lib/ld.so.1", 0x0, 0x0, null, null);
        mem.mmap(0x80000000L, 0x375cf, new Protection(true, false, false), "/etc/ld.so.cache", 0xf801cfd8, 0xb9e7, null, null);
        mem.mmap(0x80038000L, 0x1d6200, new Protection(true, false, true), "/lib/power8/altivec/libc.so.6", 0xf801cfd8, 0xc894, null, null);
        mem.mmap(0x80206000L, 0x6000, new Protection(true, true, true), "/lib/power8/altivec/libc.so.6", 0xf801cfd8, 0xc8e9, null, null);
        mem.mmap(0x8020c000L, 0x2200, new Protection(true, true, true), "null", 0xf801cfd8, 0xcab4, null, null);
        mem.mmap(0x8020f000L, 0x2000, new Protection(true, true, false), "null", 0xf801cfd8, 0xe917, null, null);
        mem.mmap(0x80211000L, 0x200000, new Protection(true, false, false), "/lib/locale/locale-archive", 0x8013be24, 0x3d209, null, null);
        mem.mmap(0x80000000L, 0x375cf, new Protection(true, false, false), "/etc/ld.so.cache", 0xf801cfd8, 0x6093c, null, null);
        mem.mmap(0x80211000L, 0x265d0, new Protection(true, false, true), "/lib/power8/altivec/libnss_files.so.2", 0xf801cfd8, 0x6160c, null, null);
        mem.mmap(0x80230000L, 0x2000, new Protection(true, true, true), "/lib/power8/altivec/libnss_files.so.2", 0xf801cfd8, 0x61672, null, null);
        mem.mmap(0x80232000L, 0x55d0, new Protection(true, true, true), "null", 0xf801cfd8, 0x61b8f, null, null);
        mem.mmap(0x80000000L, 0x375cf, new Protection(true, false, false), "/etc/ld.so.cache", 0xf801cfd8, 0x82b48, null, null);
    }

    @Test
    public void segmentTest001() {
        List<MemorySegment> segments = mem.getRegions(0);
        // @formatter:off
        String ref = "40000000-40000fff R-- /bin/ls\n"
                        + "40001000-4003afff R-X /bin/ls\n"
                        + "4004f000-40051fff RWX /bin/ls\n"
                        + "f6eff000-f6ffefff RW- [stack]\n"
                        + "f8000000-f8027fff R-X /lib/ld.so.1\n"
                        + "f803f000-f8042fff RWX /lib/ld.so.1";
        // @formatter:on
        String act = segments.stream().map(Object::toString).collect(Collectors.joining("\n"));
        assertEquals(ref, act);
    }

    @Test
    public void segmentTest002() {
        List<MemorySegment> segments = mem.getRegions(0xb9e7);
        // @formatter:off
        String ref = "40000000-40000fff R-- /bin/ls\n"
                        + "40001000-4003afff R-X /bin/ls\n"
                        + "4004f000-40051fff RWX /bin/ls\n"
                        + "80000000-80037fff R-- /etc/ld.so.cache\n"
                        + "f6eff000-f6ffefff RW- [stack]\n"
                        + "f8000000-f8027fff R-X /lib/ld.so.1\n"
                        + "f803f000-f8042fff RWX /lib/ld.so.1";
        // @formatter:on
        String act = segments.stream().map(Object::toString).collect(Collectors.joining("\n"));
        assertEquals(ref, act);
    }
}
