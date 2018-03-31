#!/bin/sh
CP=conf/:classes/:lib/*:testlib/*
SP=src/java/:test/java/

if [ $# -eq 0 ]; then
TESTS="nxt.crypto.Curve25519Test nxt.crypto.ReedSolomonTest nxt.peer.HallmarkTest nxt.TokenTest nxt.FakeForgingTest
nxt.FastForgingTest nxt.ManualForgingTest"
else
TESTS=$@
fi

/bin/rm -f nxt.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -encoding utf8 -sourcepath ${SP} -classpath ${CP} -d classes/ src/java/nxt/*.java src/java/nxt/*/*.java test/java/nxt/*.java test/java/nxt/*/*.java || exit 1

for TEST in ${TESTS} ; do
java -classpath ${CP} org.junit.runner.JUnitCore ${TEST} ;
done



