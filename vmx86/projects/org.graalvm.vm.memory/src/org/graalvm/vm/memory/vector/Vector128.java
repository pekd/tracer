/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.vm.memory.vector;

import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.io.Endianess;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.ValueType;
import com.oracle.truffle.api.nodes.ExplodeLoop;

@ValueType
public class Vector128 extends org.graalvm.vm.util.Vector128 implements Cloneable {
    private static final int SIZE = 2;

    public static final Vector128 ZERO = new Vector128();

    public Vector128() {
        this(0, 0);
    }

    public Vector128(long[] data) {
        assert data.length == 2;
        this.data0 = data[0];
        this.data1 = data[1];
    }

    public Vector128(long high, long low) {
        this.data0 = high;
        this.data1 = low;
    }

    public Vector128(int a1, int a2, int a3, int a4) {
        this(Integer.toUnsignedLong(a1) << 32 | Integer.toUnsignedLong(a2), Integer.toUnsignedLong(a3) << 32 | Integer.toUnsignedLong(a4));
    }

    public Vector128(byte[] data) {
        this(Endianess.get64bitBE(data, 0), Endianess.get64bitBE(data, 8));
        assert data.length == 16;
    }

    public Vector128(short[] data) {
        this(Short.toUnsignedLong(data[0]) << 48 | Short.toUnsignedLong(data[1]) << 32 | Short.toUnsignedLong(data[2]) << 16 | Short.toUnsignedLong(data[3]),
                        Short.toUnsignedLong(data[4]) << 48 | Short.toUnsignedLong(data[5]) << 32 | Short.toUnsignedLong(data[6]) << 16 | Short.toUnsignedLong(data[7]));
        assert data.length == 8;
    }

    public Vector128(int[] data) {
        this(data[0], data[1], data[2], data[3]);
        assert data.length == 4;
    }

    public Vector128(float a1, float a2, float a3, float a4) {
        this(Float.floatToRawIntBits(a1), Float.floatToRawIntBits(a2), Float.floatToRawIntBits(a3), Float.floatToRawIntBits(a4));
    }

    public Vector128(float[] data) {
        this(Float.floatToRawIntBits(data[0]), Float.floatToRawIntBits(data[1]), Float.floatToRawIntBits(data[2]), Float.floatToRawIntBits(data[3]));
        assert data.length == 4;
    }

    public Vector128(double high, double low) {
        this(Double.doubleToRawLongBits(high), Double.doubleToRawLongBits(low));
    }

    public Vector128(double[] data) {
        this(Double.doubleToRawLongBits(data[0]), Double.doubleToRawLongBits(data[1]));
        assert data.length == 2;
    }

    public byte[] getBytes() {
        byte[] result = new byte[16];
        Endianess.set64bitBE(result, 0, data0);
        Endianess.set64bitBE(result, 8, data1);
        return result;
    }

    public short[] getShorts() {
        short[] result = new short[8];
        for (int i = 0; i < result.length; i++) {
            result[i] = getI16(i);
        }
        return result;
    }

    public int[] getInts() {
        int[] result = new int[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = getI32(i);
        }
        return result;
    }

    @ExplodeLoop
    public float[] getFloats() {
        float[] result = new float[4];
        for (int i = 0; i < 4; i++) {
            result[i] = getF32(i);
        }
        return result;
    }

    @ExplodeLoop
    public double[] getDoubles() {
        double[] result = new double[2];
        for (int i = 0; i < 2; i++) {
            result[i] = getF64(i);
        }
        return result;
    }

    public double getF64(int i) {
        assert i >= 0 && i < 2;
        return Double.longBitsToDouble(getI64(i));
    }

    public void setF64(int i, double val) {
        assert i >= 0 && i < 2;
        setI64(i, Double.doubleToRawLongBits(val));
    }

    @Override
    public long getI64(int i) {
        assert i >= 0 && i < 2;
        switch (i) {
            case 0:
                return data0;
            case 1:
                return data1;
            default:
                throw new ArrayIndexOutOfBoundsException(i);
        }
    }

    @Override
    public void setI64(int i, long val) {
        assert i >= 0 && i < 2;
        switch (i) {
            case 0:
                data0 = val;
                break;
            case 1:
                data1 = val;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(i);
        }
    }

    public void setI128(Vector128 vec) {
        data0 = vec.data0;
        data1 = vec.data1;
    }

