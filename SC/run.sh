#!/bin/bash

echo "Compiling the project..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting."
    exit 1
fi

echo "Building classpath with dependencies..."
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q

# Read the classpath from cp.txt and prepend target/classes
DEPS=$(cat cp.txt)
CP="target/classes:$DEPS"

# List of specific ports
PORTS=(9997 14997 19997 24997 29997)

# Loop through the specified ports and launch a ChatServer instance for each
for PORT in "${PORTS[@]}"
do
    echo "Launching ChatServer on Port $PORT..."

    # Open a new terminal and run the ChatServer instance on the specified port
    gnome-terminal -- bash -c "java -cp \"$CP\" pt.uminho.di.ChatServer \"$PORT\"; exec bash"
done
