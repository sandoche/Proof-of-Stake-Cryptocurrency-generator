#!/bin/sh

echo "***********************************************"
echo "** DEPRECATED: Use 'run.sh --desktop' instead **"
echo "***********************************************"
sleep 1

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
if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
nohup ${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* -Dnxt.runtime.mode=desktop nxt.Nxt > /dev/null 2>&1 &
echo $! > ~/.nxt/nxt.pid
cd - > /dev/null
