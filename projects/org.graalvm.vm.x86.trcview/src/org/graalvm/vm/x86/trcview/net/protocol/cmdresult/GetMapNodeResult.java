package org.graalvm.vm.x86.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.net.protocol.IO;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;

public class GetMapNodeResult extends Result {
    private Node node;

    public GetMapNodeResult() {
        super(Command.GET_MAP_NODE);
    }

    public GetMapNodeResult(Node node) {
        super(Command.GET_MAP_NODE);
        this.node = node;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        if (in.read() == 0) {
            node = null;
        } else {
            node = IO.readNode(in);
        }
    }

    public Node getNode() {
        return node;
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        if (node == null) {
            out.write(0);
        } else {
            out.write(1);
            IO.writeNode(out, node);
        }
    }
}
