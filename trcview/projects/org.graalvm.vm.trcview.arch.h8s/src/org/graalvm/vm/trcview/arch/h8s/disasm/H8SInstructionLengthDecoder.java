package org.graalvm.vm.trcview.arch.h8s.disasm;

import org.graalvm.vm.trcview.arch.CodeReader;

public class H8SInstructionLengthDecoder {
    public static int getLength(CodeReader reader) {
        int op = Short.toUnsignedInt(reader.peekI16(0));

        switch (op >> 8) {
            case 0x00:
                if (op == 0) {
                    /* NOP */
                    return 2;
                } else {
                    return 0;
                }
            case 0x01:
                switch (op & 0xFF) {
                    case 0x00: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x69:
                                switch (op2 & 0x88) {
                                    case 0x00:
                                        /* MOV.L @ERs, ERd */
                                        return 4;
                                    case 0x80:
                                        /* MOV.L ERs, @ERd */
                                        return 4;
                                    default:
                                        return 0;
                                }
                            case 0x6B:
                                switch (op2 & 0xF8) {
                                    case 0x00:
                                        /* MOVL.L @aa:16, ERd */
                                        return 6;
                                    case 0x20:
                                        /* MOVL.L @aa:32, ERd */
                                        return 8;
                                    case 0x80:
                                        /* MOVL.L ERs, @aa:16 */
                                        return 6;
                                    case 0xA0:
                                        /* MOVL.L ERs, @aa:32 */
                                        return 8;
                                    default:
                                        return 0;
                                }
                            case 0x6D:
                                switch (op2 & 0x88) {
                                    case 0x00:
                                        /* MOV.L @ERs+, ERd */
                                        return 4;
                                    case 0x80:
                                        /* MOV.L ERs, @-ERd */
                                        return 4;
                                    default:
                                        return 0;
                                }
                            case 0x6F:
                                switch (op2 & 0x88) {
                                    case 0x00:
                                        /* MOV.L @(d:16, ERs), ERd */
                                        return 6;
                                    case 0x80:
                                        /* MOV.L ERs, @(d:16, ERd) */
                                        return 6;
                                    default:
                                        return 0;
                                }
                            case 0x78:
                                switch (op2 & 0x8F) {
                                    case 0x00:
                                        int op3 = Short.toUnsignedInt(reader.peekI16(4));
                                        switch (op3 >> 4) {
                                            case 0x6B2: {
                                                if ((op3 & 0x08) != 0) {
                                                    return 0;
                                                } else {
                                                    /* MOV.L @(d:32, ERs), ERd */
                                                    return 10;
                                                }
                                            }
                                            case 0x6BA: {
                                                if ((op3 & 0x08) != 0) {
                                                    return 0;
                                                } else {
                                                    /* MOV.L ERs, @(d:32, ERd) */
                                                    return 10;
                                                }
                                            }
                                            default:
                                                return 0;
                                        }
                                    default:
                                        return 0;
                                }
                            default:
                                return 0;
                        }
                    }
                    case 0x10: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 & 0xFFF8) {
                            case 0x6D70:
                                /* LDM.L @SP+, (ERn-ERn+1) */
                                return 4;
                            case 0x6DF0:
                                return 4;
                            default:
                                return 0;
                        }
                    }
                    case 0x20: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 & 0xFFF8) {
                            case 0x6D70:
                                return 4;
                            case 0x6DF0:
                                return 4;
                            default:
                                return 0;
                        }
                    }
                    case 0x30: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 & 0xFFF8) {
                            case 0x6D70:
                                return 4;
                            case 0x6DF0:
                                return 4;
                            default:
                                return 0;
                        }
                    }
                    case 0x40: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x69:
                                switch (op2 & 0x8F) {
                                    case 0x00:
                                        /* LDC.W @ERs, CCR */
                                        return 4;
                                    case 0x80:
                                        /* STC.W CCR, @ERd */
                                        return 4;
                                    default:
                                        return 0;
                                }
                            case 0x6B:
                                switch (op2 & 0xFF) {
                                    case 0x00:
                                        /* LDC.W @aa:16, CCR */
                                        return 6;
                                    case 0x20:
                                        /* LDC.W @aa:32, CCR */
                                        return 8;
                                    case 0x80:
                                        /* STC.W CCR, @aa:16 */
                                        return 6;
                                    case 0xA0:
                                        /* STC.W CCR, @aa:32 */
                                        return 8;
                                    default:
                                        return 0;
                                }
                            case 0x6D:
                                switch (op2 & 0x8F) {
                                    case 0x00:
                                        /* LDC.W @ERs+, CCR */
                                        return 4;
                                    case 0x80:
                                        /* STC.W CCR, @-ERs */
                                        return 4;
                                    default:
                                        return 0;
                                }
                            case 0x6F:
                                switch (op2 & 0x8F) {
                                    case 0x00:
                                        /* LDC.W @(d:16, ERs), CCR */
                                        return 6;
                                    case 0x80:
                                        /* STC.W CCR, @(d:16, ERd) */
                                        return 6;
                                    default:
                                        return 0;
                                }
                            case 0x78: {
                                int op3 = Short.toUnsignedInt(reader.peekI16(4));
                                switch (op3) {
                                    case 0x6B20:
                                        if ((op2 & 0x88) != 0) {
                                            return 0;
                                        } else {
                                            /* LDC.W @(d:32, ERs), CCR */
                                            return 10;
                                        }
                                    case 0x6BA0:
                                        if ((op2 & 0x88) != 0) {
                                            return 0;
                                        } else {
                                            /* STC.W CCR, @(d:32, ERs) */
                                            return 10;
                                        }
                                    default:
                                        return 0;
                                }
                            }
                            default:
                                return 0;
                        }
                    }
                    case 0x41: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x04:
                                /* ORC #xx:8, EXR */
                                return 4;
                            case 0x05:
                                /* XORC #xx:8, EXR */
                                return 4;
                            case 0x06:
                                /* ANDC #xx:8, EXR */
                                return 4;
                            case 0x07:
                                /* LDC.B #xx:8, EXR */
                                return 4;
                            case 0x69: {
                                switch (op2 & 0x8F) {
                                    case 0x00:
                                        /* LDC.W @ERs, EXR */
                                        return 4;
                                    case 0x80:
                                        /* STC.W EXR, @ERs */
                                        return 4;
                                    default:
                                        return 0;
                                }
                            }
                            case 0x6B:
                                switch (op2 & 0xFF) {
                                    case 0x00:
                                        /* LDC.W @aa:16, EXR */
                                        return 6;
                                    case 0x20:
                                        /* LDC.W @aa:32, EXR */
                                        return 8;
                                    case 0x80:
                                        /* STC.W EXR, @aa:16 */
                                        return 6;
                                    case 0xA0:
                                        /* STC.W EXR, @aa:32 */
                                        return 8;
                                    default:
                                        return 0;
                                }
                            case 0x6D:
                                switch (op2 & 0x8F) {
                                    case 0x00:
                                        /* LDC.W @ERs+, EXR */
                                        return 4;
                                    case 0x80:
                                        /* STC.W EXR, @-ERd */
                                        return 4;
                                    default:
                                        return 0;
                                }
                            case 0x6F:
                                switch (op2 & 0x8F) {
                                    case 0x00:
                                        /* LDC.W @(d:16, ERs), EXR */
                                        return 6;
                                    case 0x80:
                                        /* STC.W EXR, @(d:16, ERs) */
                                        return 6;
                                    default:
                                        return 0;
                                }
                            case 0x78: {
                                int op3 = Short.toUnsignedInt(reader.peekI16(4));
                                switch (op3) {
                                    case 0x6B20:
                                        if ((op2 & 0x8F) != 0) {
                                            return 0;
                                        } else {
                                            /* LDC.W @(d:32, ERs), EXR */
                                            return 10;
                                        }
                                    case 0x6BA0:
                                        if ((op2 & 0x8F) != 0) {
                                            return 0;
                                        } else {
                                            /* STC.W EXR, @(d:32, ERs) */
                                            return 10;
                                        }
                                    default:
                                        return 0;
                                }
                            }
                            default:
                                return 0;
                        }
                    }
                    case 0x80:
                        /* SLEEP */
                        return 2;
                    case 0xC0: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x50:
                                /* MULXS.B Rs, Rd */
                                return 4;
                            case 0x52:
                                if ((op2 & 0x08) != 0) {
                                    return 0;
                                } else {
                                    /* MULXS.W Rs, ERd */
                                    return 4;
                                }
                            default:
                                return 0;
                        }
                    }
                    case 0xD0: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x51:
                                /* DIVXS.B Rs, Rd */
                                return 4;
                            case 0x53:
                                if ((op2 & 0x08) != 0) {
                                    return 0;
                                } else {
                                    /* DIVXS.W Rs, Rd */
                                    return 4;
                                }
                            default:
                                return 0;
                        }
                    }
                    case 0xE0: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x7B:
                                if ((op2 & 0x8F) == 0x0C) {
                                    /* TAS @ERd */
                                    return 4;
                                } else {
                                    return 0;
                                }
                            default:
                                return 0;
                        }
                    }
                    case 0xF0: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x64:
                                if ((op2 & 0x88) != 0) {
                                    return 0;
                                } else {
                                    /* OR.L EERs, Rd */
                                    return 4;
                                }
                            case 0x65:
                                if ((op2 & 0x88) != 0) {
                                    return 0;
                                } else {
                                    /* XOR.L EERs, Rd */
                                    return 4;
                                }
                            case 0x66:
                                if ((op2 & 0x88) != 0) {
                                    return 0;
                                } else {
                                    /* AND.L EERs, Rd */
                                    return 4;
                                }
                            default:
                                return 0;
                        }
                    }
                    default:
                        return 0;
                }
            case 0x02:
                switch (op & 0xF0) {
                    case 0x00: {
                        /* STC.B CCR, Rd */
                        return 2;
                    }
                    case 0x10: {
                        /* STC.B EXR, Rd */
                        return 2;
                    }
                    default:
                        return 0;
                }
            case 0x03:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08:
                        /* LDC.B Rs, CCR */
                        return 2;
                    case 0x10:
                    case 0x18: {
                        /* LDC.B Rs, EXR */
                        return 2;
                    }
                    default:
                        return 0;
                }
            case 0x04:
                /* ORC #xx:8, CCR */
                return 2;
            case 0x05:
                /* XORC #xx:8, CCR */
                return 2;
            case 0x06:
                /* ANDC #xx:8, CCR */
                return 2;
            case 0x07:
                /* LDC.B #xx:8, CCR */
                return 2;
            case 0x08:
                /* ADD.B Rs, Rd */
                return 2;
            case 0x09:
                /* ADD.W Rs, Rd */
                return 2;
            case 0x0A:
                if ((op & 0x88) == 0x80) {
                    /* ADD.L Rs, Rd */
                    return 2;
                } else {
                    switch (op & 0xF0) {
                        case 0x00:
                            /* INC.B Rd */
                            return 2;
                        default:
                            return 0;
                    }
                }
            case 0x0B:
                switch (op & 0xF8) {
                    case 0x00:
                        /* ADDS #1, ERd */
                        return 2;
                    case 0x50:
                    case 0x58:
                        /* INC.W #1, Rd */
                        return 2;
                    case 0x70:
                        /* INC.L #1, ERd */
                        return 2;
                    case 0x80:
                        /* ADDS #2, ERd */
                        return 2;
                    case 0x90:
                        /* ADDS #4, ERd */
                        return 2;
                    case 0xD0:
                    case 0xD8:
                        /* INC.W #2, Rd */
                        return 2;
                    case 0xF0:
                        /* INC.L #2, ERd */
                        return 2;
                    default:
                        return 0;
                }
            case 0x0C:
                /* MOV.B Rs, Rd */
                return 2;
            case 0x0D:
                /* MOV.W Rs, Rd */
                return 2;
            case 0x0E:
                /* ADDX Rs, Rd */
                return 2;
            case 0x0F:
                if ((op & 0x88) == 0x80) {
                    /* MOV.L Rs, Rd */
                    return 2;
                } else if ((op & 0x80) != 0) {
                    return 0;
                } else {
                    switch (op & 0xF0) {
                        case 0x00:
                            /* TODO: DAA */
                            return 2;
                        default:
                            return 0;
                    }
                }
            case 0x10:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08:
                        /* SHLL.B Rd */
                        return 2;
                    case 0x10:
                    case 0x18:
                        /* SHLL.W Rd */
                        return 2;
                    case 0x30:
                        /* SHLL.L Rd */
                        return 2;
                    case 0x40:
                    case 0x48:
                        /* SHLL.B #2, Rd */
                        return 2;
                    case 0x50:
                    case 0x58:
                        /* SHLL.W #2, Rd */
                        return 2;
                    case 0x70:
                        /* SHLL.L #2, Rd */
                        return 2;
                    case 0x80:
                    case 0x88:
                        /* SHAL.B Rd */
                        return 2;
                    case 0x90:
                    case 0x98:
                        /* SHAL.W Rd */
                        return 2;
                    case 0xB0:
                        /* SHAL.L Rd */
                        return 2;
                    case 0xC0:
                    case 0xC8:
                        /* SHAL.B #2, Rd */
                        return 2;
                    case 0xD0:
                    case 0xD8:
                        /* SHAL.W #2, Rd */
                        return 2;
                    case 0xF0:
                        /* SHAL.L #2, Rd */
                        return 2;
                    default:
                        return 0;
                }
            case 0x11:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08:
                        /* SHLR.B Rd */
                        return 2;
                    case 0x10:
                    case 0x18:
                        /* SHLR.W Rd */
                        return 2;
                    case 0x30:
                        /* SHLR.L Rd */
                        return 2;
                    case 0x40:
                    case 0x48:
                        /* SHLR.B #2, Rd */
                        return 2;
                    case 0x50:
                    case 0x58:
                        /* SHLR.W #2, Rd */
                        return 2;
                    case 0x70:
                        /* SHLR.L #2, Rd */
                        return 2;
                    case 0x80:
                    case 0x88:
                        /* SHAR.B Rd */
                        return 2;
                    case 0x90:
                    case 0x98:
                        /* SHAR.W Rd */
                        return 2;
                    case 0xB0:
                        /* SHAR.L Rd */
                        return 2;
                    case 0xC0:
                    case 0xC8:
                        /* SHAR.B #2, Rd */
                        return 2;
                    case 0xD0:
                    case 0xD8:
                        /* SHAR.W #2, Rd */
                        return 2;
                    case 0xF0:
                        /* SHAR.L #2, Rd */
                        return 2;
                    default:
                        return 0;
                }
            case 0x12:
                switch (op & 0xF0) {
                    case 0x00:
                        /* ROTXL.B Rd */
                        return 2;
                    case 0x10:
                        /* ROTXL.W Rd */
                        return 2;
                    case 0x30:
                        if ((op & 0x80) != 0) {
                            return 0;
                        } else {
                            /* ROTXL.L ERd */
                            return 2;
                        }
                    case 0x40:
                        /* ROTXL.B #2, Rd */
                        return 2;
                    case 0x50:
                        /* ROTXL.W #2, Rd */
                        return 2;
                    case 0x70:
                        if ((op & 0x08) != 0) {
                            return 0;
                        } else {
                            /* ROTXL.L #2, ERd */
                            return 2;
                        }
                    case 0x80:
                        /* ROTL.B Rd */
                        return 2;
                    case 0x90:
                        /* ROTL.W Rd */
                        return 2;
                    case 0xB0:
                        if ((op & 0x08) != 0) {
                            return 0;
                        } else {
                            /* ROTL.L ERd */
                            return 2;
                        }
                    case 0xC0:
                        /* ROTL.B #2, Rd */
                        return 2;
                    case 0xD0:
                        /* ROTL.W #2, Rd */
                        return 2;
                    case 0xF0:
                        if ((op & 0x08) != 0) {
                            return 0;
                        } else {
                            /* ROTL.L #2, ERd */
                            return 2;
                        }
                    default:
                        return 0;
                }
            case 0x13:
                switch (op & 0xF0) {
                    case 0x00:
                        /* ROTXR.B Rd */
                        return 2;
                    case 0x10:
                        /* ROTXR.W Rd */
                        return 2;
                    case 0x30:
                        if ((op & 0x08) != 0) {
                            return 0;
                        } else {
                            /* ROTXR.L Rd */
                            return 2;
                        }
                    case 0x40:
                        /* ROTXR.B #2, Rd */
                        return 2;
                    case 0x50:
                        /* ROTXR.W #2, Rd */
                        return 2;
                    case 0x70:
                        if ((op & 0x08) != 0) {
                            return 0;
                        } else {
                            /* ROTXR.L #2, Rd */
                            return 2;
                        }
                    case 0x80:
                        /* ROTR.B Rd */
                        return 2;
                    case 0x90:
                        /* ROTR.W Rd */
                        return 2;
                    case 0xB0: {
                        if ((op & 0x08) != 0) {
                            return 0;
                        } else {
                            /* ROTR.L ERd */
                            return 2;
                        }
                    }
                    case 0xC0:
                        /* ROTR.B #2, Rd */
                        return 2;
                    case 0xD0:
                        /* ROTR.W #2, Rd */
                        return 2;
                    case 0xF0:
                        if ((op & 0x08) != 0) {
                            return 0;
                        } else {
                            /* ROTR.L #2, ERd */
                            return 2;
                        }
                    default:
                        return 0;
                }
            case 0x14:
                /* OR.B Rs, Rd */
                return 2;
            case 0x15:
                /* XOR.B Rs, Rd */
                return 2;
            case 0x16:
                /* AND.B Rs, Rd */
                return 2;
            case 0x17:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08:
                        /* NOT.B Rd */
                        return 2;
                    case 0x10:
                    case 0x18:
                        /* NOT.W Rd */
                        return 2;
                    case 0x30:
                    case 0x38:
                        /* NOT.L Rd */
                        return 2;
                    case 0x50:
                    case 0x58:
                        /* EXTU.W Rd */
                        return 2;
                    case 0x70:
                        /* EXTU.L Rd */
                        return 2;
                    case 0x80:
                    case 0x88:
                        /* NEG.B Rd */
                        return 2;
                    case 0x90:
                    case 0x98:
                        /* NEG.W Rd */
                        return 2;
                    case 0xB0:
                        /* NEG.L ERd */
                        return 2;
                    case 0xD0:
                    case 0xD8:
                        /* EXTS.W Rd */
                        return 2;
                    case 0xF0:
                        /* EXTS.L Rd */
                        return 2;
                    default:
                        return 0;
                }
            case 0x18:
                /* SUB.B Rs, Rd */
                return 2;
            case 0x19:
                /* SUB.W Rs, Rd */
                return 2;
            case 0x1A:
                if ((op & 0x88) == 0x80) {
                    /* SUB.L Rs, Rd */
                    return 2;
                } else {
                    switch (op & 0xF0) {
                        case 0x00:
                            /* DEC.B Rd */
                            return 2;
                        default:
                            return 0;
                    }
                }
            case 0x1B:
                switch (op & 0xF8) {
                    case 0x00:
                        /* SUBS #1, ERd */
                        return 2;
                    case 0x50:
                    case 0x58:
                        /* DEC.W #1, Rd */
                        return 2;
                    case 0x70:
                        /* DEC.L #1, Rd */
                        return 2;
                    case 0x80:
                        /* SUBS #2, ERd */
                        return 2;
                    case 0x90:
                        /* SUBS #2, ERd */
                        return 2;
                    case 0xD0:
                    case 0xD8:
                        /* DEC.W #2, Rd */
                        return 2;
                    case 0xF0:
                        /* DEC.L #2, Rd */
                        return 2;
                    default:
                        return 0;
                }
            case 0x1C:
                /* CMP.B Rs, Rd */
                return 2;
            case 0x1D:
                /* CMP.W Rs, Rd */
                return 2;
            case 0x1E:
                /* SUBX Rs, Rd */
                return 2;
            case 0x1F:
                if ((op & 0x88) == 0x80) {
                    /* CMP.L Rs, Rd */
                    return 2;
                } else if ((op & 0x80) != 0) {
                    return 0;
                } else {
                    switch (op & 0xF0) {
                        case 0x00:
                            /* TODO: DAS */
                            return 2;
                        default:
                            return 0;
                    }
                }
            case 0x20:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x24:
            case 0x25:
            case 0x26:
            case 0x27:
            case 0x28:
            case 0x29:
            case 0x2A:
            case 0x2B:
            case 0x2C:
            case 0x2D:
            case 0x2E:
            case 0x2F:
                /* MOV.B @aa:8, Rd */
                return 2;
            case 0x30:
            case 0x31:
            case 0x32:
            case 0x33:
            case 0x34:
            case 0x35:
            case 0x36:
            case 0x38:
            case 0x39:
            case 0x3A:
            case 0x3B:
            case 0x3C:
            case 0x3D:
            case 0x3E:
            case 0x3F:
                /* MOV.B Rs, @aa:8 */
                return 2;
            case 0x40:
                /* BRA d:8 */
                return 2;
            case 0x41:
                /* BRN d:8 */
                return 2;
            case 0x42:
                /* BHI d:8 */
                return 2;
            case 0x43:
                /* BLS d:8 */
                return 2;
            case 0x44:
                /* BCC d:8 */
                return 2;
            case 0x45:
                /* BCS d:8 */
                return 2;
            case 0x46:
                /* BNE d:8 */
                return 2;
            case 0x47:
                /* BEQ d:8 */
                return 2;
            case 0x48:
                /* BVC d:8 */
                return 2;
            case 0x49:
                /* BVS d:8 */
                return 2;
            case 0x4A:
                /* BPL d:8 */
                return 2;
            case 0x4B:
                /* BMI d:8 */
                return 2;
            case 0x4C:
                /* BGE d:8 */
                return 2;
            case 0x4D:
                /* BLT d:8 */
                return 2;
            case 0x4E:
                /* BGT d:8 */
                return 2;
            case 0x4F:
                /* BLE d:8 */
                return 2;
            case 0x50:
                /* MULXU.B Rs, Rd */
                return 2;
            case 0x51:
                /* DIVXU.B Rs, Rd */
                return 2;
            case 0x52:
                if ((op & 0x08) != 0) {
                    return 0;
                } else {
                    /* MULXU.W Rs, Rd */
                    return 2;
                }
            case 0x53:
                if ((op & 0x08) != 0) {
                    return 0;
                } else {
                    /* DIVXU.W Rs, Rd */
                    return 2;
                }
            case 0x54:
                switch (op & 0xFF) {
                    case 0x70:
                        /* RTS */
                        return 2;
                    default:
                        return 0;
                }
            case 0x55:
                /* BSR d:8 */
                return 2;
            case 0x56:
                switch (op & 0xFF) {
                    case 0x70:
                        /* RTE */
                        return 2;
                    default:
                        return 0;
                }
            case 0x57:
                if ((op & 0xCF) != 0) {
                    return 0;
                } else {
                    /* TRAPA #x:2 */
                    return 2;
                }
            case 0x58:
                switch (op & 0xFF) {
                    case 0x00:
                        /* BRA d:16 */
                        return 4;
                    case 0x10:
                        /* BRN d:16 */
                        return 4;
                    case 0x20:
                        /* BHI d:16 */
                        return 4;
                    case 0x30:
                        /* BLS d:16 */
                        return 4;
                    case 0x40:
                        /* BCC d:16 */
                        return 4;
                    case 0x50:
                        /* BCS d:16 */
                        return 4;
                    case 0x60:
                        /* BNE d:16 */
                        return 4;
                    case 0x70:
                        /* BEQ d:16 */
                        return 4;
                    case 0x80:
                        /* BVC d:16 */
                        return 4;
                    case 0x90:
                        /* BVS d:16 */
                        return 4;
                    case 0xA0:
                        /* BPL d:16 */
                        return 4;
                    case 0xB0:
                        /* BMI d:16 */
                        return 4;
                    case 0xC0:
                        /* BGE d:16 */
                        return 4;
                    case 0xD0:
                        /* BLT d:16 */
                        return 4;
                    case 0xE0:
                        /* BGT d:16 */
                        return 4;
                    case 0xF0:
                        /* BLE d:16 */
                        return 4;
                    default:
                        return 0;
                }
            case 0x59:
                switch (op & 0x8F) {
                    case 0x00:
                        /* JMP @ERn */
                        return 2;
                    default:
                        return 0;
                }
            case 0x5A:
                /* JMP @aa:24 */
                return 4;
            case 0x5C:
                switch (op & 0xFF) {
                    case 0x00:
                        /* BSR d:16 */
                        return 4;
                    default:
                        return 0;
                }
            case 0x5D:
                switch (op & 0x8F) {
                    case 0x00:
                        /* JSR @ERn */
                        return 2;
                    default:
                        return 0;
                }
            case 0x5E:
                /* JSR @aa:24 */
                return 4;
            case 0x60:
                /* BSET Rn, Rd */
                return 2;
            case 0x61:
                /* BNOT Rn, Rd */
                return 2;
            case 0x62:
                /* BCLR Rn, Rd */
                return 2;
            case 0x63:
                /* BTST Rn, Rd */
                return 2;
            case 0x64:
                /* OR.W Rs, Rd */
                return 2;
            case 0x65:
                /* XOR.W Rs, Rd */
                return 2;
            case 0x66:
                /* AND.W Rs, Rd */
                return 2;
            case 0x67:
                if ((op & 0x80) != 0) {
                    /* BIST #xx:3, Rd */
                    return 2;
                } else {
                    /* BST #xx:3, Rd */
                    return 2;
                }
            case 0x68:
                if ((op & 0x80) != 0) {
                    /* MOV.B Rs, @ERd */
                    return 2;
                } else {
                    /* MOV.B @ERs, Rd */
                    return 2;
                }
            case 0x69:
                if ((op & 0x80) != 0) {
                    /* MOV.W Rs, @ERd */
                    return 2;
                } else {
                    /* MOV.W @ERs, Rd */
                    return 2;
                }
            case 0x6A:
                switch (op & 0xF8) {
                    case 0x00:
                    case 0x08:
                        /* MOV.B @aa:16, Rd */
                        return 4;
                    case 0x10: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(4));
                        switch (op2 >> 8) {
                            case 0x63:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BTST Rn, @aa:16 */
                                    return 6;
                                }
                            case 0x73:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BTST #xx:3, @aa:16 */
                                    return 6;
                                }
                            case 0x74:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIOR #xx:3, @aa:16 */
                                            return 6;
                                        } else {
                                            /* BOR #xx:3, @aa:16 */
                                            return 6;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x75:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIXOR #xx:3, @aa:16 */
                                            return 6;
                                        } else {
                                            /* BXOR #xx:3, @aa:16 */
                                            return 6;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x76:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIAND #xx:3, @aa:16 */
                                            return 6;
                                        } else {
                                            /* BAND #xx:3, @aa:16 */
                                            return 6;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x77:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BILD #xx:3, @aa:16 */
                                            return 6;
                                        } else {
                                            /* BLD #xx:3, @aa:16 */
                                            return 6;
                                        }
                                    default:
                                        return 0;
                                }
                            default:
                                return 0;
                        }
                    }
                    case 0x18: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(4));
                        switch (op2 >> 8) {
                            case 0x60:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BSET Rn, @aa:16 */
                                    return 6;
                                }
                            case 0x61:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BNOT Rn, @aa:16 */
                                    return 6;
                                }
                            case 0x62:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BCLR Rn, @aa:16 */
                                    return 6;
                                }
                            case 0x67:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIST #xx:3, @aa:16 */
                                            return 6;
                                        } else {
                                            /* BST #xx:3, @aa:16 */
                                            return 6;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x70:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BSET #xx:3, @aa:16 */
                                    return 6;
                                }
                            case 0x71:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BNOT #xx:3, @aa:16 */
                                    return 6;
                                }
                            case 0x72:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BCLR #xx:3, @aa:16 */
                                    return 6;
                                }
                            default:
                                return 0;
                        }
                    }
                    case 0x20:
                    case 0x28:
                        /* MOV.B @aa:32, Rd */
                        return 6;
                    case 0x30: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(6));
                        switch (op2 >> 8) {
                            case 0x63:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BTST Rn, @aa:32 */
                                    return 8;
                                }
                            case 0x73:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BTST #xx:3, @aa:32 */
                                    return 8;
                                }
                            case 0x74:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIOR #xx:3, @aa:32 */
                                            return 8;
                                        } else {
                                            /* BOR #xx:3, @aa:32 */
                                            return 8;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x75:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIXOR #xx:3, @aa:32 */
                                            return 8;
                                        } else {
                                            /* BXOR #xx:3, @aa:32 */
                                            return 8;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x76:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIAND #xx:3, @aa:32 */
                                            return 8;
                                        } else {
                                            /* BAND #xx:3, @aa:32 */
                                            return 8;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x77:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BILD #xx:3, @aa:32 */
                                            return 8;
                                        } else {
                                            /* BLD #xx:3, @aa:32 */
                                            return 8;
                                        }
                                    default:
                                        return 0;
                                }
                            default:
                                return 0;
                        }
                    }
                    case 0x38: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(6));
                        switch (op2 >> 8) {
                            case 0x60:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BSET Rn, @aa:32 */
                                    return 8;
                                }
                            case 0x61:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BNOT Rn, @aa:32 */
                                    return 8;
                                }
                            case 0x62:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BCLR Rn, @aa:32 */
                                    return 8;
                                }
                            case 0x67:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIST #xx:3, @aa:32 */
                                            return 8;
                                        } else {
                                            /* BST #xx:3, @aa:32 */
                                            return 8;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x70:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BSET #xx:3, @aa:32 */
                                    return 8;
                                }
                            case 0x71:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BNOT #xx:3, @aa:32 */
                                    return 8;
                                }
                            case 0x72:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BCLR #xx:3, @aa:32 */
                                    return 8;
                                }
                            default:
                                return 0;
                        }
                    }
                    case 0x40:
                    case 0x48:
                        /* MOVFPE @aa:16, Rd */
                        return 4;
                    case 0x80:
                    case 0x88:
                        /* MOV.B Rs, @aa:16 */
                        return 4;
                    case 0xA0:
                    case 0xA8:
                        /* MOV.B Rs, @aa:32 */
                        return 6;
                    case 0xC0:
                    case 0xC8:
                        /* MOVTPE Rs, @aa:16 */
                        return 4;
                    default:
                        return 0;
                }
            case 0x6B:
                switch (op & 0xF0) {
                    case 0x00:
                        /* MOV.W @aa:16, Rd */
                        return 4;
                    case 0x20:
                        /* MOV.W @aa:32, Rd */
                        return 6;
                    case 0x80:
                        /* MOV.W Rs, @aa:16 */
                        return 4;
                    case 0xA0:
                        /* MOV.W Rs, @aa:32 */
                        return 6;
                    default:
                        return 0;
                }
            case 0x6C:
                if ((op & 0x80) != 0) {
                    /* MOV.B Rs, @-ERd */
                    return 2;
                } else {
                    /* MOV.B @ERs+, Rd */
                    return 2;
                }
            case 0x6D:
                if ((op & 0x80) != 0) {
                    /* MOV.W @-ERs, Rd */
                    return 2;
                } else {
                    /* MOV.W @ERs+, Rd */
                    return 2;
                }
            case 0x6E:
                if ((op & 0x80) != 0) {
                    /* MOV.B Rs, @(d:16, ERd) */
                    return 4;
                } else {
                    /* MOV.B @(d:16, ERs), Rd */
                    return 4;
                }
            case 0x6F:
                if ((op & 0x80) != 0) {
                    /* MOV.W Rs, @(d:16, ERd) */
                    return 4;
                } else {
                    /* MOV.W @(d:16, ERs), Rd */
                    return 4;
                }
            case 0x70:
                if ((op & 0x80) != 0) {
                    return 0;
                } else {
                    /* BSET #xx:3, Rd */
                    return 2;
                }
            case 0x71:
                if ((op & 0x80) != 0) {
                    return 0;
                } else {
                    /* BNOT #xx:3, Rd */
                    return 2;
                }
            case 0x72:
                if ((op & 0x80) != 0) {
                    return 0;
                } else {
                    /* BCLR #xx:3, Rd */
                    return 2;
                }
            case 0x73:
                if ((op & 0x80) != 0) {
                    return 0;
                } else {
                    /* BTST #xx:3, Rd */
                    return 2;
                }
            case 0x74:
                if ((op & 0x80) != 0) {
                    /* BIOR #xx:3, Rd */
                    return 2;
                } else {
                    /* BOR #xx:3, Rd */
                    return 2;
                }
            case 0x75:
                if ((op & 0x80) != 0) {
                    /* BIXOR #xx:3, Rd */
                    return 2;
                } else {
                    /* BXOR #xx:3, Rd */
                    return 2;
                }
            case 0x76:
                if ((op & 0x80) != 0) {
                    /* BIAND #xx:3, Rd */
                    return 2;
                } else {
                    /* BAND #xx:3, Rd */
                    return 2;
                }
            case 0x77:
                if ((op & 0x80) != 0) {
                    /* BILD #xx:3, Rd */
                    return 2;
                } else {
                    /* BLD #xx:3, Rd */
                    return 2;
                }
            case 0x78:
                if ((op & 0x80) != 0) {
                    return 0;
                } else {
                    int op2 = Short.toUnsignedInt(reader.peekI16(2));
                    switch (op2 >> 8) {
                        case 0x6A:
                            switch (op2 & 0xF0) {
                                case 0x20: {
                                    if ((op & 0x0F) == 0x00) {
                                        /* MOV.B @(d:32, ERs), Rd */
                                        return 8;
                                    } else {
                                        return 0;
                                    }
                                }
                                case 0xA0: {
                                    if ((op & 0x0F) == 0x00) {
                                        /* MOV.B Rs, @(d:32, ERd) */
                                        return 8;
                                    } else {
                                        return 0;
                                    }
                                }
                                default:
                                    return 0;
                            }
                        case 0x6B:
                            switch (op2 & 0xF0) {
                                case 0x20: {
                                    if ((op & 0x0F) == 0x00) {
                                        /* MOV.W @(d:32, ERs), Rd */
                                        return 8;
                                    } else {
                                        return 0;
                                    }
                                }
                                case 0xA0: {
                                    if ((op & 0x0F) == 0x00) {
                                        /* MOV.W Rd, @(d:32, ERs) */
                                        return 8;
                                    } else {
                                        return 0;
                                    }
                                }
                                default:
                                    return 0;
                            }
                        default:
                            return 0;
                    }
                }
            case 0x79:
                switch (op & 0xF0) {
                    case 0x00:
                        /* MOV.W #xx:16, Rd */
                        return 4;
                    case 0x10:
                        /* ADD.W #xx:16, Rd */
                        return 4;
                    case 0x20:
                        /* CMP.W #xx:16, Rd */
                        return 4;
                    case 0x30:
                        /* SUB.W #xx:16, Rd */
                        return 4;
                    case 0x40:
                        /* OR.W #xx:16, Rd */
                        return 4;
                    case 0x50:
                        /* XOR.W #xx:16, Rd */
                        return 4;
                    case 0x60:
                        /* AND.W #xx:16, Rd */
                        return 4;
                    default:
                        return 0;
                }
            case 0x7A:
                switch (op & 0xF8) {
                    case 0x00:
                        /* MOV.L #xx:32, ERd */
                        return 6;
                    case 0x10:
                        /* ADD.L #xx:32, Rd */
                        return 6;
                    case 0x20:
                        /* CMP.L #xx:32, Rd */
                        return 6;
                    case 0x30:
                        /* SUB.L #xx:32, Rd */
                        return 6;
                    case 0x40:
                        /* OR.L #xx:32, ERd */
                        return 6;
                    case 0x50:
                        /* XOR.L #xx:32, ERd */
                        return 6;
                    case 0x60:
                        /* AND.L #xx:32, ERd */
                        return 6;
                    default:
                        return 0;
                }
            case 0x7B:
                switch (op & 0xFF) {
                    case 0x5C: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        if (op2 == 0x598F) {
                            /* EEPMOV.B */
                            return 4;
                        } else {
                            return 0;
                        }
                    }
                    case 0xD4: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        if (op2 == 0x598F) {
                            /* EEPMOV.W */
                            return 4;
                        } else {
                            return 0;
                        }
                    }
                    default:
                        return 0;
                }
            case 0x7C:
                switch (op & 0x8F) {
                    case 0x00: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x63:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BTST Rn, @ERd */
                                    return 4;
                                }
                            case 0x73:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BTST #xx:3, @ERd */
                                    return 4;
                                }
                            case 0x74:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIOR #xx:3, @ERd */
                                            return 4;
                                        } else {
                                            /* BOR #xx:3, @ERd */
                                            return 4;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x75:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIXOR #xx:3, @ERd */
                                            return 4;
                                        } else {
                                            /* BXOR #xx:3, @ERd */
                                            return 4;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x76:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIAND #xx:3, @ERd */
                                            return 4;
                                        } else {
                                            /* BAND #xx:3, @ERd */
                                            return 4;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x77:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BILD #xx:3, @ERd */
                                            return 4;
                                        } else {
                                            /* BLD #xx:3, @ERd */
                                            return 4;
                                        }
                                    default:
                                        return 0;
                                }
                            default:
                                return 0;
                        }
                    }
                    default:
                        return 0;
                }
            case 0x7D:
                switch (op & 0x8F) {
                    case 0x00: {
                        int op2 = Short.toUnsignedInt(reader.peekI16(2));
                        switch (op2 >> 8) {
                            case 0x60:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BSET Rn, @ERd */
                                    return 4;
                                }
                            case 0x61:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BNOT Rn, @ERd */
                                    return 4;
                                }
                            case 0x62:
                                if ((op2 & 0x0F) != 0) {
                                    return 0;
                                } else {
                                    /* BCLR Rn, @ERd */
                                    return 4;
                                }
                            case 0x67:
                                switch (op2 & 0x0F) {
                                    case 0x00:
                                        if ((op2 & 0x80) != 0) {
                                            /* BIST #xx:3, @ERd */
                                            return 4;
                                        } else {
                                            /* BST #xx:3, @ERd */
                                            return 4;
                                        }
                                    default:
                                        return 0;
                                }
                            case 0x70:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BSET #xx:3, @ERd */
                                    return 4;
                                }
                            case 0x71:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BNOT #xx:3, @ERd */
                                    return 4;
                                }
                            case 0x72:
                                if ((op2 & 0x8F) != 0) {
                                    return 0;
                                } else {
                                    /* BCLR #xx:3, @ERd */
                                    return 4;
                                }
                            default:
                                return 0;
                        }
                    }
                    default:
                        return 0;
                }
            case 0x7E: {
                int op2 = Short.toUnsignedInt(reader.peekI16(2));
                switch (op2 >> 8) {
                    case 0x63:
                        if ((op2 & 0x0F) != 0) {
                            return 0;
                        } else {
                            /* BTST Rn, @aa:8 */
                            return 4;
                        }
                    case 0x73:
                        if ((op2 & 0x8F) != 0) {
                            return 0;
                        } else {
                            /* BTST #xx:3, @aa:8 */
                            return 4;
                        }
                    case 0x74:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BIOR #xx:3, @aa:8 */
                                    return 4;
                                } else {
                                    /* BOR #xx:3, @aa:8 */
                                    return 4;
                                }
                            default:
                                return 0;
                        }
                    case 0x75:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BIXOR #xx:3, @aa:8 */
                                    return 4;
                                } else {
                                    /* BXOR #xx:3, @aa:8 */
                                    return 4;
                                }
                            default:
                                return 0;
                        }
                    case 0x76:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BIAND #xx:3, @aa:8 */
                                    return 4;
                                } else {
                                    /* BAND #xx:3, @aa:8 */
                                    return 4;
                                }
                            default:
                                return 0;
                        }
                    case 0x77:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BILD #xx:3, @aa:8 */
                                    return 4;
                                } else {
                                    /* BLD #xx:3, @aa:8 */
                                    return 4;
                                }
                            default:
                                return 0;
                        }
                    default:
                        return 0;
                }
            }
            case 0x7F: {
                int op2 = Short.toUnsignedInt(reader.peekI16(2));
                switch (op2 >> 8) {
                    case 0x60:
                        if ((op2 & 0x0F) != 0) {
                            return 0;
                        } else {
                            /* BSET Rn, @aa:8 */
                            return 4;
                        }
                    case 0x61:
                        if ((op2 & 0x0F) != 0) {
                            return 0;
                        } else {
                            /* BNOT Rn, @aa:8 */
                            return 4;
                        }
                    case 0x62:
                        if ((op2 & 0x0F) != 0) {
                            return 0;
                        } else {
                            /* BCLR Rn, @aa:8 */
                            return 4;
                        }
                    case 0x67:
                        switch (op2 & 0x0F) {
                            case 0x00:
                                if ((op2 & 0x80) != 0) {
                                    /* BIST #xx:3, @aa:8 */
                                    return 4;
                                } else {
                                    /* BST #xx:3, @aa:8 */
                                    return 4;
                                }
                            default:
                                return 0;
                        }
                    case 0x70:
                        if ((op2 & 0x8F) != 0) {
                            return 0;
                        } else {
                            /* BSET #xx:3, @aa:8 */
                            return 4;
                        }
                    case 0x71:
                        if ((op2 & 0x8F) != 0) {
                            return 0;
                        } else {
                            /* BNOT #xx:3, @aa:8 */
                            return 4;
                        }
                    case 0x72:
                        if ((op2 & 0x8F) != 0) {
                            return 0;
                        } else {
                            /* BCLR #xx:3, @aa:8 */
                            return 4;
                        }
                    default:
                        return 0;
                }
            }
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8A:
            case 0x8B:
            case 0x8C:
            case 0x8D:
            case 0x8E:
            case 0x8F:
                /* ADD.B #xx:8, Rd */
                return 2;
            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9A:
            case 0x9B:
            case 0x9C:
            case 0x9D:
            case 0x9E:
            case 0x9F:
                /* ADDX #xx:8, Rd */
                return 2;
            case 0xA0:
            case 0xA1:
            case 0xA2:
            case 0xA3:
            case 0xA4:
            case 0xA5:
            case 0xA6:
            case 0xA7:
            case 0xA8:
            case 0xA9:
            case 0xAA:
            case 0xAB:
            case 0xAC:
            case 0xAD:
            case 0xAE:
            case 0xAF:
                /* CMP.B #xx:8, Rd */
                return 2;
            case 0xB0:
            case 0xB1:
            case 0xB2:
            case 0xB3:
            case 0xB4:
            case 0xB5:
            case 0xB6:
            case 0xB7:
            case 0xB8:
            case 0xB9:
            case 0xBA:
            case 0xBB:
            case 0xBC:
            case 0xBD:
            case 0xBE:
            case 0xBF:
                /* SUBX #xx:8, Rd */
                return 2;
            case 0xC0:
            case 0xC1:
            case 0xC2:
            case 0xC3:
            case 0xC4:
            case 0xC5:
            case 0xC6:
            case 0xC7:
            case 0xC8:
            case 0xC9:
            case 0xCA:
            case 0xCB:
            case 0xCC:
            case 0xCD:
            case 0xCE:
            case 0xCF:
                /* OR.B #xx:8, Rd */
                return 2;
            case 0xD0:
            case 0xD1:
            case 0xD2:
            case 0xD3:
            case 0xD4:
            case 0xD5:
            case 0xD6:
            case 0xD7:
            case 0xD8:
            case 0xD9:
            case 0xDA:
            case 0xDB:
            case 0xDC:
            case 0xDD:
            case 0xDE:
            case 0xDF:
                /* XOR.B #xx:8, Rd */
                return 2;
            case 0xE0:
            case 0xE1:
            case 0xE2:
            case 0xE3:
            case 0xE4:
            case 0xE5:
            case 0xE6:
            case 0xE7:
            case 0xE8:
            case 0xE9:
            case 0xEA:
            case 0xEB:
            case 0xEC:
            case 0xED:
            case 0xEE:
            case 0xEF:
                /* AND.B #xx:8, Rd */
                return 2;
            case 0xF0:
            case 0xF1:
            case 0xF2:
            case 0xF3:
            case 0xF4:
            case 0xF5:
            case 0xF6:
            case 0xF7:
            case 0xF8:
            case 0xF9:
            case 0xFA:
            case 0xFB:
            case 0xFC:
            case 0xFD:
            case 0xFE:
            case 0xFF:
                /* MOV.B #xx:8, Rd */
                return 2;
            default:
                return 0;
        }
    }
}
