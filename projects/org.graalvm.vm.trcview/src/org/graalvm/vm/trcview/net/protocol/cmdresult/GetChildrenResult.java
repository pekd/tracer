package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetChildrenResult extends Result {
    private BlockNode node;

    public GetChildrenResult() {
        super(Command.GET_CHILDREN);
    }

    public GetChildrenResult(BlockNode node) {
        super(Command.GET_CHILDREN);
        this.node = node;
    }

    public BlockNode getNode() {
        return node;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        node = (BlockNode) IO.readNode(in);
        int count = in.read32bit();
        List<Node> children = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            children.add(IO.readNode(in));
        }
        node.setChildren(children);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        IO.writeNode(out, node);
        out.write32bit(node.getNodes().size());
        for (Node n : node.getNodes()) {
            IO.writeNode(out, n);
        }
    }
}