    public int getI32(int i) {
        assert i >= 0 && i < 4;
        long val = getI64(i / 2);
        if ((i & 1) == 0) {
            return (int) (val >>> 32);
        } else {
            return (int) val;
        }
    }

    public void setI32(int i, int val) {
        assert i >= 0 && i < 4;
        long old = getI64(i / 2);
        long mask;
        int shift;
        if ((i & 1) == 0) {
            mask = 0x00000000FFFFFFFFL;
            shift = 32;
        } else {
            mask = 0xFFFFFFFF00000000L;
            shift = 0;
        }
        long result = (old & mask) | (Integer.toUnsignedLong(val) << shift);
        setI64(i / 2, result);
    }

    public float getF32(int i) {
        assert i >= 0 && i < 4;
        return Float.intBitsToFloat(getI32(i));
    }

    public void setF32(int i, float val) {
        assert i >= 0 && i < 4;
        setI32(i, Float.floatToRawIntBits(val));
    }

    public short getI16(int i) {
        assert i >= 0 && i < 8;
        long val = getI64(i / 4);
        int shift = (3 - (i & 3)) << 4;
        return (short) (val >>> shift);
    }

    public void setI16(int i, short val) {
        assert i >= 0 && i < 8;
        long old = getI64(i / 4);
        int shift = (3 - (i & 3)) << 4;
        long mask = ~(0xFFFFL << shift);
        long result = (old & mask) | ((Short.toUnsignedLong(val) & 0xFFFFL) << shift);
        setI64(i / 4, result);
    }

    public byte getI8(int i) {
        assert i >= 0 && i < 16;
        long val = getI64(i / 8);
        int shift = (7 - (i & 7)) << 3;
        return (byte) (val >>> shift);
    }

