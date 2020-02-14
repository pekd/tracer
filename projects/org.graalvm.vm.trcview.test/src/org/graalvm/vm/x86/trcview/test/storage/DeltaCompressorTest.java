package org.graalvm.vm.x86.trcview.test.storage;

import static org.junit.Assert.assertArrayEquals;

import org.graalvm.vm.trcview.storage.DeltaCompressor;
import org.junit.Test;

public class DeltaCompressorTest {
    @Test
    public void encodeLonger() {
        byte[] last = {46, 4, -3, 53};
        byte[] curr = {1, 2, 3, 4, 5};
        byte[] ref = {0, 1, 2, 3, 4, 5};
        byte[] act = DeltaCompressor.compress(last, curr);
        assertArrayEquals(ref, act);
    }

    @Test
    public void encodeDiff() {
        byte[] last = {46, 4, -3, 53};
        byte[] current = {46, 2, -3, 54};
        byte[] ref = {1, 0b00001010, 2, 54};
        byte[] act = DeltaCompressor.compress(last, current);
        assertArrayEquals(ref, act);
    }

    @Test
    public void encodeLongDiff() {
        byte[] last = {46, 4, -3, 53, 45, 97, 22, -98, -52, 42, 30, 127};
        byte[] curr = {46, 4, -4, 53, 45, 97, 22, -98, -52, 42, 31, 127};
        byte[] ref = {2, 0b00000100, 0b00000100, -4, 31};
        byte[] act = DeltaCompressor.compress(last, curr);
        assertArrayEquals(ref, act);
    }

    @Test
    public void decodeLonger() {
        byte[] last = {46, 4, -3, 53};
        byte[] comp = {0, 1, 2, 3, 4, 5};
        byte[] ref = {1, 2, 3, 4, 5};
        byte[] act = DeltaCompressor.decompress(last, comp);
        assertArrayEquals(ref, act);
    }

    @Test
    public void decodeDiff() {
        byte[] last = {46, 4, -3, 53};
        byte[] comp = {1, 0b00001010, 2, 54};
        byte[] ref = {46, 2, -3, 54};
        byte[] act = DeltaCompressor.decompress(last, comp);
        assertArrayEquals(ref, act);
    }

    @Test
    public void decodeLongDiff() {
        byte[] last = {46, 4, -3, 53, 45, 97, 22, -98, -52, 42, 30, 127};
        byte[] comp = {2, 0b00000100, 0b00000100, -4, 31};
        byte[] ref = {46, 4, -4, 53, 45, 97, 22, -98, -52, 42, 31, 127};
        byte[] act = DeltaCompressor.decompress(last, comp);
        assertArrayEquals(ref, act);
    }
}
