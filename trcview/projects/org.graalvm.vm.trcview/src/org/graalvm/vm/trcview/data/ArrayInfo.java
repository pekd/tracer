package org.graalvm.vm.trcview.data;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class ArrayInfo {
    public final int elementSize;
    private final long[] addresses;

    public ArrayInfo(int elementSize) {
        this(elementSize, new long[0]);
    }

    public ArrayInfo(int elementSize, long[] addresses) {
        this.elementSize = elementSize;
        this.addresses = addresses;
    }

    public int getElementSize() {
        return elementSize;
    }

    public long[] getAddresses() {
        return addresses;
    }

    @Override
    public String toString() {
        if (addresses.length > 0) {
            String addrs = LongStream.of(addresses).sorted().mapToObj(x -> Long.toUnsignedString(x, 16)).collect(Collectors.joining(","));
            return "Array[elementSize=" + elementSize + ";addrs=" + addrs + "]";
        } else {
            return "Array[elementSize=" + elementSize + "]";
        }
    }
}
