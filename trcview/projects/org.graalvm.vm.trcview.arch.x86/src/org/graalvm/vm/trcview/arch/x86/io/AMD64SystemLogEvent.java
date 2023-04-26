package org.graalvm.vm.trcview.arch.x86.io;

import static org.graalvm.vm.trcview.arch.x86.io.AMD64TraceReader.readString;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.graalvm.vm.trcview.arch.io.SystemLogEvent;
import org.graalvm.vm.util.StringUtils;
import org.graalvm.vm.util.io.WordInputStream;

public class AMD64SystemLogEvent extends SystemLogEvent {
    private long seq;
    private long time;
    private int level;
    private int threadID;
    private String logger;
    private String clazz;
    private String method;
    private String message;
    private String throwable;

    public AMD64SystemLogEvent(WordInputStream in, int tid) throws IOException {
        super(tid);

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
