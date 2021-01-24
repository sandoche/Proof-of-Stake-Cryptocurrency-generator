@REM Compact the Nxt NRS database
@echo *********************************************************************
@echo * This batch file will compact and reorganize the Nxt NRS database. *
@echo * This process can take a long time.  Do not interrupt the batch    *
@echo * file or shutdown the computer until it finishes.                  *
@echo *********************************************************************

if exist jdk (
    set javaDir=jdk\bin\
)

%javaDir%java.exe -Xmx1024m -cp "classes;lib/*;conf" -Dnxt.runtime.mode=desktop nxt.tools.CompactDatabase
