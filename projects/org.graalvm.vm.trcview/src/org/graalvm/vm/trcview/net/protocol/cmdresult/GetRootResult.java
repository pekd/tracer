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

public class GetRootResult extends Result {
    private BlockNode root;

    public GetRootResult() {
        super(Command.GET_ROOT);
    }

    public GetRootResult(BlockNode root) {
        super(Command.GET_ROOT);
        this.root = root;
    }

    public BlockNode getRoot() {
        return root;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        root = (BlockNode) IO.readNode(in);
        int count = in.read32bit();
        List<Node> children = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            children.add(IO.readNode(in));
        }
        root.setChildren(children);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        if (root == null) {
            throw new NullPointerException();
        }

        IO.writeNode(out, root);
        out.write32bit(root.getNodes().size());
        for (Node node : root.getNodes()) {
            IO.writeNode(out, node);
        }
    }
}
