package org.graalvm.vm.trcview.data;

import java.util.Arrays;

public class Histogram {
    private final long[] values;
    private final int[] counts;

    public Histogram(long[] data) {
        if (data.length == 0) {
            values = new long[0];
            counts = new int[0];
            return;
        }

        Arrays.sort(data);
        int distinct = 1;
        long last = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] != last) {
                distinct++;
                last = data[i];
            }
        }

        values = new long[distinct];
        counts = new int[distinct];

        values[0] = data[0];
        last = values[0];
        int count = 1;
        int idx = 0;
        for (int i = 1; i < data.length; i++) {
            if (data[i] != last) {
                values[idx] = last;
                counts[idx] = count;
                idx++;
                last = data[i];
                count = 1;
            } else {
                count++;
            }
        }

        values[idx] = last;
        counts[idx] = count;
    }

    public int size() {
        return values.length;
    }

    public long getValue(int index) {
        return values[index];
    }

    public int getCount(int index) {
        return counts[index];
    }

    public int get(long value) {
        int idx = Arrays.binarySearch(values, value);
        if (idx >= 0) {
            return counts[idx];
        } else {
            return 0;
        }
    }
}
