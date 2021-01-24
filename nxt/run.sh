#!/bin/sh

desktop=0
authbind=0
daemon=0

help()
{
    echo "Parameters:"
    echo
    echo "  --desktop : Force desktop mode in the current directory."
    echo "  --daemon  : Start in daemon mode (background). Use stop.sh to stop the node."
    echo "  --authbind: Use authbind (installed separately) to allow binding of privileged ports."
    exit
}

while [ "$1" != "" ]; do
    case $1 in
        --desktop )    desktop=1
                       ;;
        --authbind )   authbind=1
                       ;;
        --daemon )     daemon=1
                       ;;
        * )            help
                       ;;
    esac
    shift
done

if [ $desktop -eq 1 ] && [ $daemon -eq 1 ]; then
    echo "You can't start in desktop and daemon mode at the same time. Pick one."
    exit 1
fi

if [ -x jdk/bin/java ]; then
    JAVACMD=./jdk/bin/java
else
    JAVACMD=java
fi

if [ $authbind -eq 1 ]; then
    JAVACMD="authbind ${JAVACMD}"
fi

if [ $desktop -eq 1 ]; then
  echo "Starting desktop mode in current directory"
  ${JAVACMD} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* -Dnxt.runtime.mode=desktop -Dnxt.runtime.dirProvider=nxt.env.DefaultDirProvider nxt.Nxt
elif [ $daemon -eq 1 ]; then
  echo "Starting daemon mode"
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
  nohup ${JAVACMD} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* nxt.Nxt > /dev/null 2>&1 &
  echo $! > ~/.nxt/nxt.pid
  cd - > /dev/null
else
  echo "Starting default mode"
  ${JAVACMD} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* nxt.Nxt
fi
