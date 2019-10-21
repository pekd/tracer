package org.graalvm.vm.x86.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.net.protocol.IO;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;

public class GetNextPCResult extends Result {
    private Node node;

    public GetNextPCResult() {
        super(Command.GET_NEXT_PC);
    }

    public GetNextPCResult(Node node) {
        super(Command.GET_NEXT_PC);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        node = IO.readNode(in);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        IO.writeNode(out, node);
    }
}
