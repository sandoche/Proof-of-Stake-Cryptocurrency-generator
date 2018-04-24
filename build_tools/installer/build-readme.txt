Building the NXT installer

Note: all pathes are relative to the git repository folder (nxt-private)

Pre-requisites for both Unix and Windows
========================================
Compile classes on Unix using compile.sh
Windows build has to be executed from a Cygwin shell (Install from https://www.cygwin.com/)
Compile classes on Windows by running win-compile.sh from a Cygwin shell
In order to create an obfuscated release also install proguard (http://proguard.sourceforge.net/)
Package NXT.jar using jar.sh
mkdir jre

Building the installer on Unix
==============================
Execute the build: ./release-package.sh
Review results: vi installer/build-installer.log
Test X11 installer: java -jar nxt-client-<version>.jar
Test console installer: java -jar nxt-client-<version>.jar -console

Building the installer on Mac
==============================
Update the icon in ./installer/AppIcon.icns
Create a jre folder
Copy the content of /Library/Java/JavaVirtualMachines/jdk<version>.jdk/Contents/Home/jre content to the ./jre folder
Edit ./mac-release-package.sh and replace /Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home/bin/javapackager with your path of javapackager (you may have to update only the version)
Edit mac-release-package.sh and replace "Developer ID Application: Stichting NXT (YU63QW5EFW)" with your developer signature (you can also remove -Bmac.signing-key-developer-id-app="Developer ID Application: Stichting NXT (YU63QW5EFW)" to generate an unsigned package)
Run ./compile.sh then ./mac-release-package.sh

Building the installer on Windows
=================================
Initial setup (perform once for each development workstation or whenever Java is updated)
Install the latest version of the Java Windows Server JRE (64 bit)
Copy the content of Java JRE folder (C:\Program Files\Java\jre<version>) into the jre folder under your nxt-private git repository preserving the folder structure
Install the latest Java JDK x64 for Windows - needed for the jar and jarsigner commands
Install python and associate it with .py file extension - required by build-exe.bat
Install the Microsoft Windows SDK for Windows 7 and .NET Framework 4, make sure the signtool.exe command is in your path - needed to sign the installer executable

From a Cygwin shell invoke the command win-release-package.sh followed by the version number
Review results in installer/build-installer.log, installer/build-exe.log
Digitally sign the installer jar file and zip file using jarsigner
Digitally sign the installer exe file using signtool
Test the installer using Java: java -jar nxt-client-<version>.jar
Test the windows executable: nxt-client-<version>.exe

Technical information
=====================
Installer is based on the IzPack product from http://izpack.org/
Installer project source code is in installer/setup.xml, RegistrySpec.xml and shortcutSpec.xml
nxt.jar is now self executing, the full classpath is embedded into the jar file manifest automatically by jar.sh
Converting the Jar file to a Windows exe relies on the 7za utility (http://www.7-zip.org/) bundled with the IzPack distribution
Creating and signing the installer jar can be performed either on Unix or Windows
Creating and signing the installer exe must be performed on a Windows workstation
