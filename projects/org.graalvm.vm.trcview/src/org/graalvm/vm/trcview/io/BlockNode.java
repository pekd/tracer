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

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.Analysis;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.IncompleteTraceStep;
import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class BlockNode extends Node implements Block {
    private static final Logger log = Trace.create(BlockNode.class);
    private static final int PROGRESS_UPDATE = 10_000;

    private StepEvent head;
    private List<Node> children;
    private InterruptEvent interrupt;

    public BlockNode(StepEvent head) {
        this(head, null);
    }

    public BlockNode(InterruptEvent head) {
        this(head.getStep(), null);
        this.interrupt = head;
    }

    public BlockNode(StepEvent head, List<Node> children) {
        this.head = head;
        if (children != null) {
            setChildren(children);
        }
    }

    public void setChildren(List<Node> children) {
        this.children = children;
        if (children != null) {
            for (Node n : children) {
                n.setParent(this);
            }
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

    public static BlockNode read(TraceReader in, Analysis analysis) throws IOException {
        return read(in, analysis, null);
    }

    public static BlockNode read(TraceReader in, Analysis analysis, ProgressListener progress) throws IOException {
        List<Node> nodes = new ArrayList<>();
        Node node;
        int tid = 0;
        int cnt = 0;
        while ((node = parseRecord(in, analysis, progress, tid, false)) != null) {
            if (isStep(node)) {
                nodes.add(node);
            }
            if (tid == 0) {
                tid = node.getTid();
            }
            if (progress != null && cnt > PROGRESS_UPDATE) {
                cnt = 0;
                progress.progressUpdate(in.tell());
            } else {
                cnt++;
            }
        }
        return new BlockNode(null, nodes);
    }

    private static boolean isStep(Node node) {
        if (node instanceof BlockNode) {
            return true;
        } else if (node instanceof StepEvent) {
            return true;
        }
        return false;
    }

    private static Node parseRecord(TraceReader in, Analysis analysis, ProgressListener progress, int thread, boolean ignoreTrap) throws IOException {
        boolean system = in.getArchitecture().isSystemLevel();
        boolean stackedTraps = in.getArchitecture().isStackedTraps();
        int tid = thread;
        Event event = null;
        try {
            event = in.read();
        } catch (EOFException e) {
            log.log(Levels.WARNING, "Unexpected EOF", e);
        }
        if (event == null) {
            return null;
        }
        while (tid != 0 && event.getTid() != tid) {
            event = in.read();
            if (event == null) {
                return null;
            }
        }
        if (tid == 0) {
            tid = event.getTid();
        }
        if (event instanceof StepEvent) {
            StepEvent step = (StepEvent) event;
            if (step.getMachinecode() != null && (step.isCall() || (system && step.isSyscall()))) {
                BlockNode block = new BlockNode(step);
                analysis.process(event, block);
                if (progress != null) {
                    progress.progressUpdate(in.tell());
                }
                List<Node> result = new ArrayList<>();
                boolean hasSteps = false;
                int cnt = 0;
                boolean ignore = true;
                while (true) {
                    Node child = parseRecord(in, analysis, progress, tid, ignore);
                    if (child == null) {
                        if (!hasSteps) {
                            result.add(new IncompleteTraceStep(tid));
                        }
                        break;
                    }
                    if (isStep(child)) {
                        result.add(child);
                    }
                    if (progress != null && cnt > PROGRESS_UPDATE) {
                        cnt = 0;
                        progress.progressUpdate(in.tell());
                    } else {
                        cnt++;
                    }
                    if (child instanceof StepEvent) {
                        hasSteps = true;
                        ignore = false;
                        StepEvent s = (StepEvent) child;
                        if (s.getMachinecode() == null || (s.isReturn() || (system && s.isReturnFromSyscall()))) {
                            if (progress != null) {
                                progress.progressUpdate(in.tell());
                            }
                            break;
                        }
                    } else if (child instanceof BlockNode) {
                        hasSteps = true;
                        ignore = false;
                        BlockNode n = (BlockNode) child;
                        int size = n.getNodes().size();
                        if (!stackedTraps && system && n.getHead() != null && !n.isInterrupt() && !n.getHead().isSyscall() && size > 0) {
                            Node last = n.getNodes().get(size - 1);
                            while (last instanceof BlockNode) {
                                BlockNode b = (BlockNode) last;
                                if (b.isInterrupt() || b.getHead().isSyscall()) {
                                    break;
                                } else {
                                    size = b.getNodes().size();
                                    if (size > 0) {
                                        last = b.getNodes().get(size - 1);
                                    } else {
                                        break;
                                    }
                                }
                            }
                            if (last instanceof StepEvent) {
                                StepEvent s = (StepEvent) last;
                                if (s.isReturnFromSyscall()) {
                                    log.log(Levels.DEBUG, () -> String.format("break: %d [%x] in %d [%x]\n", s.getStep(), s.getPC(), step.getStep(), step.getPC()));
                                    break;
                                }
                            }
                        }
                    }
                }
                block.setChildren(result);
                return block;
            } else {
                Event node = event;
                analysis.process(event, node);
                return node;
            }
        } else if (!ignoreTrap && system && event instanceof InterruptEvent) {
            InterruptEvent interrupt = (InterruptEvent) event;
            BlockNode block = new BlockNode(interrupt);
            analysis.process(event, block);
            if (progress != null) {
                progress.progressUpdate(in.tell());
            }
            List<Node> result = new ArrayList<>();
            boolean hasSteps = false;
            int cnt = 0;
            while (true) {
                Node child = parseRecord(in, analysis, progress, tid, false);
                if (child == null) {
                    if (!hasSteps) {
                        result.add(new IncompleteTraceStep(tid));
                    }
                    break;
                }
                if (isStep(child)) {
                    result.add(child);
                }
                if (progress != null && cnt > 10_000) {
                    cnt = 0;
                    progress.progressUpdate(in.tell());
                } else {
                    cnt++;
                }
                if (child instanceof StepEvent) {
                    hasSteps = true;
                    StepEvent s = (StepEvent) child;
                    if (s.getMachinecode() == null || s.isReturnFromSyscall()) {
                        if (progress != null) {
                            progress.progressUpdate(in.tell());
                        }
                        break;
                    }
                } else if (child instanceof BlockNode) {
                    hasSteps = true;
                    BlockNode n = (BlockNode) child;
                    int size = n.getNodes().size();
                    if (!stackedTraps && system && n.getHead() != null && !n.isInterrupt() && !n.getHead().isSyscall() && size > 0) {
                        Node last = n.getNodes().get(size - 1);
                        while (last instanceof BlockNode) {
                            BlockNode b = (BlockNode) last;
                            if (b.isInterrupt() || b.getHead().isSyscall()) {
                                break;
                            } else {
                                size = b.getNodes().size();
                                if (size > 0) {
                                    last = b.getNodes().get(size - 1);
                                } else {
                                    break;
                                }
                            }
                        }
                        if (last instanceof StepEvent) {
                            StepEvent s = (StepEvent) last;
                            if (s.isReturnFromSyscall()) {
                                log.log(Levels.DEBUG, () -> String.format("break trap: %d [%x]\n", s.getStep(), s.getPC()));
                                break;
                            }
                        }
                    }
                }
            }
            block.setChildren(result);
            return block;
        } else {
            Event node = event;
            analysis.process(event, node);
            return node;
        }
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
