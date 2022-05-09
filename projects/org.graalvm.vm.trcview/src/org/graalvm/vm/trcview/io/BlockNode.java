/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.vm.trcview.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class BlockNode extends Node implements Block {
    private static final Logger log = Trace.create(BlockNode.class);

    private StepEvent head;
    private List<Node> children;
    private InterruptEvent interrupt;

    private CpuState headState;

    public BlockNode(StepEvent head) {
        this(head, null);
    }

    public BlockNode(InterruptEvent head) {
        this(head.getStep(), null);
        this.interrupt = head;
    }

    public BlockNode(StepEvent head, List<Node> children) {
        this.head = head;
        if (head != null) {
            head.setParent(this);
        }
        if (children != null) {
            setChildren(children);
        } else {
            this.children = new ArrayList<>();
        }
    }

    public void setHeadState(CpuState state) {
        headState = state;
    }

    public CpuState getHeadState() {
        return headState;
    }

    public void clearHeadState() {
        headState = null;
    }

    public void setChildren(List<Node> children) {
        if (children != null) {
            this.children = children;
            for (Node n : children) {
                n.setParent(this);
            }
        } else {
            this.children = new ArrayList<>();
        }
    }

    public void add(Node child) {
        children.add(child);
        child.setParent(this);
    }

    public void trim() {
        if (children != null && children instanceof ArrayList) {
            ((ArrayList<?>) children).trimToSize();
        }
    }

    public boolean isInterrupt() {
        return interrupt != null;
    }

    public InterruptEvent getInterrupt() {
        return interrupt;
    }

    @Override
    public StepEvent getHead() {
        return head;
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public Node get(int i) {
        return children.get(i);
    }

    @Override
    public int size() {
        return children.size();
    }

    public Node getFirstNode() {
        if (children.isEmpty()) {
            return null;
        } else {
            return children.get(0);
        }
    }

    public long getStep() {
        if (head != null) {
            return head.getStep();
        } else {
            return getFirstStep().getStep();
        }
    }

    public StepEvent getFirstStep() {
        for (Node n : children) {
            if (n instanceof StepEvent) {
                return (StepEvent) n;
            } else if (n instanceof BlockNode) {
                BlockNode b = (BlockNode) n;
                if (b.getHead() != null) {
                    return b.getHead();
                } else {
                    return b.getFirstStep();
                }
            }
        }
        log.log(Levels.WARNING, "No step event found! " + children.size() + " children");
        return null;
    }

    @Override
    public int getTid() {
        if (head != null) {
            return head.getTid();
        } else {
            return children.get(0).getTid();
        }
    }
}