    @ExplodeLoop
    public Vector128 and(Vector128 x) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = getI64(i) & x.getI64(i);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 or(Vector128 x) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = getI64(i) | x.getI64(i);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 xor(Vector128 x) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = getI64(i) ^ x.getI64(i);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 not() {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = ~getI64(i);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 addFloat(Vector128 x) {
        float[] a = getFloats();
        float[] b = x.getFloats();
        float[] sum = new float[a.length];
        CompilerAsserts.partialEvaluationConstant(sum.length);
        for (int i = 0; i < sum.length; i++) {
            sum[i] = a[i] + b[i];
        }
        return new Vector128(sum);
    }

    @ExplodeLoop
    public Vector128 subFloat(Vector128 x) {
        float[] a = getFloats();
        float[] b = x.getFloats();
        float[] diff = new float[a.length];
        CompilerAsserts.partialEvaluationConstant(diff.length);
        for (int i = 0; i < diff.length; i++) {
            diff[i] = a[i] - b[i];
        }
        return new Vector128(diff);
    }

    @ExplodeLoop
    public Vector128 mulFloat(Vector128 x) {
        float[] a = getFloats();
        float[] b = x.getFloats();
        float[] prod = new float[a.length];
        CompilerAsserts.partialEvaluationConstant(prod.length);
        for (int i = 0; i < prod.length; i++) {
            prod[i] = a[i] * b[i];
        }
        return new Vector128(prod);
    }

    @ExplodeLoop
    public Vector128 divFloat(Vector128 x) {
        float[] a = getFloats();
        float[] b = x.getFloats();
        float[] quot = new float[a.length];
        CompilerAsserts.partialEvaluationConstant(quot.length);
        for (int i = 0; i < quot.length; i++) {
            quot[i] = a[i] / b[i];
        }
        return new Vector128(quot);
    }

    @ExplodeLoop
    public Vector128 rcpFloat() {
        float[] a = getFloats();
        float[] rcp = new float[a.length];
        CompilerAsserts.partialEvaluationConstant(rcp.length);
        for (int i = 0; i < rcp.length; i++) {
            rcp[i] = 1.0f / a[i];
        }
        return new Vector128(rcp);
    }

    @ExplodeLoop
    public Vector128 addDouble(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        double[] sum = new double[a.length];
        CompilerAsserts.partialEvaluationConstant(sum.length);
        for (int i = 0; i < sum.length; i++) {
            sum[i] = a[i] + b[i];
        }
        return new Vector128(sum);
    }

    @ExplodeLoop
    public Vector128 subDouble(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        double[] diff = new double[a.length];
        CompilerAsserts.partialEvaluationConstant(diff.length);
        for (int i = 0; i < diff.length; i++) {
            diff[i] = a[i] - b[i];
        }
        return new Vector128(diff);
    }

    @ExplodeLoop
    public Vector128 mulDouble(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        double[] prod = new double[a.length];
        CompilerAsserts.partialEvaluationConstant(prod.length);
        for (int i = 0; i < prod.length; i++) {
            prod[i] = a[i] * b[i];
        }
        return new Vector128(prod);
    }

    @ExplodeLoop
    public Vector128 divDouble(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        double[] quot = new double[a.length];
        CompilerAsserts.partialEvaluationConstant(quot.length);
        for (int i = 0; i < quot.length; i++) {
            quot[i] = a[i] / b[i];
        }
        return new Vector128(quot);
    }

    private static long eq(long x, long y, long mask) {
        if ((x & mask) == (y & mask)) {
            return mask;
        } else {
            return 0;
        }
    }

    private static long gt8(long x, long y, int b) {
        int shamt = 8 * b;
        if (((byte) (x >> shamt)) > ((byte) (y >> shamt))) {
            return 0xFFL << shamt;
        } else {
            return 0;
        }
    }

    private static long gt16(long x, long y, int b) {
        int shamt = 16 * b;
        if (((short) (x >> shamt)) > ((short) (y >> shamt))) {
            return 0xFFFFL << shamt;
        } else {
            return 0;
        }
    }

    private static long gt32(long x, long y, int b) {
        int shamt = 32 * b;
        if (((int) (x >> shamt)) > ((int) (y >> shamt))) {
            return 0xFFFFFFFFL << shamt;
        } else {
            return 0;
        }
    }

    @ExplodeLoop
    public Vector128 eq8(Vector128 x) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            long r = 0;
            r |= eq(getI64(i), x.getI64(i), 0xFF00000000000000L);
            r |= eq(getI64(i), x.getI64(i), 0x00FF000000000000L);
            r |= eq(getI64(i), x.getI64(i), 0x0000FF0000000000L);
            r |= eq(getI64(i), x.getI64(i), 0x000000FF00000000L);
            r |= eq(getI64(i), x.getI64(i), 0x00000000FF000000L);
            r |= eq(getI64(i), x.getI64(i), 0x0000000000FF0000L);
            r |= eq(getI64(i), x.getI64(i), 0x000000000000FF00L);
            r |= eq(getI64(i), x.getI64(i), 0x00000000000000FFL);
            result[i] = r;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 eq16(Vector128 x) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            long r = 0;
            r |= eq(getI64(i), x.getI64(i), 0xFFFF000000000000L);
            r |= eq(getI64(i), x.getI64(i), 0x0000FFFF00000000L);
            r |= eq(getI64(i), x.getI64(i), 0x00000000FFFF0000L);
            r |= eq(getI64(i), x.getI64(i), 0x000000000000FFFFL);
            result[i] = r;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 eq32(Vector128 x) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            long r = 0;
            r |= eq(getI64(i), x.getI64(i), 0xFFFFFFFF00000000L);
            r |= eq(getI64(i), x.getI64(i), 0x00000000FFFFFFFFL);
            result[i] = r;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 gt8(Vector128 x) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            long r = 0;
            r |= gt8(getI64(i), x.getI64(i), 7);
            r |= gt8(getI64(i), x.getI64(i), 6);
            r |= gt8(getI64(i), x.getI64(i), 5);
            r |= gt8(getI64(i), x.getI64(i), 4);
            r |= gt8(getI64(i), x.getI64(i), 3);
            r |= gt8(getI64(i), x.getI64(i), 2);
            r |= gt8(getI64(i), x.getI64(i), 1);
            r |= gt8(getI64(i), x.getI64(i), 0);
            result[i] = r;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 gt16(Vector128 x) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            long r = 0;
            r |= gt16(getI64(i), x.getI64(i), 3);
            r |= gt16(getI64(i), x.getI64(i), 2);
            r |= gt16(getI64(i), x.getI64(i), 1);
            r |= gt16(getI64(i), x.getI64(i), 0);
            result[i] = r;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 gt32(Vector128 x) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            long r = 0;
            r |= gt32(getI64(i), x.getI64(i), 1);
            r |= gt32(getI64(i), x.getI64(i), 0);
            result[i] = r;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 leF32(Vector128 x) {
        float[] a = getFloats();
        float[] b = x.getFloats();
        int[] result = new int[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = a[i] <= b[i];
            if (val) {
                result[i] = 0xFFFFFFFF;
            } else {
                result[i] = 0x00000000;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 ltF32(Vector128 x) {
        float[] a = getFloats();
        float[] b = x.getFloats();
        int[] result = new int[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = a[i] < b[i];
            if (val) {
                result[i] = 0xFFFFFFFF;
            } else {
                result[i] = 0x00000000;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 geF32(Vector128 x) {
        float[] a = getFloats();
        float[] b = x.getFloats();
        int[] result = new int[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = a[i] >= b[i];
            if (val) {
                result[i] = 0xFFFFFFFF;
            } else {
                result[i] = 0x00000000;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 gtF32(Vector128 x) {
        float[] a = getFloats();
        float[] b = x.getFloats();
        int[] result = new int[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = a[i] > b[i];
            if (val) {
                result[i] = 0xFFFFFFFF;
            } else {
                result[i] = 0x00000000;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 eqF64(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        long[] result = new long[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = a[i] == b[i];
            if (val) {
                result[i] = 0xFFFFFFFFFFFFFFFFL;
            } else {
                result[i] = 0x0000000000000000L;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 leF64(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        long[] result = new long[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = a[i] <= b[i];
            if (val) {
                result[i] = 0xFFFFFFFFFFFFFFFFL;
            } else {
                result[i] = 0x0000000000000000L;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 ltF64(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        long[] result = new long[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = a[i] < b[i];
            if (val) {
                result[i] = 0xFFFFFFFFFFFFFFFFL;
            } else {
                result[i] = 0x0000000000000000L;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 geF64(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        long[] result = new long[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = a[i] >= b[i] || Double.isNaN(a[i]) || Double.isNaN(b[i]);
            if (val) {
                result[i] = 0xFFFFFFFFFFFFFFFFL;
            } else {
                result[i] = 0x0000000000000000L;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 gtF64(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        long[] result = new long[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = a[i] > b[i] || Double.isNaN(a[i]) || Double.isNaN(b[i]);
            if (val) {
                result[i] = 0xFFFFFFFFFFFFFFFFL;
            } else {
                result[i] = 0x0000000000000000L;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 orderedF64(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        long[] result = new long[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = !Double.isNaN(a[i]) && !Double.isNaN(b[i]);
            if (val) {
                result[i] = 0xFFFFFFFFFFFFFFFFL;
            } else {
                result[i] = 0x0000000000000000L;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 unorderedF64(Vector128 x) {
        double[] a = getDoubles();
        double[] b = x.getDoubles();
        long[] result = new long[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            boolean val = Double.isNaN(a[i]) || Double.isNaN(b[i]);
            if (val) {
                result[i] = 0xFFFFFFFFFFFFFFFFL;
            } else {
                result[i] = 0x0000000000000000L;
            }
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public int signsF64() {
        int result = 0;
        for (int i = 0; i < SIZE; i++) {
            if (getI64(i) < 0) {
                result |= (1 << i);
            }
        }
        return result;
    }

    @ExplodeLoop
    public int signsF64le() {
        int result = 0;
        for (int i = 0; i < SIZE; i++) {
            if (getI64(1 - i) < 0) {
                result |= (1 << i);
            }
        }
        return result;
    }

    @ExplodeLoop
    public long byteMaskMSB() {
        long result = 0;
        long o = 1L << (SIZE * 8 - 1);
        for (int i = 0; i < SIZE; i++) {
            assert o != 0;
            long val = getI64(i);
            long mask = 0x8000000000000000L;
            for (int n = 0; n < 8; n++) {
                if (BitTest.test(val, mask)) {
                    result |= o;
                }
                o >>>= 1;
                mask >>>= 8;
            }
            assert mask == 0;
        }
        assert o == 0;
        return result;
    }

    @ExplodeLoop
    public Vector128 shl(int n) {
        assert n > 0 && n < 128;
        if (n < 64) {
            long overflow = 0;
            long overflowShift = 64 - n;
            long overflowMask = 0;
            for (int i = 0, bit = 0; i < 64; i++, bit <<= 1) {
                if (i < n) {
                    overflowMask |= bit;
                }
            }
            long[] result = new long[SIZE];
            CompilerAsserts.partialEvaluationConstant(result.length);
            for (int i = 0; i < result.length; i++) {
                result[i] = overflow | (getI64(i) << n);
                overflow = (getI64(i) >> overflowShift) & overflowMask;
            }
            return new Vector128(result);
        } else {
            throw new AssertionError("not yet implemented");
        }
    }

    @ExplodeLoop
    public Vector128 shrBytes(int n) {
        assert n > 0 && n < 16;
        byte[] bytes = getBytes();
        byte[] shifted = new byte[bytes.length];
        for (int i = 0; i < 16; i++) {
            int src = i - n;
            if (src < 0) {
                shifted[i] = 0;
            } else {
                shifted[i] = bytes[src];
            }
        }
        return new Vector128(shifted);
    }

    @ExplodeLoop
    public Vector128 shrPackedI16(int n) {
        short[] shorts = getShorts();
        short[] result = new short[shorts.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(shorts.length);
        for (int i = 0; i < shorts.length; i++) {
            result[i] = (short) (Short.toUnsignedInt(shorts[i]) >>> n);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 shrPackedI32(int n) {
        int[] ints = getInts();
        int[] result = new int[ints.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(ints.length);
        for (int i = 0; i < ints.length; i++) {
            result[i] = ints[i] >>> n;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 shrPackedI64(int n) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < SIZE; i++) {
            result[i] = getI64(i) >>> n;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 sarPackedI16(int n) {
        short[] shorts = getShorts();
        short[] result = new short[shorts.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(shorts.length);
        for (int i = 0; i < shorts.length; i++) {
            result[i] = (short) (shorts[i] >> n);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 sarPackedI32(int n) {
        int[] ints = getInts();
        int[] result = new int[ints.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(ints.length);
        for (int i = 0; i < ints.length; i++) {
            result[i] = ints[i] >> n;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 sarPackedI64(int n) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < SIZE; i++) {
            result[i] = getI64(i) >> n;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 shlBytes(int n) {
        assert n > 0 && n < 16;
        byte[] bytes = getBytes();
        byte[] shifted = new byte[bytes.length];
        CompilerAsserts.partialEvaluationConstant(bytes.length);
        CompilerAsserts.partialEvaluationConstant(shifted.length);
        for (int i = 0; i < 16; i++) {
            int src = i + n;
            if (src >= bytes.length) {
                shifted[i] = 0;
            } else {
                shifted[i] = bytes[src];
            }
        }
        return new Vector128(shifted);
    }

    @ExplodeLoop
    public Vector128 shlPackedI16(int n) {
        short[] shorts = getShorts();
        short[] result = new short[shorts.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(shorts.length);
        for (int i = 0; i < shorts.length; i++) {
            result[i] = (short) (shorts[i] << n);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 shlPackedI32(int n) {
        int[] ints = getInts();
        int[] result = new int[ints.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(ints.length);
        for (int i = 0; i < ints.length; i++) {
            result[i] = ints[i] << n;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 shlPackedI64(int n) {
        long[] result = new long[SIZE];
        CompilerAsserts.partialEvaluationConstant(result.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = getI64(i) << n;
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 addPackedI8(Vector128 vec) {
        byte[] a = getBytes();
        byte[] b = vec.getBytes();
        byte[] result = new byte[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] + b[i]);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 addPackedI16(Vector128 vec) {
        short[] a = getShorts();
        short[] b = vec.getShorts();
        short[] result = new short[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (short) (a[i] + b[i]);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 addPackedI32(Vector128 vec) {
        int[] a = getInts();
        int[] b = vec.getInts();
        int[] result = new int[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return new Vector128(result);
    }

    public Vector128 addPackedI64(Vector128 vec) {
        long[] result = {data0 + vec.data0, data1 + vec.data1};
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 subPackedI8(Vector128 vec) {
        byte[] a = getBytes();
        byte[] b = vec.getBytes();
        byte[] result = new byte[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] - b[i]);
        }
        return new Vector128(result);
    }

    private static byte saturateU8(int x) {
        if (x < 0) {
            return 0;
        } else if (x > 0xFF) {
            return (byte) 0xFF;
        } else {
            return (byte) x;
        }
    }

    private static short saturateU16(int x) {
        if (x < 0) {
            return 0;
        } else if (x > 0xFF) {
            return (byte) 0xFF;
        } else {
            return (byte) x;
        }
    }

    @ExplodeLoop
    public Vector128 subPackedI8SaturateUnsigned(Vector128 vec) {
        byte[] a = getBytes();
        byte[] b = vec.getBytes();
        byte[] result = new byte[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = saturateU8(Byte.toUnsignedInt(a[i]) - Byte.toUnsignedInt(b[i]));
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 subPackedI16(Vector128 vec) {
        short[] a = getShorts();
        short[] b = vec.getShorts();
        short[] result = new short[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (short) (a[i] - b[i]);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 subPackedI16SaturateUnsigned(Vector128 vec) {
        short[] a = getShorts();
        short[] b = vec.getShorts();
        short[] result = new short[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = saturateU16(Short.toUnsignedInt(a[i]) - Short.toUnsignedInt(b[i]));
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 subPackedI32(Vector128 vec) {
        int[] a = getInts();
        int[] b = vec.getInts();
        int[] result = new int[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return new Vector128(result);
    }

    public Vector128 subPackedI64(Vector128 vec) {
        long[] result = {data0 - vec.data0, data1 - vec.data1};
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 mulPackedI16(Vector128 vec) {
        short[] a = getShorts();
        short[] b = vec.getShorts();
        short[] result = new short[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (short) (a[i] * b[i]);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 mulPackedI32(Vector128 vec) {
        int[] a = getInts();
        int[] b = vec.getInts();
        int[] result = new int[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b[i];
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 mulHighPackedI16(Vector128 vec) {
        short[] a = getShorts();
        short[] b = vec.getShorts();
        short[] result = new short[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (short) ((a[i] * b[i]) >> 16);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 mulUnsignedPackedI16(Vector128 vec) {
        short[] a = getShorts();
        short[] b = vec.getShorts();
        short[] result = new short[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (short) (Short.toUnsignedInt(a[i]) * Short.toUnsignedInt(b[i]));
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 mulHighUnsignedPackedI16(Vector128 vec) {
        short[] a = getShorts();
        short[] b = vec.getShorts();
        short[] result = new short[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (short) ((Short.toUnsignedInt(a[i]) * Short.toUnsignedInt(b[i])) >> 16);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 minUnsignedPackedI8(Vector128 vec) {
        byte[] a = getBytes();
        byte[] b = vec.getBytes();
        byte[] result = new byte[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) Math.min(Byte.toUnsignedInt(a[i]), Byte.toUnsignedInt(b[i]));
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 maxUnsignedPackedI8(Vector128 vec) {
        byte[] a = getBytes();
        byte[] b = vec.getBytes();
        byte[] result = new byte[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) Math.max(Byte.toUnsignedInt(a[i]), Byte.toUnsignedInt(b[i]));
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 minUnsignedPackedI32(Vector128 vec) {
        int[] a = getInts();
        int[] b = vec.getInts();
        int[] result = new int[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (int) Math.min(Integer.toUnsignedLong(a[i]), Integer.toUnsignedLong(b[i]));
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 maxUnsignedPackedI32(Vector128 vec) {
        int[] a = getInts();
        int[] b = vec.getInts();
        int[] result = new int[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = (int) Math.max(Integer.toUnsignedLong(a[i]), Integer.toUnsignedLong(b[i]));
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 minPackedF32(Vector128 vec) {
        float[] a = getFloats();
        float[] b = vec.getFloats();
        float[] result = new float[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = Math.min(a[i], b[i]);
        }
        return new Vector128(result);
    }

    @ExplodeLoop
    public Vector128 maxPackedF32(Vector128 vec) {
        float[] a = getFloats();
        float[] b = vec.getFloats();
        float[] result = new float[a.length];
        CompilerAsserts.partialEvaluationConstant(result.length);
        CompilerAsserts.partialEvaluationConstant(a.length);
        CompilerAsserts.partialEvaluationConstant(b.length);
        for (int i = 0; i < a.length; i++) {
            result[i] = Math.max(a[i], b[i]);
        }
        return new Vector128(result);
    }

    @Override
    public int hashCode() {
        long result = 0;
        for (int i = 0; i < SIZE; i++) {
            result ^= getI64(i);
        }
        return (int) result;
    }

    @Override
    @ExplodeLoop
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Vector128)) {
            return false;
        }
        Vector128 v = (Vector128) o;
        for (int i = 0; i < SIZE; i++) {
            if (getI64(i) != v.getI64(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Vector128 clone() {
        return new Vector128(data0, data1);
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        return String.format("0x%016x%016x", data0, data1);
    }
}
