package org.graalvm.vm.trcview.analysis.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegisterFormat {
    private final List<FieldFormat> formats = new ArrayList<>();

    public void addField(FieldFormat field) {
        formats.add(field);
        Collections.sort(formats, (a, b) -> Integer.compare(b.field.from, a.field.from));
    }

    public List<FieldFormat> getFormats() {
        return Collections.unmodifiableList(formats);
    }
}
