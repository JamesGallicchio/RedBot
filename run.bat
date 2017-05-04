IF EXIST "temp" RMDIR /S /Q "temp"

MKDIR "temp"

COPY "target\RedBot-jar-with-dependencies.jar" "temp\RedBot-jar-with-dependencies.jar"

set /p token=<token.txt

start java -jar temp\RedBot-jar-with-dependencies.jar %token%

exit