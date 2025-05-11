#!/bin/bash

# Compile the project using Maven
echo "Compiling the project..."
mvn clean compile

# Check if the compilation was successful
if [ $? -ne 0 ]; then
  echo "Compilation failed. Exiting."
  exit 1
fi

# Get the Maven classpath including all dependencies
MAVEN_CLASSPATH=$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)

# Combine with the target/classes directory
FULL_CLASSPATH="target/classes:$MAVEN_CLASSPATH"

echo "Using classpath: $FULL_CLASSPATH"

# Array of ports
PORTS=(5000 6000 7000 8000 9000)

# Launch 5 instances with different ports
for i in {0..4}
do
    PORT=${PORTS[$i]}
    echo "Launching instance $((i+1)) on port $PORT"
    gnome-terminal -- bash -c "java -cp \"$FULL_CLASSPATH\" sa.Main initial_peers.txt $PORT; exec bash"
done