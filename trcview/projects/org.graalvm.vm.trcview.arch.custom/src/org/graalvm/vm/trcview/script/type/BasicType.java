package org.graalvm.vm.trcview.script.type;

public enum BasicType {
    VOID("void"),
    CHAR("char"),
    SHORT("short"),
    INT("int"),
    LONG("long"),
    LONGLONG("long long");

    private final String name;

    private BasicType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
