#!/bin/bash
VERSION=$1
if [ -x ${VERSION} ];
then
	echo VERSION not defined
	exit 1
fi
PACKAGE=nxt-client-${VERSION}
echo PACKAGE="${PACKAGE}"
CHANGELOG=nxt-client-${VERSION}.changelog.txt
OBFUSCATE=$2

FILES="changelogs conf html lib resource contrib logs"
FILES="${FILES} nxt.exe nxtservice.exe"
FILES="${FILES} 3RD-PARTY-LICENSES.txt AUTHORS.txt LICENSE.txt"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-tor.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh"
FILES="${FILES} nxt.policy nxtdesktop.policy Wallet.url Dockerfile"

# unix2dos *.bat
echo compile
./win-compile.sh
rm -rf html/doc/*
rm -rf nxt
rm -rf ${PACKAGE}.jar
rm -rf ${PACKAGE}.exe
rm -rf ${PACKAGE}.zip
mkdir -p nxt/
mkdir -p nxt/logs
mkdir -p nxt/addons/src

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo obfuscate
proguard.bat @nxt.pro
mv ../nxt.map ../nxt.map.${VERSION}
mkdir -p nxt/src/
else
FILES="${FILES} classes src COPYING.txt"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh"
FILES="${FILES} win-compile.sh win-javadoc.sh win-package.sh"
echo javadoc
./win-javadoc.sh
fi
echo copy resources
cp installer/lib/JavaExe.exe nxt.exe
cp installer/lib/JavaExe.exe nxtservice.exe
cp -a ${FILES} nxt
cp -a logs/placeholder.txt nxt/logs
echo gzip
for f in `find nxt/html -name *.gz`
do
	rm -f "$f"
done
for f in `find nxt/html -name *.html -o -name *.js -o -name *.css -o -name *.json  -o -name *.ttf -o -name *.svg -o -name *.otf`
do
	gzip -9c "$f" > "$f".gz
done
cd nxt
echo generate jar files
../jar.sh
echo package installer Jar
../installer/build-installer.sh ../${PACKAGE}
echo create installer exe
../installer/build-exe.bat ${PACKAGE}
echo create installer zip
cd -
zip -q -X -r ${PACKAGE}.zip nxt -x \*/.idea/\* \*/.gitignore \*/.git/\* \*.iml nxt/conf/nxt.properties nxt/conf/logging.properties nxt/conf/localstorage/\*
rm -rf nxt

echo creating change log ${CHANGELOG}
echo -e "Release $1\n" > ${CHANGELOG}
echo -e "https://bitbucket.org/JeanLucPicard/nxt/downloads/${PACKAGE}.exe\n" >> ${CHANGELOG}
echo -e "sha256:\n" >> ${CHANGELOG}
sha256sum ${PACKAGE}.exe >> ${CHANGELOG}

echo -e "https://bitbucket.org/JeanLucPicard/nxt/downloads/${PACKAGE}.jar\n" >> ${CHANGELOG}
echo -e "sha256:\n" >> ${CHANGELOG}
sha256sum ${PACKAGE}.jar >> ${CHANGELOG}

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo -e "\n\nThis is an experimental release for testing only. Source code is not provided." >> ${CHANGELOG}
fi
echo -e "\n\nChange log:\n" >> ${CHANGELOG}

cat changelogs/${CHANGELOG} >> ${CHANGELOG}
echo >> ${CHANGELOG}
