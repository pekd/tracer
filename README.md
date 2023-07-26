tracer
======

This repository contains multiple subprojects:

- trcview: architecture agnostic execution trace analysis tool
- vmx86: AMD64 interpreter based on GraalVM
- utils: library with ELF file parser, Linux syscall emulation, and various
  other utility functions


Build Dependencies
------------------

- git
- JDK
- [mx](https://github.com/graalvm/mx)
- Python (required by mx)

vmx86 also requires an installation of a Labs JDK as well as the
[GraalVM](https://github.com/oracle/graal) source repository.
