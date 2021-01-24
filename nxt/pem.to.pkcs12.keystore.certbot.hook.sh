#!/bin/sh

#######################################################################
## This script takes the pem files from a Let's Encrypt / Certbot
## directory and bundles them together for use by the current NRS
## installation, reading the corresponding properties from the
## nxt.properties file.
##
## It is designed to be run from the --deploy-hook Certbot option,
## meaning that it expects the RENEWED_LINEAGE environment variable
## to point to a directory with the PEM encoded files.
#######################################################################

PROPERTIES_PATH="conf/nxt.properties"

if [ -z $RENEWED_LINEAGE ]; then
	echo "RENEWED_LINEAGE environment variable not found, running from certbot --deploy-hook ?"
	exit
fi

OLD_DIR="$(pwd)"
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
cd $SCRIPTPATH

if [ ! -r $PROPERTIES_PATH ]; then
	echo "nxt.properties file not found"
	exit
fi

KEYSTORE=$(grep "^nxt.keyStorePath=" $PROPERTIES_PATH | cut -d'=' -f2)

if [ -z $KEYSTORE ]; then
	echo "You need to define nxt.keyStorePath on nxt.properties"
	exit
fi

KEYSTORE_PASS=$(grep "^nxt.keyStorePassword=" $PROPERTIES_PATH | cut -d'=' -f2)

if [ -z $KEYSTORE_PASS ]; then
	echo "You need to define nxt.keyStorePassword on nxt.properties"
	exit
fi

KEYSTORE_TYPE=$(grep "^nxt.keyStoreType=" $PROPERTIES_PATH | cut -d'=' -f2)

if [ -z $KEYSTORE_TYPE ] || [ $KEYSTORE_TYPE != "PKCS12" ]; then
	echo "You need to define the keystore type as PKCS12. Add \"nxt.keyStoreType=PKCS12\" to your nxt.properties file "
	exit
fi

openssl pkcs12 -export -in $RENEWED_LINEAGE/fullchain.pem -inkey $RENEWED_LINEAGE/privkey.pem -out $KEYSTORE -name nrs -passout pass:$KEYSTORE_PASS
chmod a+r $KEYSTORE

cd $OLD_DIR
