package org.graalvm.vm.x86;

import java.io.File;
import java.io.IOException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import com.everyware.util.log.Levels;
import com.everyware.util.log.Trace;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;

public class AMD64VM {
    public static void main(String[] args) throws IOException {
        Trace.setupConsoleApplication(Levels.INFO);
        Source source = Source.newBuilder(AMD64Language.NAME, new File(args[0])).build();
        executeSource(source, args);
    }

    private static int executeSource(Source source, String[] args) {
        System.out.println("== running on " + Truffle.getRuntime().getName());

        Context ctx = Context.newBuilder(AMD64Language.NAME).arguments(AMD64Language.NAME, args).build();

        try {
            Value result = ctx.eval(source);

            if (result == null) {
                throw new Exception("Error while executing file");
            }

            return result.asInt();
        } catch (Throwable ex) {
            /*
             * PolyglotEngine.eval wraps the actual exception in an IOException, so we have to unwrap here.
             */
            Throwable cause = ex.getCause();
            if (cause instanceof UnsupportedSpecializationException) {
                cause.printStackTrace(System.err);
            } else {
                /* Unexpected error, just print out the full stack trace for debugging purposes. */
                ex.printStackTrace(System.err);
            }
            return 1;
        } finally {
            ctx.close();
        }
    }
}
