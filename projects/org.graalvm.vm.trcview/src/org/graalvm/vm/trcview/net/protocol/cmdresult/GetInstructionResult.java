package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetInstructionResult extends Result {
    private Node node;

    public GetInstructionResult() {
        super(Command.GET_INSTRUCTION);
    }

    public GetInstructionResult(Node node) {
        super(Command.GET_INSTRUCTION);
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