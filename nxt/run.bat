	@echo off
	if exist jre ( 
		set "javaDir=jre"
		goto startJava
	)
	rem inspired by http://stackoverflow.com/questions/24486834/reliable-way-to-find-jre-installation-in-windows-bat-file-to-run-java-program
	rem requires Windows 7 or higher
	setlocal enableextensions disabledelayedexpansion

	rem find Java information in the Windows registry
	rem for 64 bit Java on Windows 64 bit or for Java on Windows 32 bit
	set "javaKey=HKLM\SOFTWARE\JavaSoft\Java Runtime Environment"

	rem look for Java version
	set "javaVersion="
	for /f "tokens=3" %%v in ('reg query "%javaKey%" /v "CurrentVersion" 2^>nul') do set "javaVersion=%%v"

	rem for 32 bit Java on Windows 64 bit
	set "javaKey32=HKLM\SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment"

	rem look for 32 bit Java version on Windows 64 bit
	set "javaVersion32="
	for /f "tokens=3" %%v in ('reg query "%javaKey32%" /v "CurrentVersion" 2^>nul') do set "javaVersion32=%%v"

	echo Java version in "%javaKey%" is "%javaVersion%" and in "%javaKey32%" is "%javaVersion32%"

	rem test if a java version has been found
	if not defined javaVersion if not defined javaVersion32 (
		echo Java not found, please install Java JRE
		goto endProcess
	)

	if not defined javaVersion ( set "javaVersion=0" )

	if not defined javaVersion32 ( set "javaVersion32=0" )

	rem test if a java version is compatible
	if not %javaVersion% geq 1.8 (
		if not %javaVersion32% geq 1.8 (
			echo Java version is lower than 1.8, please install Java 8 or later Java JRE
			goto endProcess
		) else (
			echo using Java 32 bit on a 64 bit Windows workstation
			set "javaKey=%javaKey32%"
			set "javaVersion=%javaVersion32%"
		)
	)

	rem Get java home for current java version
	for /f "tokens=2,*" %%d in ('reg query "%javaKey%\%javaVersion%" /v "JavaHome" 2^>nul') do set "javaDir=%%e"

	if not defined javaDir (
		echo Java directory not found
		goto endProcess
	) else (
		echo using Java home directory "%javaDir%"
	)

:startJava	
	start "NXT NRS from %~dp0" "%javaDir%"\bin\java.exe -cp classes;lib\*;conf;addons\classes;addons\lib\* -Dnxt.runtime.mode=desktop nxt.Nxt

:endProcess 
	endlocal
