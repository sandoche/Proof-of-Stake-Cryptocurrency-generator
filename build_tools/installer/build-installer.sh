#!/bin/sh
java -Xmx512m -cp "../installer/lib/*" com.izforge.izpack.compiler.bootstrap.CompilerLauncher ../installer/setup.xml -o $1.jar > ../installer/build-installer.log 2>&1