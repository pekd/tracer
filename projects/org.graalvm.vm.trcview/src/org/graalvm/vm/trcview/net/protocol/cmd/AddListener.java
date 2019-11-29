package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class AddListener extends Command {
    private int type;

    public AddListener() {
        super(ADD_LISTENER);
    }

    public AddListener(int type) {
        super(ADD_LISTENER);
        this.type = type;
    }

    public int getListenerType() {
        return type;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        type = in.read();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write(type);
    }
}
