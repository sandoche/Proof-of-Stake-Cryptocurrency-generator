#!/bin/sh
PLATFORM=$1
if [ -x ${PLATFORM} ];
then
	echo PLATFORM not defined
	exit 1
fi
./updateapp.sh
cd wallet
cordova run ${PLATFORM}
cd ..
