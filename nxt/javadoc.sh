#!/bin/sh

if [ -x jdk/bin/java ]; then
    JAVADOC=./jdk/bin/javadoc
else
    JAVADOC=javadoc
fi

PATHSEP=":"
if [ "$OSTYPE" = "cygwin" ] ; then
PATHSEP=";"
fi

CP="lib/*${PATHSEP}classes${PATHSEP}addons/classes"
SP=src/java/${PATHSEP}addons/src/java/

/bin/rm -rf html/doc/*

${JAVADOC} -quiet -sourcepath ${SP} -classpath "${CP}" -protected -splitindex -subpackages nxt -d html/doc/
