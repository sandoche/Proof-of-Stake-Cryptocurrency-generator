#!/bin/sh
if [ -e ~/.nxt/nxt.pid ]; then
    PID=`cat ~/.nxt/nxt.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "Nxt server already running"
        exit 1
    fi
fi
mkdir -p ~/.nxt/
DIR=`dirname "$0"`
cd "${DIR}"
if [ -x jre/bin/java ]; then
    JAVA=./jre/bin/java
else
    JAVA=java
fi
nohup ${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/* -Dnxt.runtime.mode=desktop nxt.Nxt > /dev/null 2>&1 &
echo $! > ~/.nxt/nxt.pid
cd - > /dev/null
