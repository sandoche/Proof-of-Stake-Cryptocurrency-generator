#!/bin/sh

echo "************************************************"
echo "** DEPRECATED: Use 'run.sh --desktop' instead **"
echo "************************************************"
sleep 1

if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* -Dnxt.runtime.mode=desktop -Dnxt.runtime.dirProvider=nxt.env.DefaultDirProvider nxt.Nxt
