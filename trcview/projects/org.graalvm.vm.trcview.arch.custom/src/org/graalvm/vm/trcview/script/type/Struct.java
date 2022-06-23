package org.graalvm.vm.trcview.script.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Struct extends Type {
    public static final Member DUMMY_MEMBER = new Member("<anonymous>", new PrimitiveType(BasicType.INT), 0);

    private final String name;
    private final Map<String, Member> members;

    private int size;

    public static class Member {
        public final String name;
        public final Type type;
        public final int offset;

        Member(String name, Type type, int offset) {
            this.name = name;
            this.type = type;
            this.offset = offset;
        }
    }

    public Struct() {
        this(null); // anonymous struct
    }

    public Struct(String name) {
        this.name = name;
        members = new HashMap<>();
        size = 0;
    }

    public String getName() {
        return name;
    }

    public void addMember(String field, Type type) {
        if (members.containsKey(field)) {
            throw new IllegalArgumentException("field " + field + " already exists");
        }
        Member member = new Member(field, type, size);
        members.put(field, member);
        size += type.size();
    }

    public List<Member> getMembers() {
        return members.values().stream().sorted((x, y) -> x.offset - y.offset).collect(Collectors.toList());
    }

    public Member getMember(String field) {
        return members.get(field);
    }

    public void copyof(Struct struct) {
        String otherName = struct.getName();
        if ((otherName != null && name == null) || (otherName == null && name != null)) {
            throw new IllegalArgumentException("name mismatch: one struct is anonymous");
        }
        if (otherName != null && !otherName.equals(name)) {
            throw new IllegalArgumentException("name mismatch");
        }
        if (!members.isEmpty()) {
            throw new IllegalStateException("struct is not empty");
        }
        for (Member m : struct.getMembers()) {
            members.put(m.name, m);
        }
        size = struct.size;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("struct " + name + " {\n");
        members.values().stream().sorted((x, y) -> x.offset - y.offset).forEach(member -> {
            Type t = member.type;
            buf.append('\t');
            buf.append(t.vardecl(member.name));
            buf.append(";\n");
        });
        buf.append("}");
        return buf.toString();
    }

    @Override
    public String vardecl(String var) {
        if (name == null) {
            return toString() + " " + var;
        } else {
            return "struct " + name + " " + var;
        }
    }

    @Override
    public int size() {
        return size;
    }
}
