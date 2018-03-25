#!/bin/sh
java -cp "classes:lib/*:conf" nxt.tools.SignTransactionJSON $@
exit $?
