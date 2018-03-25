#!/bin/sh
if [ -e ~/.nxt/nxt.pid ]; then
    PID=`cat ~/.nxt/nxt.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ~/.nxt/nxt.pid` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ~/.nxt/nxt.pid
    echo "Nxt server stopped"
fi

