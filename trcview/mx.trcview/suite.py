suite = {
  "mxversion" : "5.223.0",
  "name" : "trcview",
  "versionConflictResolution" : "latest",

  "imports" : {
    "suites" : [
      {
        "name" : "utils",
        "subdir" : True,
        "version" : "00e0018db91b66ebaec939f6cd7fa5f90f46ef30",
        "urls" : [
          {"url" : "https://github.com/pekd/vmx86", "kind" : "git"},
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

  "defaultLicense" : "UPL",

  "projects" : {
    "org.graalvm.vm.trcview.arch.pdp11" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "vmx86",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.arch.riscv" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "vmx86",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.arch.custom" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "vmx86",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "utils:CORE",
        "utils:POSIX",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "vmx86",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.libtrc" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "utils:CORE",
        "utils:POSIX",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "vmx86",
      "license" : "UPL",
    },

    "org.graalvm.vm.trcview.test" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.trcview",
        "org.graalvm.vm.trcview.arch.pdp11",
        "org.graalvm.vm.trcview.arch.riscv",
        "mx:JUNIT",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "vmx86",
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
      "javaCompliance" : "1.8+",
      "workingSets" : "vmx86",
      "testProject" : True,
      "license" : "UPL",
    },
  },

  "distributions" : {
    "TRCVIEW" : {
      "path" : "build/trcview.jar",
      "sourcesPath" : "build/trcview.src.zip",
      "subDir" : "vmx86",
      "mainClass" : "org.graalvm.vm.trcview.ui.MainWindow",
      "dependencies" : [
        "org.graalvm.vm.trcview",
        "org.graalvm.vm.trcview.arch.pdp11",
        "org.graalvm.vm.trcview.arch.riscv",
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
      "subDir" : "vmx86",
      "mainClass" : "org.graalvm.vm.trcview.ui.MainWindow",
      "dependencies" : [
        "org.graalvm.vm.trcview",
      ],
      "overlaps" : [
        "utils:CORE",
        "utils:POSIX",
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
