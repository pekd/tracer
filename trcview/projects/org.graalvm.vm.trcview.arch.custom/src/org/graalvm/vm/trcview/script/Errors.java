package org.graalvm.vm.trcview.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Errors {
    /** List of error messages. */
    private final List<String> errors;

    /**
     * Initialization (must be called before compilation).
     */
    public Errors() {
        errors = new ArrayList<>();
    }

    /**
     * Add a new error message to the list of errors.
     */
    public void error(int line, int col, Message msg, Object... msgParams) {
        errors.add("-- line " + line + " col " + col + ": " + msg.format(msgParams));
    }

    /**
     * Returns the number of errors.
     */
    public int numErrors() {
        return errors.size();
    }

    /**
     * String representation for JUnit test cases.
     */
    public String dump() {
        return errors.stream().collect(Collectors.joining("\n"));
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
