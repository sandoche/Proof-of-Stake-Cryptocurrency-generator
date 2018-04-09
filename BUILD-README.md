Building the NXT installer

Note: all pathes are relative to the git repository folder (nxt-private)

Pre-requisites for both Unix and Windows
========================================
* Compile classes on Unix using compile.sh
* Windows build has to be executed from a Cygwin shell (Install from https://www.cygwin.com/)
* Compile classes on Windows by running win-compile.sh from a Cygwin shell
* In order to create an obfuscated release also install proguard (http://proguard.sourceforge.net/)
* Package NXT.jar using jar.sh
* `mkdir jre`

Building the installer on Unix
==============================
* Execute the build: ./release-package.sh
* Review results: vi installer/build-installer.log
* Test X11 installer: java -jar nxt-client-<version>.jar
* Test console installer: java -jar nxt-client-<version>.jar -console

Building the installer on Mac
==============================
* Update the icon
* Copy the jre files and folder from your mac into the jre folder
* Update the signature (or remove it) from the mac-release-package.sh

Building the installer on Windows
=================================
1. Initial setup (perform once for each development workstation or whenever Java is updated)
2. Install the latest version of the Java Windows Server JRE (64 bit)
3. Copy the content of Java JRE folder (C:\Program Files\Java\jre<version>) into the jre folder under your nxt-private git repository preserving the folder structure
4. Install the latest Java JDK x64 for Windows - needed for the jar and jarsigner commands
5. Install python and associate it with .py file extension - required by build-exe.bat
6. Install the Microsoft Windows SDK for Windows 7 and .NET Framework 4, make sure the signtool.exe command is in your path - needed to sign the installer executable
7. From a Cygwin shell invoke the command win-release-package.sh followed by the version number
8. Review results in installer/build-installer.log, installer/build-exe.log
9. Digitally sign the installer jar file and zip file using jarsigner
10. Digitally sign the installer exe file using signtool
11. Test the installer using Java: java -jar nxt-client-<version>.jar
12. Test the windows executable: nxt-client-<version>.exe

Technical information
=====================
* Installer is based on the IzPack product from http://izpack.org/
* Installer project source code is in installer/setup.xml, RegistrySpec.xml and shortcutSpec.xml
* nxt.jar is now self executing, the full classpath is embedded into the jar file manifest automatically by jar.sh
* Converting the Jar file to a Windows exe relies on the 7za utility (http://www.7-zip.org/) bundled with the IzPack distribution
* Creating and signing the installer jar can be performed either on Unix or Windows
* Creating and signing the installer exe must be performed on a Windows workstation
