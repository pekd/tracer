package org.graalvm.vm.trcview.info;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Comments {
    private final Map<Long, String> commentsInsn;
    private final Map<Long, String> commentsPC;

    public Comments() {
        commentsInsn = new HashMap<>();
        commentsPC = new HashMap<>();
    }

    public String getCommentForInsn(long insn) {
        return commentsInsn.get(insn);
    }

    public void setCommentForInsn(long insn, String comment) {
        if (comment == null) {
            commentsInsn.remove(insn);
        } else {
            commentsInsn.put(insn, comment);
        }
    }

    public String getCommentForPC(long pc) {
        return commentsPC.get(pc);
    }

    public void setCommentForPC(long pc, String comment) {
        if (comment == null) {
            commentsPC.remove(pc);
        } else {
            commentsPC.put(pc, comment);
        }
    }

    public Map<Long, String> getCommentsForInsns() {
        return Collections.unmodifiableMap(commentsInsn);
    }

    public Map<Long, String> getCommentsForPCs() {
        return Collections.unmodifiableMap(commentsPC);
    }
}
