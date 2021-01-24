#!/bin/sh
if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
${JAVA} -cp "classes:lib/*:conf" nxt.tools.SignTransactionJSON $@
exit $?
