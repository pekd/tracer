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
package org.graalvm.vm.trcview.analysis;

import java.util.List;

import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;

public class Search {
    public static Node next(Node node) {
        if (node instanceof BlockNode) {
            BlockNode block = (BlockNode) node;
            return block.getFirstNode();
        } else if (node instanceof StepEvent) {
            BlockNode block = node.getParent();
            boolean start = false;
            for (Node n : block.getNodes()) {
                if (n == node) {
                    start = true;
                } else if (start && (n instanceof BlockNode || n instanceof StepEvent)) {
                    return n;
                }
            }
            return null;
        } else {
            throw new IllegalArgumentException("Not a BlockNode/RecordNode");
        }
    }

    public static Node nextStep(Node node) {
        if (node instanceof BlockNode) {
            BlockNode block = (BlockNode) node;
            return block.getFirstNode();
        } else if (node instanceof Event) {
            BlockNode block = node.getParent();
            boolean start = false;
            for (Node n : block.getNodes()) {
                if (n == node) {
                    start = true;
                } else if (start && (n instanceof BlockNode || n instanceof StepEvent)) {
                    return n;
                }
            }
            return null;
        } else {
            throw new IllegalArgumentException("Not a BlockNode/RecordNode");
        }
    }

    public static Node previousStep(Node node) {
        BlockNode block = node.getParent();
        if (block == null) {
            return null;
        }
        if (node == block.getFirstNode()) {
            return block;
        }
        Node last = null;
        for (Node n : block.getNodes()) {
            if (n == node) {
                if (last != null) {
                    return last;
                } else {
                    return block;
                }
            } else if (n instanceof BlockNode || n instanceof StepEvent) {
                last = n;
            }
        }
        return null;
    }

    public static Node nextPC(Node startNode, long pc) {
        Node start = next(startNode);
        if (start == null) {
            return null;
        }
        Node c = nextPCChildren(start, pc);
        if (c == null) {
            // follow parents until reaching root
            c = start;
            BlockNode block = c.getParent();
            while (block != null) {
                boolean started = false;
                for (Node n : block.getNodes()) {
                    if (n == c) {
                        started = true;
                    } else if (started && (n instanceof BlockNode || (n instanceof StepEvent))) {
                        Node ret = nextPCChildren(n, pc);
                        if (ret != null) {
                            return ret;
                        }
                        break;
                    }
                }
                c = block;
                block = c.getParent();
            }
            return null;
        } else {
            return c;
        }
    }

    private static Node nextPCChildren(Node start, long pc) {
        if (start instanceof BlockNode) {
            BlockNode block = (BlockNode) start;
            if (block.getHead().getPC() == pc) {
                return block;
            }
            for (Node n : block.getNodes()) {
                if (n instanceof StepEvent) {
                    StepEvent step = (StepEvent) n;
                    if (step.getPC() == pc) {
                        return n;
                    }
                } else if (n instanceof BlockNode) {
                    Node next = nextPCChildren(n, pc);
                    if (next != null) {
                        return next;
                    }
                }
            }
            return null;
        } else if (start instanceof StepEvent) {
            if (((StepEvent) start).getPC() == pc) {
                return start;
            }
            BlockNode parent = start.getParent();
            boolean started = false;
            for (Node n : parent.getNodes()) {
                if (started) {
                    if (n instanceof StepEvent) {
                        StepEvent step = (StepEvent) n;
                        if (step.getPC() == pc) {
                            return n;
                        }
                    } else if (n instanceof BlockNode) {
                        Node next = nextPCChildren(n, pc);
                        if (next != null) {
                            return next;
                        }
                    }
                } else if (n == start) {
                    started = true;
                }
            }
            return null;
        } else {
            if (start instanceof Event) {
                throw new IllegalArgumentException("invalid start node type: " + start.getClass().getCanonicalName() + " [" + start.getClass() + "]");
            } else {
                throw new IllegalArgumentException("invalid start node type: " + start.getClass().getCanonicalName());
            }
        }
    }

    private static long getInstruction(Node node) {
        if (node instanceof BlockNode) {
            return ((BlockNode) node).getHead().getStep();
        } else if (node instanceof StepEvent) {
            return ((StepEvent) node).getStep();
        } else {
            return -1;
        }
    }

    public static Node instruction(Node root, long insn) {
        assert root != null;
        if (root instanceof StepEvent) {
            StepEvent step = (StepEvent) root;
            return step.getStep() == insn ? root : null;
        } else if (root instanceof BlockNode) {
            BlockNode block = (BlockNode) root;
            if (block.getHead() != null) {
                if (block.getHead().getStep() == insn) {
                    return block;
                } else if (block.getHead().getStep() > insn) {
                    return null;
                }
            }

            // TODO: use binary search here
            List<Node> nodes = block.getNodes();
            BlockNode previous = null;
            for (Node node : nodes) {
                long cnt = getInstruction(node);
                if (cnt == insn) {
                    return node;
                } else if (cnt > insn) {
                    if (previous == null) {
                        return null;
                    } else {
                        return instruction(previous, insn);
                    }
                } else {
                    if (node instanceof BlockNode) {
                        previous = (BlockNode) node;
                    }
                }
            }
            if (previous != null) {
                return instruction(previous, insn);
            } else {
                return null;
            }
        } else {
            throw new IllegalArgumentException("Not a BlockNode/RecordNode");
        }
    }
}
