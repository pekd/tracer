package org.graalvm.vm.trcview.ui.event;

import org.graalvm.vm.trcview.arch.io.StepEvent;

public interface StepListener {
    void setStep(StepEvent step);
}
