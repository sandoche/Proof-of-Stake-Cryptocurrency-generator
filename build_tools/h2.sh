#!/bin/sh
java -cp lib/h2*.jar org.h2.tools.Shell -url jdbc:h2:./nxt_db/nxt -user sa -password sa
