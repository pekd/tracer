package org.graalvm.vm.x86.trcview.net.protocol;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class InfoRecord extends RpcRecord {
    public static final int MAGIC = 0x494e464f; // INFO

    private int version;

    protected InfoRecord() {
        super(MAGIC);
    }

    public InfoRecord(int version) {
        super(MAGIC);
        this.version = version;
    }

    @Override
    protected void writeData(WordOutputStream out) throws IOException {
        out.write32bit(version);
    }

    @Override
    protected void parse(WordInputStream in) throws IOException {
        this.version = in.read32bit();
    }
}
