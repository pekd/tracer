package org.graalvm.vm.trcview.ui.event;

public interface StepListenable {
    void addStepListener(StepListener listener);

    void removeStepListener(StepListener listener);
}
