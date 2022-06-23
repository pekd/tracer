package org.graalvm.vm.trcview.analysis.type;

public class NameValidator {
    // [a-zA-Z_][a-zA-Z0-9_]*
    public static boolean isValidName(String name) {
        if (name.length() == 0) {
            return false;
        }
        char c = name.charAt(0);
        if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_') {
            for (int i = 1; i < name.length(); i++) {
                c = name.charAt(i);
                if (c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_') {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
