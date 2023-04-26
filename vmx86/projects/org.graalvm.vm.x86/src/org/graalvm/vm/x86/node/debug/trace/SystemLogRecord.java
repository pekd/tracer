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
package org.graalvm.vm.x86.node.debug.trace;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.graalvm.vm.util.StackTraceUtil;
import org.graalvm.vm.util.StringUtils;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class SystemLogRecord extends Record {
    public static final byte ID = 0x41;

    private long seq;
    private long time;
    private int level;
    private long threadID;
    private String logger;
    private String clazz;
    private String method;
    private String message;
    private String throwable;

    SystemLogRecord() {
        super(ID);
    }

    public SystemLogRecord(long seq, long time, int level, long threadID, String logger, String clazz, String method, String message, Throwable throwable) {
        this();
        this.seq = seq;
        this.time = time;
        this.level = level;
        this.threadID = threadID;
        this.logger = logger;
        this.clazz = clazz;
        this.method = method;
        this.message = message;
        this.throwable = throwable != null ? StackTraceUtil.getStackTrace(throwable) : null;
    }

    @SuppressWarnings("deprecation")
    public LogRecord getLogRecord() {
        @SuppressWarnings("serial")
        Level lvl = new Level("level-" + level, level) {
        };
        LogRecord r = new LogRecord(lvl, clazz);
        r.setSequenceNumber(seq);
        r.setInstant(Instant.ofEpochMilli(time));
        r.setLongThreadID(threadID);
        r.setLoggerName(logger);
        r.setSourceClassName(clazz);
        r.setSourceMethodName(method);
        r.setMessage(message);
        return r;
    }

    public String getThrown() {
        return throwable;
    }

    @Override
    protected int getDataSize() {
        int size = 2 * 8 + 2 * 4 + 5 * 2;
        if (logger != null) {
            size += logger.getBytes().length;
        }
        if (clazz != null) {
            size += clazz.getBytes().length;
        }
        if (method != null) {
            size += method.getBytes().length;
        }
        if (message != null) {
            size += message.getBytes().length;
        }
        if (throwable != null) {
            size += throwable.getBytes().length;
        }
        return size;
    }

    @Override
    protected void readRecord(WordInputStream in) throws IOException {
        seq = in.read64bit();
        time = in.read64bit();
        level = in.read32bit();
        threadID = in.read32bit();
        logger = readString(in);
        clazz = readString(in);
        method = readString(in);
        message = readString(in);
        throwable = readString(in);
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(seq);
        out.write64bit(time);
        out.write32bit(level);
        out.write32bit(threadID);
        writeString(out, logger);
        writeString(out, clazz);
        writeString(out, method);
        writeString(out, message);
        writeString(out, throwable);
    }

    @Override
    public String toString() {
        String src = StringUtils.rpad(clazz + "#" + method, 60);
        if (clazz == null || method == null) {
            if (logger != null) {
                src = StringUtils.rpad("<" + logger + ">", 60);
            } else {
                src = StringUtils.rpad("unknown source", 60);
            }
        }
        char lvl = '?';
        String error = "";
        if (throwable != null) {
            error = "\n" + throwable;
        }
        return String.format("[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL] %2$08x %3$s %4$c %5$s%6$s", new Date(time), threadID, src, lvl, message, error);
    }
}
