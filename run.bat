IF EXIST "temp" RMDIR /S /Q "temp"

MKDIR "temp"

COPY "target\RedBot-jar-with-dependencies.jar" "temp\RedBot-jar-with-dependencies.jar"

java -jar temp\RedBot-jar-with-dependencies.jar