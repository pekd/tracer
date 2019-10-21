package org.graalvm.vm.x86.trcview.net.protocol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public abstract class RpcRecord {
    private final int magic;
    private static final Map<Integer, Supplier<? extends RpcRecord>> types = new HashMap<>();

    protected RpcRecord(int magic) {
        this.magic = magic;
    }

    public static <T extends RpcRecord> void register(int magic, Supplier<T> create) {
        types.put(magic, create);
    }

    public void write(WordOutputStream out) throws IOException {
        out.write32bit(magic);
        writeData(out);
    }

    protected abstract void writeData(WordOutputStream out) throws IOException;

    protected abstract void parse(WordInputStream in) throws IOException;

    public static <T extends RpcRecord> T read(WordInputStream in) throws IOException {
        int type = in.read32bit();
        @SuppressWarnings("unchecked")
        Supplier<T> generator = (Supplier<T>) types.get(type);
        T record = generator.get();
        record.parse(in);
        return record;
    }

    static {
        register(CommandRecord.MAGIC, CommandRecord::new);
        register(CommandResponseRecord.MAGIC, CommandResponseRecord::new);
        register(InfoRecord.MAGIC, InfoRecord::new);
    }
}
