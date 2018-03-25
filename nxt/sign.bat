if exist jre ( 
    set javaDir=jre\bin\
)

%javaDir%java.exe -Xmx1024m -cp "classes;lib/*;conf" -Dnxt.runtime.mode=desktop nxt.tools.SignTransactionJSON
