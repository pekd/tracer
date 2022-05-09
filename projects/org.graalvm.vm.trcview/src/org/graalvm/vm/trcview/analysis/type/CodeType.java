package org.graalvm.vm.trcview.analysis.type;

import org.graalvm.vm.trcview.arch.io.StepEvent;

public class CodeType extends Type {
    private StepEvent event;

    public CodeType(StepEvent event) {
        super(DataType.CODE, true, event.getMachinecode().length);
        this.event = event;
    }

    public StepEvent getEvent() {
        return event;
    }
}
