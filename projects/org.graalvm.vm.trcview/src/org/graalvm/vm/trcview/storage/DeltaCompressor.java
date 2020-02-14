package org.graalvm.vm.trcview.storage;

public class DeltaCompressor {
    public static byte[] compress(byte[] last, byte[] current) {
        if (last.length != current.length) {
            return keyframe(current);
        } else {
            int header = current.length / 8 + 1;
            if ((current.length % 8) != 0) {
                header++;
            }
            int diff = 0;
            for (int i = 0; i < current.length; i++) {
                if (last[i] != current[i]) {
                    diff++;
                }
            }
            byte[] result = new byte[header + diff];
            if ((header - 1) > 255) {
                throw new IllegalStateException("State too long");
            }
            result[0] = (byte) (header - 1);
            int wr = header;
            for (int i = 0; i < current.length; i++) {
                int hdr = i / 8;
                int bit = 1 << (i % 8);
                if (last[i] != current[i]) {
                    result[1 + hdr] |= bit;
                    result[wr++] = current[i];
                }
            }
            return result;
        }
    }

    public static byte[] decompress(byte[] last, byte[] compressed) {
        if (compressed[0] == 0) {
            byte[] result = new byte[compressed.length - 1];
            System.arraycopy(compressed, 1, result, 0, result.length);
            return result;
        } else {
            int header = Byte.toUnsignedInt(compressed[0]) + 1;
            byte[] result = new byte[last.length];
            int rd = header;
            for (int i = 0; i < last.length; i++) {
                int hdr = i / 8;
                int bit = 1 << (i % 8);
                if ((compressed[1 + hdr] & bit) != 0) {
                    result[i] = compressed[rd++];
                } else {
                    result[i] = last[i];
                }
            }
            return result;
        }
    }

    public static byte[] keyframe(byte[] data) {
        byte[] result = new byte[data.length + 1];
        result[0] = 0;
        System.arraycopy(data, 0, result, 1, data.length);
        return result;
    }

    public static boolean isKeyframe(byte[] compressed) {
        return compressed[0] == 0;
    }
}
