package org.graalvm.vm.trcview.data;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class ArrayStructRecovery {
    public static ArrayInfo recoverArray(Semantics semantics, long pc) {
        long[] reads = semantics.getDataReads(pc);
        long[] writes = semantics.getDataWrites(pc);

        Arrays.sort(reads);
        Arrays.sort(writes);

        int readDeltaCnt = Math.max(reads.length - 1, 0);
        int writeDeltaCnt = Math.max(writes.length - 1, 0);

        long[] deltas = new long[readDeltaCnt + writeDeltaCnt];

        if (deltas.length == 0) {
            return null;
        }

        for (int i = 0; i < readDeltaCnt; i++) {
            deltas[i] = reads[i + 1] - reads[i];
        }

        for (int i = 0; i < writeDeltaCnt; i++) {
            deltas[i + readDeltaCnt] = writes[i + 1] - writes[i];
        }

        Histogram hist = new Histogram(deltas);
        int cnt = hist.size();
        int[] counts = new int[cnt];
        for (int i = 0; i < counts.length; i++) {
            counts[i] = hist.getCount(i);
        }
        Arrays.sort(counts);

        int top = 1;
        int max = counts[counts.length - 1];
        for (int i = counts.length - 2; i >= 0; i--) {
            if (counts[i] == max) {
                top++;
            }
        }

        // at least 6 memory locations accessed and half of them had the same stride
        if (max > deltas.length / 2 && deltas.length > 5) {
            assert top == 1;
            for (int i = 0; i < cnt; i++) {
                if (hist.getCount(i) == max) {
                    return new ArrayInfo((int) hist.getValue(i));
                }
            }

            System.out.println("top: " + top);
            System.out.println(LongStream.of(deltas).mapToObj(Long::toUnsignedString).collect(Collectors.joining(" ")));
        }

        return null;
    }
}
