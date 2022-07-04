trcview
=======

trcview is an extensible execution trace analyzer written in Java.


Build Dependencies
==================

- JDK11+
- mx
- Python (required by mx)


How to get started
==================

Create a new directory, e.g. `git` which will contain all necessary git repositories:

```
mkdir git && cd git
```

Install mx which is used to build trcview:

```
git clone https://github.com/graalvm/mx
export PATH=$PWD/mx:$PATH
```

Clone this repository:

```
git clone https://github.com/pekd/tracer
```

Set `JAVA_HOME` to a JDK >= 11, e.g.:

```
echo JAVA_HOME=/usr/lib/jvm/java-11-openjdk > tracer/trcview/mx.trcview/env
```

Build trcview:

```
cd tracer/trcview && mx build
```

Running trcview
---------------

After building the project with mx, you can find an executable jar file in the folder `trcview/build`.

```
java -jar build/trcview.jar
```

This build of trcview will contain support for the generic architecture as well as for PDP-11 and RISC-V.

IDE Setup
---------

Generate Eclipse project files:

```
mx eclipseinit
```

Generate IntelliJ project files:

```
mx intellijinit
```
