#!/bin/sh
CP="conf/;classes/;lib/*;testlib/*"
SP="src/java/;test/java/"
TESTS="nxt.crypto.Curve25519Test nxt.crypto.ReedSolomonTest"

/bin/rm -f nxt.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -encoding utf8 -sourcepath $SP -classpath $CP -d classes/ src/java/nxt/*.java src/java/nxt/*/*.java test/java/nxt/*/*.java || exit 1

java -classpath $CP org.junit.runner.JUnitCore $TESTS

