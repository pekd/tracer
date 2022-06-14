package org.graalvm.vm.trcview.data;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class ArrayStructRecovery {
    public static ArrayInfo recoverArray(Semantics semantics, long pc) {
        return recoverArray(semantics, pc, false);
    }

    public static ArrayInfo recoverArray(Semantics semantics, long pc, boolean getAddresses) {
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

        // add read deltas
        for (int i = 0; i < readDeltaCnt; i++) {
            deltas[i] = reads[i + 1] - reads[i];
        }

        // add write deltas
        for (int i = 0; i < writeDeltaCnt; i++) {
            deltas[i + readDeltaCnt] = writes[i + 1] - writes[i];
        }

        // compute histogram of delta values
        Histogram hist = new Histogram(deltas);
        int cnt = hist.size();
        int[] counts = new int[cnt];
        for (int i = 0; i < counts.length; i++) {
            counts[i] = hist.getCount(i);
        }
        Arrays.sort(counts);

        // compute the delta with the most occurrences
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
                    if (getAddresses) {
                        int n = 0;
                        long d = hist.getValue(i);
                        long[] addresses = new long[deltas.length + 2];
                        for (int j = 0; j < readDeltaCnt; j++) {
                            long delta = reads[j + 1] - reads[j];
                            if (delta == d || delta == 2 * d) {
                                if (n > 0 && addresses[n - 1] != reads[j]) {
                                    addresses[n++] = reads[j];
                                } else if (n == 0) {
                                    addresses[n++] = reads[j];
                                }
                                addresses[n++] = reads[j + 1];
                            }
                        }
                        for (int j = 0; j < writeDeltaCnt; j++) {
                            long delta = writes[j + 1] - writes[j];
                            if (delta == d || delta == 2 * d) {
                                if (n > 0 && addresses[n - 1] != writes[j]) {
                                    addresses[n++] = writes[j];
                                } else if (n == 0) {
                                    addresses[n++] = writes[j];
                                }
                                addresses[n++] = writes[j + 1];
                            }
                        }
                        addresses = Arrays.copyOf(addresses, n);
                        Arrays.sort(addresses);
                        return new ArrayInfo((int) d, addresses);
                    }
                    return new ArrayInfo((int) hist.getValue(i));
                }
            }

            System.out.println("top: " + top);
            System.out.println(LongStream.of(deltas).mapToObj(Long::toUnsignedString).collect(Collectors.joining(" ")));
        }

        return null;
    }
}
