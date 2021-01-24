if exist jdk (
    set javaDir=jdk\bin\
)

%javaDir%java.exe -Xmx1024m -cp "classes;lib/*;conf" -Dnxt.runtime.mode=desktop nxt.tools.SignTransactionJSON %*
