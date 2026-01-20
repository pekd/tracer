suite = {
  "mxversion" : "5.223.0",
  "name" : "trcview",
  "versionConflictResolution" : "latest",

  "imports" : {
    "suites" : [
      {
        "name" : "utils",
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
    },
    "GPLv3" : {
      "name" : "GNU General Public License, version 3",
      "url" : "https://opensource.org/licenses/GPL-3.0",
    },
  },

  "defaultLicense" : "UPL",

  "projects" : {
    "org.graalvm.vm.trcview.arch.pdp11" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.arch.riscv" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.arch.ppc" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.arch.x86" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.arch.z80" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "license" : "GPLv3",
    },

    "org.graalvm.vm.trcview.arch.h8s" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "license" : "GPLv3",
    },

    "org.graalvm.vm.trcview.arch.custom" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "utils:CORE",
        "utils:POSIX",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.libtrc" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "utils:CORE",
        "utils:POSIX",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.test" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
        "org.graalvm.vm.trcview.arch.pdp11",
        "org.graalvm.vm.trcview.arch.riscv",
        "org.graalvm.vm.trcview.arch.ppc",
        "org.graalvm.vm.trcview.arch.x86",
        "org.graalvm.vm.trcview.arch.z80",
        "org.graalvm.vm.trcview.arch.h8s",
        "mx:JUNIT",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "testProject" : True,
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.arch.custom.test" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview.arch.custom",
        "mx:JUNIT",
      ],
      "javaCompliance" : "17+",
      "workingSets" : "trcview",
      "testProject" : True,
      "license" : "UPL",
    },
  },

  "distributions" : {
    "TRCVIEW" : {
      "path" : "build/trcview.jar",
      "sourcesPath" : "build/trcview.src.zip",
      "subDir" : "trcview",
      "mainClass" : "org.graalvm.vm.trcview.ui.MainWindow",
      "dependencies" : [
        "org.graalvm.vm.trcview",
        "org.graalvm.vm.trcview.arch.pdp11",
        "org.graalvm.vm.trcview.arch.riscv",
        "org.graalvm.vm.trcview.arch.ppc",
        "org.graalvm.vm.trcview.arch.x86",
        "org.graalvm.vm.trcview.arch.z80",
        "org.graalvm.vm.trcview.arch.h8s",
        "org.graalvm.vm.trcview.arch.custom",
      ],
      "overlaps" : [
        "utils:CORE",
        "utils:POSIX",
      ],
      "license" : "UPL",
    },

    "TRCVIEW_MINIMAL" : {
      "path" : "build/trcview-minimal.jar",
      "sourcesPath" : "build/trcview-minimal.src.zip",
      "subDir" : "trcview",
      "mainClass" : "org.graalvm.vm.trcview.ui.MainWindow",
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "overlaps" : [
        "utils:CORE",
        "utils:POSIX",
        "TRCVIEW",
      ],
      "license" : "UPL",
    },

    "LIBTRC" : {
      "path" : "build/libtrc.jar",
      "subDir" : "core",
      "sourcesPath" : "build/libtrc.src.zip",
      "dependencies" : [
        "org.graalvm.vm.trcview.libtrc",
      ],
      "distDependencies" : [
        "utils:CORE",
        "utils:POSIX",
      ],
      "license" : "UPL",
    },
  }
}
