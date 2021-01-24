#!/bin/sh

# WARNING: java still bypasses the tor proxy when sending DNS queries and
# this can reveal the fact that you are running Nxt, however blocks and
# transactions will be sent over tor only. Requires a tor proxy running
# at localhost:9050. Set nxt.shareMyAddress=false when using tor.

if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
${JAVA} -DsocksProxyHost=localhost -DsocksProxyPort=9050 -cp classes:lib/*:conf:addons/classes:addons/lib/* nxt.Nxt

