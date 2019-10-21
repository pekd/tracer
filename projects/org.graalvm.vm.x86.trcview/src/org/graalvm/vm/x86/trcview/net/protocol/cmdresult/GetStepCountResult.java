package org.graalvm.vm.x86.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;

public class GetStepCountResult extends Result {
    private long steps;

    public GetStepCountResult() {
        super(Command.GET_STEP_COUNT);
    }

    public GetStepCountResult(long steps) {
        super(Command.GET_STEP_COUNT);
        this.steps = steps;
    }

    public long getSteps() {
        return steps;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        steps = in.read64bit();
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(steps);
    }
}
