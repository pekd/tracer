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
package org.graalvm.vm.trcview.ui;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.StringUtils;

public class StateEncoder {
    public static String diff(String s1, String s2) {
        return diff(s1, s2, false);
    }

    public static String diff(String s1, String s2, boolean html) {
        StringBuilder buf = new StringBuilder();
        String[] lines1 = s1.split("\\n");
        String[] lines2 = s2.split("\\n");
        if (lines1.length != lines2.length) {
            if (!html) {
                return Utils.html(s2);
            } else {
                return s2;
            }
        }
        for (int i = 0; i < lines1.length; i++) {
            String l1 = lines1[i];
            String l2 = lines2[i];
            if (l1.length() != l2.length()) {
                if (!html) {
                    buf.append(Utils.html(l2)).append('\n');
                } else {
                    buf.append(l2).append('\n');
                }
            } else if (l1.equals(l2)) {
                if (!html) {
                    buf.append(Utils.html(l2)).append('\n');
                } else {
                    buf.append(l2).append('\n');
                }
            } else {
                boolean eq = true;
                int state = 0;
                StringBuilder attrname = new StringBuilder();
                StringBuilder attrvalue = new StringBuilder();
                String data = null;
                for (int j = 0; j < l1.length(); j++) {
                    char c1 = l1.charAt(j);
                    char c2 = l2.charAt(j);
                    if (html) {
                        switch (state) {
                            case 0: // outside of tag
                                if (c2 == '<') {
                                    state = 7;
                                } else if (c1 == c2) {
                                    if (!eq) {
                                        eq = true;
                                        buf.append("</span>");
                                    }
                                } else {
                                    if (eq) {
                                        eq = false;
                                        if (data != null) {
                                            buf.append("<span class=\"change\" data=\"" + data + "\">");
                                        } else {
                                            buf.append("<span class=\"change\">");
                                        }
                                    }
                                }
                                break;
                            case 1: // in tag
                                if (c2 == '>') {
                                    state = 0;
                                } else if (!Character.isWhitespace(c2)) {
                                    attrname = new StringBuilder();
                                    attrvalue = new StringBuilder();
                                    attrname.append(c2);
                                    state = 2;
                                }
                                break;
                            case 2: // attribute name
                                if (c2 == '>') {
                                    state = 0;
                                } else if (c2 == '=') {
                                    state = 3;
                                } else {
                                    attrname.append(c2);
                                }
                                break;
                            case 3: // attribute value: begin
                                if (c2 == '"') {
                                    state = 4;
                                } else if (c2 == '\'') {
                                    state = 5;
                                } else {
                                    state = 6;
                                }
                                break;
                            case 4: // attribute value: quoted
                                if (c2 == '"') {
                                    if (attrname.toString().equalsIgnoreCase("data")) {
                                        data = attrvalue.toString();
                                    }
                                    state = 1;
                                } else {
                                    attrvalue.append(c2);
                                }
                                break;
                            case 5: // attribute value: squoted
                                if (c2 == '\'') {
                                    if (attrname.toString().equalsIgnoreCase("data")) {
                                        data = attrvalue.toString();
                                    }
                                    state = 1;
                                } else {
                                    attrvalue.append(c2);
                                }
                                break;
                            case 6: // attribute value: not quoted
                                if (Character.isWhitespace(c2)) {
                                    if (attrname.toString().equalsIgnoreCase("data")) {
                                        data = attrvalue.toString();
                                    }
                                    state = 1;
                                } else {
                                    attrvalue.append(c2);
                                }
                                break;
                            case 7: // '<'
                                if (c2 == '/') {
                                    state = 9;
                                    data = null;
                                } else {
                                    state = 8;
                                }
                                break;
                            case 8: // tag name
                                if (Character.isWhitespace(c2)) {
                                    state = 1;
                                } else if (c2 == '>') {
                                    state = 0;
                                }
                                break;
                            case 9: // closing tag
                                if (c2 == '>') {
                                    state = 0;
                                }
                                break;
                        }
                        buf.append(c2);
                    } else {
                        if (c1 == c2) {
                            if (!eq) {
                                eq = true;
                                buf.append("</span>");
                            }
                        } else {
                            if (eq) {
                                eq = false;
                                buf.append("<span class=\"change\">");
                            }
                        }
                        buf.append(Utils.html(Character.toString(c2)));
                    }
                }
                if (!eq) {
                    buf.append("</span>");
                }
                buf.append('\n');
            }
        }
        return buf.toString();

    }

    private static String str(String s, String style) {
        if (s == null) {
            return "";
        } else {
            if (style != null) {
                return "<span class=\"" + style + "\">" + Utils.html(s) + "</span>";
            } else {
                return Utils.html(s);
            }
        }
    }

    private static String pad(String s, int cnt) {
        int c = cnt - s.length();
        if (c < 1) {
            c = 1;
        }
        return StringUtils.repeat(" ", c);
    }

