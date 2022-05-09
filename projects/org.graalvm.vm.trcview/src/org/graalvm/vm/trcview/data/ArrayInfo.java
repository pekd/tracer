package org.graalvm.vm.trcview.data;

public class ArrayInfo {
    public final int elementSize;

    public ArrayInfo(int elementSize) {
        this.elementSize = elementSize;
    }

    public int getElementSize() {
        return elementSize;
    }

    @Override
    public String toString() {
        return "Array[elementSize=" + elementSize + "]";
    }
}
