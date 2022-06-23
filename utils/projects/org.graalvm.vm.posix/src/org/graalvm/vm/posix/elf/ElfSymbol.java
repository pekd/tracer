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
package org.graalvm.vm.posix.elf;

import org.graalvm.vm.util.io.Endianess;

public class ElfSymbol extends SectionData implements Symbol {
    private int st_name;
    private long st_value;
    private long st_size;
    private byte st_info;
    private byte st_other;
    private short st_shndx;

    private int index;

    public ElfSymbol(Section section, int index) {
        super(section);
        this.index = index;

        byte[] data = elf.getData();
        int offset = (int) (index * section.sh_entsize + section.getOffset());
        if (elf.ei_class == Elf.ELFCLASS32) {
            if (elf.ei_data == Elf.ELFDATA2LSB) {
                st_name = Endianess.get32bitLE(data, offset);
                st_value = Endianess.get32bitLE(data, offset + 0x04);
                st_size = Endianess.get32bitLE(data, offset + 0x08);
                st_info = data[offset + 0x0C];
                st_other = data[offset + 0x0D];
                st_shndx = Endianess.get16bitLE(data, offset + 0x0E);
            } else if (elf.ei_data == Elf.ELFDATA2MSB) {
                st_name = Endianess.get32bitBE(data, offset);
                st_value = Endianess.get32bitBE(data, offset + 0x04);
                st_size = Endianess.get32bitBE(data, offset + 0x08);
                st_info = data[offset + 0x0C];
                st_other = data[offset + 0x0D];
                st_shndx = Endianess.get16bitBE(data, offset + 0x0E);
            } else {
                throw new IllegalArgumentException("unknown ei_data: " + elf.ei_data);
            }
        } else if (elf.ei_class == Elf.ELFCLASS64) {
            if (elf.ei_data == Elf.ELFDATA2LSB) {
                st_name = Endianess.get32bitLE(data, offset);
                st_info = data[offset + 0x04];
                st_other = data[offset + 0x05];
                st_shndx = Endianess.get16bitLE(data, offset + 0x06);
                st_value = Endianess.get64bitLE(data, offset + 0x08);
                st_size = Endianess.get64bitLE(data, offset + 0x10);
            } else if (elf.ei_data == Elf.ELFDATA2MSB) {
                st_name = Endianess.get32bitBE(data, offset);
                st_info = data[offset + 0x04];
                st_other = data[offset + 0x05];
                st_shndx = Endianess.get16bitBE(data, offset + 0x06);
                st_value = Endianess.get64bitBE(data, offset + 0x08);
                st_size = Endianess.get64bitBE(data, offset + 0x10);
            }
        } else {
            throw new IllegalArgumentException("unknown ei_class: " + elf.ei_class);
        }
    }

    protected ElfSymbol(ElfSymbol sym) {
        super(sym.section);
        this.index = sym.index;
        this.st_name = sym.st_name;
        this.st_value = sym.st_value;
        this.st_size = sym.st_size;
        this.st_info = sym.st_info;
        this.st_other = sym.st_other;
        this.st_shndx = sym.st_shndx;
    }

    @Override
    public String getName() {
        StringTable strtab = section.getLink();
        return strtab.getString(st_name);
    }

    @Override
    public int getBind() {
        return st_info >>> 4;
    }

    @Override
    public int getType() {
        return st_info & 0xf;
    }

    @Override
    public int getVisibility() {
        return st_other & 0x3;
    }

    @Override
    public long getSize() {
        return st_size;
    }

    @Override
    public long getValue() {
        return st_value;
    }

    @Override
    public short getSectionIndex() {
        return st_shndx;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public ElfSymbol offset(long off) {
        ElfSymbol sym = new ElfSymbol(this);
        sym.st_value += off;
        return sym;
    }

    @Override
    public String toString() {
        return String.format("Symbol[%s=0x%x]", getName(), getValue());
    }
}
