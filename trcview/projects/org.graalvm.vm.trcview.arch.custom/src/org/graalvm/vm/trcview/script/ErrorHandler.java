package org.graalvm.vm.trcview.script;

public interface ErrorHandler {
    void error(Message msg, Object... args);
}
