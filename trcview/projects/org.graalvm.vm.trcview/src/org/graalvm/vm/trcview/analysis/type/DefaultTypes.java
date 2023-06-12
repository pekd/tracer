package org.graalvm.vm.trcview.analysis.type;

import java.util.HashMap;
import java.util.Map;

public class DefaultTypes {
    public static final Type CODE_TYPE = new Type(DataType.CODE);

    private static final Map<Integer, Type> DEFAULT_CODE_TYPES = new HashMap<>();

    public static Type getCodeType(int length) {
        Type type = DEFAULT_CODE_TYPES.get(length);
        if (type == null) {
            type = new Type(DataType.CODE, true, length);
            DEFAULT_CODE_TYPES.put(length, type);
        }
        return type;
    }

    public static void populate(UserTypeDatabase db, ArchitectureTypeInfo info) {
        // ELF32
        Struct elf32_ehdr = new Struct("Elf32_Ehdr");
        elf32_ehdr.add("e_ident", new Type(DataType.U8, false, 16, Representation.HEX));
        elf32_ehdr.add("e_type", new Type(DataType.U16, Representation.HEX));
        elf32_ehdr.add("e_machine", new Type(DataType.U16));
        elf32_ehdr.add("e_version", new Type(DataType.U32));
        elf32_ehdr.add("e_entry", new Type(new Type(DataType.VOID), info));
        elf32_ehdr.add("e_phoff", new Type(DataType.U32, Representation.HEX));
        elf32_ehdr.add("e_shoff", new Type(DataType.U32, Representation.HEX));
        elf32_ehdr.add("e_flags", new Type(DataType.U32, Representation.HEX));
        elf32_ehdr.add("e_ehsize", new Type(DataType.U16));
        elf32_ehdr.add("e_phentsize", new Type(DataType.U16));
        elf32_ehdr.add("e_phnum", new Type(DataType.U16));
        elf32_ehdr.add("e_shentsize", new Type(DataType.U16));
        elf32_ehdr.add("e_shnum", new Type(DataType.U16));
        elf32_ehdr.add("e_shstrndx", new Type(DataType.U16));
        add(db, elf32_ehdr);

        Struct elf32_shdr = new Struct("Elf32_Shdr");
        elf32_shdr.add("sh_name", new Type(DataType.U32));
        elf32_shdr.add("sh_type", new Type(DataType.U32, Representation.HEX));
        elf32_shdr.add("sh_flags", new Type(DataType.U32, Representation.HEX));
        elf32_shdr.add("sh_addr", new Type(new Type(DataType.VOID), info));
        elf32_shdr.add("sh_offset", new Type(DataType.U32, Representation.HEX));
        elf32_shdr.add("sh_size", new Type(DataType.U32, Representation.HEX));
        elf32_shdr.add("sh_link", new Type(DataType.U32, Representation.HEX));
        elf32_shdr.add("sh_info", new Type(DataType.U32, Representation.HEX));
        elf32_shdr.add("sh_addralign", new Type(DataType.U32, Representation.HEX));
        elf32_shdr.add("sh_entsize", new Type(DataType.U32));
        add(db, elf32_shdr);

        Struct elf32_phdr = new Struct("Elf32_Phdr");
        elf32_phdr.add("p_type", new Type(DataType.U32, Representation.HEX));
        elf32_phdr.add("p_offset", new Type(DataType.U32, Representation.HEX));
        elf32_phdr.add("p_vaddr", new Type(new Type(DataType.VOID), info));
        elf32_phdr.add("p_paddr", new Type(new Type(DataType.VOID), info));
        elf32_phdr.add("p_filesz", new Type(DataType.U32, Representation.HEX));
        elf32_phdr.add("p_memsz", new Type(DataType.U32, Representation.HEX));
        elf32_phdr.add("p_flags", new Type(DataType.U32, Representation.HEX));
        elf32_phdr.add("p_align", new Type(DataType.U32, Representation.HEX));
        add(db, elf32_phdr);

        Struct elf32_rela = new Struct("Elf32_Rela");
        elf32_rela.add("r_offset", new Type(DataType.U32, Representation.HEX));
        elf32_rela.add("r_info", new Type(DataType.U32, Representation.HEX));
        elf32_rela.add("r_addend", new Type(DataType.U32, Representation.HEX));
        add(db, elf32_rela);

        Struct elf32_dyn = new Struct("Elf32_Dyn");
        elf32_dyn.add("d_tag", new Type(DataType.U32, Representation.HEX));
        elf32_dyn.add("d_un", new Type(DataType.U32, Representation.HEX));
        add(db, elf32_dyn);

        // ELF64
        Struct elf64_ehdr = new Struct("Elf64_Ehdr");
        elf64_ehdr.add("e_ident", new Type(DataType.U8, false, 16, Representation.HEX));
        elf64_ehdr.add("e_type", new Type(DataType.U16, Representation.HEX));
        elf64_ehdr.add("e_machine", new Type(DataType.U16));
        elf64_ehdr.add("e_version", new Type(DataType.U32));
        elf64_ehdr.add("e_entry", new Type(new Type(DataType.VOID), info));
        elf64_ehdr.add("e_phoff", new Type(DataType.U64, Representation.HEX));
        elf64_ehdr.add("e_shoff", new Type(DataType.U64, Representation.HEX));
        elf64_ehdr.add("e_flags", new Type(DataType.U32, Representation.HEX));
        elf64_ehdr.add("e_ehsize", new Type(DataType.U16));
        elf64_ehdr.add("e_phentsize", new Type(DataType.U16));
        elf64_ehdr.add("e_phnum", new Type(DataType.U16));
        elf64_ehdr.add("e_shentsize", new Type(DataType.U16));
        elf64_ehdr.add("e_shnum", new Type(DataType.U16));
        elf64_ehdr.add("e_shstrndx", new Type(DataType.U16));
        add(db, elf64_ehdr);

        Struct elf64_shdr = new Struct("Elf64_Shdr");
        elf64_shdr.add("sh_name", new Type(DataType.U32));
        elf64_shdr.add("sh_type", new Type(DataType.U32, Representation.HEX));
        elf64_shdr.add("sh_flags", new Type(DataType.U64, Representation.HEX));
        elf64_shdr.add("sh_addr", new Type(new Type(DataType.VOID), info));
        elf64_shdr.add("sh_offset", new Type(DataType.U64, Representation.HEX));
        elf64_shdr.add("sh_size", new Type(DataType.U64, Representation.HEX));
        elf64_shdr.add("sh_link", new Type(DataType.U32, Representation.HEX));
        elf64_shdr.add("sh_info", new Type(DataType.U32, Representation.HEX));
        elf64_shdr.add("sh_addralign", new Type(DataType.U64, Representation.HEX));
        elf64_shdr.add("sh_entsize", new Type(DataType.U64));
        add(db, elf64_shdr);

        Struct elf64_phdr = new Struct("Elf64_Phdr");
        elf64_phdr.add("p_type", new Type(DataType.U32, Representation.HEX));
        elf64_phdr.add("p_flags", new Type(DataType.U32, Representation.HEX));
        elf64_phdr.add("p_offset", new Type(DataType.U64, Representation.HEX));
        elf64_phdr.add("p_vaddr", new Type(new Type(DataType.VOID), info));
        elf64_phdr.add("p_paddr", new Type(new Type(DataType.VOID), info));
        elf64_phdr.add("p_filesz", new Type(DataType.U64, Representation.HEX));
        elf64_phdr.add("p_memsz", new Type(DataType.U64, Representation.HEX));
        elf64_phdr.add("p_align", new Type(DataType.U64, Representation.HEX));
        add(db, elf64_phdr);

        Struct elf64_rela = new Struct("Elf64_Rela");
        elf64_rela.add("r_offset", new Type(DataType.U64, Representation.HEX));
        elf64_rela.add("r_info", new Type(DataType.U64, Representation.HEX));
        elf64_rela.add("r_addend", new Type(DataType.U64, Representation.HEX));
        add(db, elf64_rela);

        Struct elf64_dyn = new Struct("Elf64_Dyn");
        elf64_dyn.add("d_tag", new Type(DataType.U64, Representation.HEX));
        elf64_dyn.add("d_un", new Type(DataType.U64, Representation.HEX));
        add(db, elf64_dyn);

        // various other types
        Struct vecfx32 = new Struct("VecFx32");
        vecfx32.add("x", new Type(DataType.FX32));
        vecfx32.add("y", new Type(DataType.FX32));
        vecfx32.add("z", new Type(DataType.FX32));
        add(db, vecfx32);
    }

    private static void add(UserTypeDatabase db, UserDefinedType type) {
        try {
            db.add(type);
        } catch (NameAlreadyUsedException e) {
            // swallow
        }
    }
}
