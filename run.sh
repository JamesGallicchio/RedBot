if [ -d "temp" ]; then
	rm -R "temp"
fi

mkdir "temp"

cp "target/RedBot-jar-with-dependencies.jar" "temp/RedBot-jar-with-dependencies.jar"

token=$(<token.txt)

java -jar "temp/RedBot-jar-with-dependencies.jar" "$token"
