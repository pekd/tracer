#
# Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# The Universal Permissive License (UPL), Version 1.0
#
# Subject to the condition set forth below, permission is hereby granted to any
# person obtaining a copy of this software, associated documentation and/or
# data (collectively the "Software"), free of charge and under any and all
# copyright rights in the Software, and any and all patent rights owned or
# freely licensable by each licensor hereunder covering either (i) the
# unmodified Software as contributed to or provided by such licensor, or (ii)
# the Larger Works (as defined below), to deal in both
#
# (a) the Software, and
#
# (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
# one is included with the Software each a "Larger Work" to which the Software
# is contributed by such licensors),
#
# without restriction, including without limitation the rights to copy, create
# derivative works of, display, perform, and distribute the Software and make,
# use, sell, offer for sale, import, export, have made, and have sold the
# Software and the Larger Work(s), and to sublicense the foregoing rights on
# either these or other terms.
#
# This license is subject to the following condition:
#
# The above copyright notice and either this complete permission notice or at a
# minimum a reference to the UPL must be included in all copies or substantial
# portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#
suite = {
  "mxversion" : "5.223.0",
  "name" : "vmx86",
  "versionConflictResolution" : "latest",

  "imports" : {
    "suites" : [
      {
        "name" : "truffle",
        "subdir" : True,
        "version" : "6360ce65914290b214f3a939bc0eff3281f1f9a0",
        "urls" : [
          {"url" : "https://github.com/oracle/graal", "kind" : "git"},
          {"url" : "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind" : "binary"},
        ]
      },
      {
        "name" : "substratevm",
        "subdir" : True,
        "version" : "6360ce65914290b214f3a939bc0eff3281f1f9a0",
        "urls" : [
          {"url" : "https://github.com/oracle/graal", "kind" : "git"},
          {"url" : "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind" : "binary"},
        ]
      },
      {
        "name" : "utils",
        "subdir" : True,
        "urls" : [
          {"url" : "https://github.com/pekd/tracer", "kind" : "git"},
        ]
      },
      {
        "name" : "trcview",
        "subdir" : True,
        "urls" : [
          {"url" : "https://github.com/pekd/tracer", "kind" : "git"},
        ]
      }
    ]
  },

  "javac.lint.overrides" : "none",

  "licenses" : {
    "UPL" : {
      "name" : "Universal Permissive License, Version 1.0",
      "url" : "http://opensource.org/licenses/UPL",
    },
    "LGPL-2.1" : {
      "name" : "GNU Lesser General Public License version 2.1",
      "url" : "http://opensource.org/licenses/LGPL-2.1",
    }
  },

  "libraries" : {
    "XED" : {
      # source code: https://github.com/intelxed/xed
      "urls" : [
        "https://lafo.ssw.jku.at/pub/graal-external-deps/xed-install-base-2018-06-29-lin-x86-64.tar.gz"
      ],
      "sha1" : "ec6ada3f0f3c8c71b57724b3d841d21d6c57fa67",
      "license" : "Apache-2.0",
    }
  },

  "defaultLicense" : "UPL",

  "projects" : {
    "org.graalvm.vm.memory" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "jniHeaders" : True,
      "dependencies" : [
        "utils:CORE",
        "utils:POSIX",
        "truffle:TRUFFLE_API",
      ],
      "javaCompliance" : "17+",
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
      "workingSets" : "vmx86",
      "license" : "UPL",
    },

    "org.graalvm.vm.memory.svm" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.memory",
        "substratevm:SVM"
      ],
      "javaCompliance" : "17+",
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
      "workingSets" : "vmx86",
      "license" : "UPL",
    },

    "org.graalvm.vm.memory.native" : {
      "subDir" : "projects",
      "native" : True,
      "vpath" : True,
      "results" : [
        "bin/<lib:memory>",
        "bin/libmemory.a"
      ],
      "buildDependencies" : [
        "org.graalvm.vm.memory",
        "XED"
      ],
      "buildEnv" : {
        "CFLAGS" : "-I<jnigen:org.graalvm.vm.memory>",
        "LIBMEMORY" : "<lib:memory>",
        "XED" : "<path:XED>",
        "XED_VERSION" : "xed-install-base-2018-06-29-lin-x86-64",
        "OS" : "<os>",
        "ARCH" : "<arch>"
      },
      "license" : "UPL",
    },

    "org.graalvm.vm.x86" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.memory",
      ],
      "javaCompliance" : "17+",
      "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
      "workingSets" : "vmx86",
      "license" : "UPL",
    },

    #"org.graalvm.vm.x86.nfi" : {
    #  "subDir" : "projects",
    #  "sourceDirs" : ["src"],
    #  "dependencies" : [
    #    "org.graalvm.vm.x86",
    #    "truffle:TRUFFLE_NFI"
    #  ],
    #  "javaCompliance" : "17+",
    #  "annotationProcessors" : ["truffle:TRUFFLE_DSL_PROCESSOR"],
    #  "workingSets" : "vmx86",
    #  "license" : "UPL",
    #},

    "org.graalvm.vm.x86.launcher" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "utils:CORE",
        "sdk:LAUNCHER_COMMON"
      ],
      "javaCompliance" : "17+",
      "workingSets" : "vmx86",
      "license" : "UPL",
    },

    "org.graalvm.vm.x86.testcases.asm" : {
      "subDir" : "tests",
      "class" : "VMX86TestSuite",
      "testProject" : True,
      "license" : "UPL",
    },

    "org.graalvm.vm.x86.testcases.c" : {
      "subDir" : "tests",
      "class" : "VMX86TestSuite",
      "testProject" : True,
      "license" : "UPL",
    },

    "org.graalvm.vm.x86.testcases.sulong.asm" : {
      "subDir" : "tests",
      "class" : "VMX86TestSuite",
      "testProject" : True,
      "license" : "UPL",
    },

    "org.graalvm.vm.x86.test" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.x86",
        "org.graalvm.vm.x86.testcases.asm",
        "org.graalvm.vm.x86.testcases.c",
        "org.graalvm.vm.x86.testcases.sulong.asm",
        "mx:JUNIT",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "vmx86",
      "testProject" : True,
      "license" : "UPL",
    },

    "org.graalvm.vm.memory.test" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.memory",
        "mx:JUNIT",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "vmx86",
      "testProject" : True,
      "license" : "UPL",
    },

    "org.graalvm.vm.x86.emu" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "jniHeaders" : True,
      "dependencies" : [
        "org.graalvm.vm.memory",
        "org.graalvm.vm.x86"
      ],
      "javaCompliance" : "17+",
      "workingSets" : "vmx86",
      "testProject" : True,
      "license" : "UPL",
    },

    "org.graalvm.vm.x86.emu.native" : {
      "subDir" : "projects",
      "native" : True,
      "vpath" : True,
      "results" : [
        "bin/<lib:emu86>",
      ],
      "buildDependencies" : [
        "org.graalvm.vm.x86.emu",
      ],
      "buildEnv" : {
        "CFLAGS" : "-I<jnigen:org.graalvm.vm.x86.emu>",
        "LIBEMU86" : "<lib:emu86>",
        "OS" : "<os>",
      },
      "testProject" : True,
      "license" : "UPL",
    },

    #"org.graalvm.vm.x86.nfi.native" : {
    #  "subDir" : "projects",
    #  "native" : True,
    #  "vpath" : True,
    #  "results" : [
    #    "bin/<lib:nfi>",
    #  ],
    #  "buildEnv" : {
    #    "LIBNFI" : "<lib:nfi>",
    #    "OS" : "<os>",
    #  },
    #  "license" : "UPL",
    #},
  },

  "distributions" : {
    "VM" : {
      "path" : "build/vmx86.jar",
      "subDir" : "vmx86",
      "sourcesPath" : "build/vmx86.src.zip",
      "dependencies" : [
        "org.graalvm.vm.x86",
      ],
      "distDependencies" : [
        "utils:CORE",
        "utils:POSIX",
        "truffle:TRUFFLE_API",
      ],
      "license" : "UPL",
    },

    "VM_MEMORY_NATIVE" : {
      "native" : True,
      "platformDependent" : True,
      "output" : "build",
      "dependencies" : [
        "org.graalvm.vm.memory.native"
      ],
      "license" : "UPL",
    },

    "VM_MEMORY_SVM" : {
      "path" : "build/memory-svm.jar",
      "subDir" : "vmx86",
      "sourcesPath" : "build/memory-svm.src.zip",
      "dependencies" : [
        "org.graalvm.vm.memory.svm",
        "substratevm:SVM"
      ],
      "distDependencies" : [
        "VM"
      ],
      "license" : "UPL",
    },

    #"VM_NFI" : {
    #  "path" : "build/vmx86_nfi.jar",
    #  "subDir" : "vmx86",
    #  "sourcesPath" : "build/vmx86_nfi.src.zip",
    #  "dependencies" : [
    #    "org.graalvm.vm.x86.nfi"
    #  ],
    #  "distDependencies" : [
    #    "VM",
    #    "VM_NFI_NATIVE",
    #    "utils:CORE",
    #    "utils:POSIX",
    #    "truffle:TRUFFLE_API",
    #    "truffle:TRUFFLE_NFI",
    #  ],
    #  "license" : "UPL",
    #},

    #"VM_NFI_NATIVE" : {
    #  "native" : True,
    #  "platformDependent" : True,
    #  "output" : "build",
    #  "dependencies" : [
    #    "org.graalvm.vm.x86.nfi.native"
    #  ],
    #  "license" : "UPL",
    #},

    "VMX86_LAUNCHER" : {
      "path" : "build/vmx86-launcher.jar",
      "sourcesPath" : "build/vmx86-launcher.src.zip",
      "subDir" : "vmx86",
      "mainClass" : "org.graalvm.vm.x86.launcher.AMD64Launcher",
      "dependencies" : ["org.graalvm.vm.x86.launcher"],
      "distDependencies" : [
        "utils:CORE",
        "sdk:LAUNCHER_COMMON",
      ],
      "license" : "UPL",
    },

    "VM_TESTCASES" : {
      "native" : True,
      "platformDependent" : True,
      "output" : "mxbuild/testcases",
      "dependencies" : [
        "org.graalvm.vm.x86.testcases.asm",
        "org.graalvm.vm.x86.testcases.c",
        "org.graalvm.vm.x86.testcases.sulong.asm",
      ],
      "javaProperties" : {
        "library.path" : "<path:VM_MEMORY_NATIVE>/<lib:memory>"
      },
      "license" : "UPL",
    },

    "VM_TEST" : {
      "path" : "build/vmx86_test.jar",
      "subDir" : "vmx86",
      "sourcesPath" : "build/vmx86_test.src.zip",
      "dependencies" : [
        "org.graalvm.vm.x86.test",
        "org.graalvm.vm.memory.test"
      ],
      "distDependencies" : [
        "VM",
        #"VM_NFI",
        "VM_TESTCASES",
        "utils:CORE",
        "utils:POSIX",
        "truffle:TRUFFLE_API",
        "truffle:TRUFFLE_NFI",
        "truffle:TRUFFLE_TCK",
        "sdk:POLYGLOT_TCK"
      ],
      "exclude" : [
        "mx:JUNIT"
      ],
      "javaProperties" : {
        "vmx86test.testSuitePath" : "<path:VM_TESTCASES>"
      },
      "license" : "UPL",
    },

    "VM_EMU86_NATIVE" : {
      "native" : True,
      "platformDependent" : True,
      "output" : "build",
      "dependencies" : [
        "org.graalvm.vm.x86.emu.native"
      ],
      "license" : "UPL",
    },

    "VM_EMU86" : {
      "path" : "build/vmx86emu.jar",
      "subDir" : "vmx86",
      "sourcesPath" : "build/vmx86emu.src.zip",
      "dependencies" : [
        "org.graalvm.vm.x86.emu"
      ],
      "distDependencies" : [
        "VM",
        "utils:CORE",
        "utils:POSIX"
      ],
      "license" : "UPL",
    },

    "VMX86_GRAALVM_SUPPORT" : {
      "native" : True,
      "platformDependent" : True,
      "description" : "vmx86 support distribution for the GraalVM",
      "layout": {
        "native-image.properties" : "file:mx.vmx86/native-image.properties",
        #"./" : ["extracted-dependency:vmx86:VM_MEMORY_NATIVE/<lib:memory>", "extracted-dependency:vmx86:VM_NFI_NATIVE/<lib:nfi>"],
        "./" : ["extracted-dependency:vmx86:VM_MEMORY_NATIVE/<lib:memory>"],
        "clibraries/<os>-<arch>/" : ["extracted-dependency:vmx86:VM_MEMORY_NATIVE/libmemory.a", "file:projects/org.graalvm.vm.memory.native/include"],
      },
      "license" : "UPL",
    },
  }
}
