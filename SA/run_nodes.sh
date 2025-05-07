#!/bin/bash

# Compile the project using Maven
echo "Compiling the project..."
mvn clean compile

# Check if the compilation was successful
if [ $? -ne 0 ]; then
  echo "Compilation failed. Exiting."
  exit 1
fi

# Loop to open 5 terminals and run the Java program in each one
for i in {1..5}
do
    gnome-terminal -- bash -c "java -cp target/classes sa.Main initial_peers.txt 127.0.0.1:$((5000 + i - 1)); exec bash"
done
