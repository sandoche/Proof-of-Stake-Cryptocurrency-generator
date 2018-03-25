#!/bin/sh
java -cp classes nxt.tools.ManifestGenerator
/bin/rm -f nxt.jar
jar cfm nxt.jar resource/nxt.manifest.mf -C classes . || exit 1
/bin/rm -f nxtservice.jar
jar cfm nxtservice.jar resource/nxtservice.manifest.mf -C classes . || exit 1

echo "jar files generated successfully"