    private static String state(CpuState state) {
        StringBuilder buf = new StringBuilder();
        String text = state.toString();
        StringBuilder tmp = new StringBuilder();
        int fsm = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (fsm) {
                case 0:
                    switch (c) {
                        case '&':
                            buf.append("&amp;");
                            break;
                        case '<':
                            buf.append("&lt;");
                            break;
                        case '>':
                            buf.append("&gt;");
                            break;
                        case '{':
                            fsm = 1;
                            tmp = new StringBuilder();
                            break;
                        default:
                            buf.append(c);
                    }
                    break;
                case 1:
                    if (c == '{') {
                        fsm = 2;
                    } else {
                        buf.append('{');
                        buf.append(c);
                        fsm = 0;
                    }
                    break;
                case 2:
                    if (c == '}') {
                        fsm = 3;
                    } else {
                        tmp.append(c);
                    }
                    break;
                case 3:
                    if (c == '}') {
                        fsm = 4;
                    } else {
                        buf.append("{{");
                        buf.append(tmp);
                        buf.append('}');
                        buf.append(c);
                        fsm = 0;
                    }
                    break;
                case 4:
                    switch (c) {
                        case 'x':
                            buf.append("<span class=\"number\" data=\"hex:");
                            buf.append(tmp);
                            buf.append("\">");
                            buf.append(tmp);
                            buf.append("</span>");
                            break;
                        case 'o':
                            buf.append("<span class=\"number\" data=\"oct:");
                            buf.append(tmp);
                            buf.append("\">");
                            buf.append(tmp);
                            buf.append("</span>");
                            break;
                        case 'd':
                            buf.append("<span class=\"number\" data=\"dec:");
                            buf.append(tmp);
                            buf.append("\">");
                            buf.append(tmp);
                            buf.append("</span>");
                            break;
                        case 's':
                            buf.append("<span class=\"number\" data=\"str:");
                            buf.append(Utils.html(tmp.toString()));
                            buf.append("\">");
                            buf.append(tmp);
                            buf.append("</span>");
                            break;
                        case 'S':
                            buf.append("<span class=\"number\" data=\"str:");
                            buf.append(Utils.html(tmp.toString().toLowerCase()));
                            buf.append("\">");
                            buf.append(tmp);
                            buf.append("</span>");
                            break;
                        default:
                            buf.append(tmp);
                    }
                    fsm = 0;
                    break;
            }
        }
        return buf.toString();
    }

    private static String getDisassembly(Location loc) {
        String[] assembly = loc.getDisassembly();
        if (assembly == null) {
            return null;
        }
        if (assembly.length == 1) {
            return str(assembly[0], "mnemonic");
        } else {
            return str(assembly[0], "mnemonic") + pad(assembly[0], 8) + Utils.html(Stream.of(assembly).skip(1).collect(Collectors.joining(", ")));
        }
    }

    private static String encode(Location location) {
        StringBuilder buf = new StringBuilder();
        buf.append("IN: ");
        buf.append(str(location.getSymbolName(), "symbol"));
        if (location.getFilename() != null) {
            buf.append(" # ");
            buf.append(Utils.html(location.getFilename()));
            if (location.getOffset() != -1) {
                buf.append(" @ 0x");
                buf.append(HexFormatter.tohex(location.getOffset(), 8));
            }
        }
        buf.append('\n');
        if (location.getFormat().numberfmt == StepFormat.NUMBERFMT_HEX) {
            buf.append("0x");
            buf.append(HexFormatter.tohex(location.getPC(), 8));
        } else {
            buf.append(location.getFormat().formatAddress(location.getPC()));
        }
        buf.append(":\t");
        if (location.getDisassembly() != null) {
            buf.append(getDisassembly(location));
            buf.append(" <span class=\"comment\">; ");
            if (location.getMachinecode() != null && location.getMachinecode().length > 0) {
                buf.append(Utils.html(location.getPrintableBytes()));
            } else {
                buf.append("&lt;no code&gt;");
            }
            buf.append("</span>");
        }
        return buf.toString();
    }

    public static String encode(TraceAnalyzer trc, StepEvent step) {
        Location location = Location.getLocation(trc, step);
        CpuState state = step.getState();
        String loc = encode(location);
        String pos = "TID: " + step.getTid() + "\ninstruction: " + step.getStep();
        if (trc.getArchitecture().isTaggedState()) {
            return loc + "\n\n" + state(state) + "\n" + pos;
        } else {
            return loc + "\n\n" + state + "\n" + pos;
        }
    }

    public static String encode(TraceAnalyzer trc, StepEvent previous, StepEvent current) {
        Location location = Location.getLocation(trc, current);
        CpuState state1 = previous.getState();
        CpuState state2 = current.getState();
        String loc = encode(location);
        String pos = "TID: " + state2.getTid() + "\ninstruction: " + state2.getStep();
        if (trc.getArchitecture().isTaggedState()) {
            return loc + "\n\n" + diff(state(state1), state(state2), true) + "\n" + pos;
        } else {
            return loc + "\n\n" + diff(state1.toString(), state2.toString()) + "\n" + pos;
        }
    }
}
