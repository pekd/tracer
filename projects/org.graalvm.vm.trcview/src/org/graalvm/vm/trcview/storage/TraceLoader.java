package org.graalvm.vm.trcview.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.graalvm.vm.trcview.arch.io.TraceFileReader;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.util.log.Trace;

public class TraceLoader {
    public static void main(String[] args) throws IOException {
        Trace.setupConsoleApplication();
        try (InputStream file = new BufferedInputStream(new FileInputStream(args[0]), 1024 * 1024)) {
            TraceReader in = new TraceFileReader(file);

            // StorageBackend storage = new CassandraBackend();
            StorageBackend storage = new MemoryBackend();
            try {
                storage.connect("test.trc");
            } catch (IllegalArgumentException e) {
                storage.create("test.trc", in.getArchitecture().getId());
            }

            long size = new File(args[0]).length();
            long start = System.currentTimeMillis();
            TraceParser parser = new TraceParser(in, pos -> {
                double percent = pos * 100.0 / size;
                long t = System.currentTimeMillis();
                double sec = (t - start) / 1000.0;
                double total = sec / (percent / 100.0);
                double left = total - sec;
                System.out.printf("\r\u001b[K%2.3f%% [%1.2f min remaining]", percent, left / 60.0);
            }, storage);
            long steps = parser.read();
            storage.flush();
            System.out.println(" done");
            long end = System.currentTimeMillis();
            double sec = (end - start) / 1000.0;
            System.out.printf("%s seconds, %d steps\n", sec, steps);
            storage.close();
        }
        System.exit(0);
    }
}
