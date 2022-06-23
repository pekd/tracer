suite = {
  "mxversion" : "5.223.0",
  "name" : "utils",
  "versionConflictResolution" : "latest",

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
    "org.graalvm.vm.util" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "javaCompliance" : "1.8+",
      "workingSets" : "core",
      "license" : "UPL",
    },

    "org.graalvm.vm.math" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "javaCompliance" : "1.8+",
      "workingSets" : "core",
      "license" : "UPL",
    },

    "org.graalvm.vm.posix" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.util",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "core",
      "license" : "UPL",
    },

    "org.graalvm.vm.util.test" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.util",
        "mx:JUNIT",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "core",
      "license" : "UPL",
    },

    "org.graalvm.vm.math.test" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.math",
        "mx:JUNIT",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "core",
      "license" : "UPL",
    },

    "org.graalvm.vm.posix.test" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.vm.posix",
        "mx:JUNIT",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "core",
      "license" : "UPL",
    },
  },

  "distributions" : {
    "CORE" : {
      "path" : "build/core.jar",
      "subDir" : "core",
      "sourcesPath" : "build/core.src.zip",
      "dependencies" : [
        "org.graalvm.vm.util",
        "org.graalvm.vm.math",
      ],
      "license" : "UPL",
    },

    "POSIX" : {
      "path" : "build/posix.jar",
      "subDir" : "core",
      "sourcesPath" : "build/posix.src.zip",
      "dependencies" : [
        "org.graalvm.vm.posix",
      ],
      "distDependencies" : [
        "CORE",
      ],
      "license" : "UPL",
    },

    "CORE_TEST" : {
      "path" : "build/core_test.jar",
      "subDir" : "core",
      "sourcesPath" : "build/core_test.src.zip",
      "dependencies" : [
        "org.graalvm.vm.util.test",
        "org.graalvm.vm.math.test",
      ],
      "exclude" : [
        "mx:JUNIT"
      ],
      "distDependencies" : [
        "CORE",
      ],
      "license" : "UPL",
    },

    "POSIX_TEST" : {
      "path" : "build/posix_test.jar",
      "subDir" : "core",
      "sourcesPath" : "build/posix_test.src.zip",
      "dependencies" : [
        "org.graalvm.vm.posix.test"
      ],
      "exclude" : [
        "mx:JUNIT"
      ],
      "distDependencies" : [
        "CORE",
        "POSIX"
      ],
      "license" : "UPL",
    }
  }
}
