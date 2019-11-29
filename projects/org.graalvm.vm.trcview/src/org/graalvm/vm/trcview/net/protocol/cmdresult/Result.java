package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public abstract class Result {
    private int type;

    public Result(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public abstract void read(WordInputStream in) throws IOException;

    public abstract void write(WordOutputStream out) throws IOException;
}
