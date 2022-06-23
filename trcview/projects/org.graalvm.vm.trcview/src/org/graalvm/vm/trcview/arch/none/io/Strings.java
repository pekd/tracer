package org.graalvm.vm.trcview.arch.none.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;

public class Strings {
    private final List<String> strings = new ArrayList<>();

    public Strings() {
        strings.add("");
    }

    public String[] read(WordInputStream in) throws IOException {
        int idcnt = in.read8bit();
        String[] asm = new String[idcnt];
        for (int i = 0; i < asm.length; i++) {
            int id = in.read32bit();
            if (id == -1) {
                String s = IO.readString(in);
                strings.add(s);
                asm[i] = s;
            } else {
                asm[i] = strings.get(id);
            }
        }
        return asm;
    }
}
