#!/bin/bash

# Compile the project using Maven
echo "Compiling the project..."
mvn clean compile

# Check if the compilation was successful
if [ $? -ne 0 ]; then
  echo "Compilation failed. Exiting."
  exit 1
fi

# Loopback IPs to use (within 127.0.0.0/8)
LOOPBACK_BASE=127.0.0

# Launch 5 instances on the same port (5000) but different loopback IPs
for i in {1..5}
do
    IP="$LOOPBACK_BASE.$i"
    echo "Launching instance $i on $IP:5000"
    gnome-terminal -- bash -c "java -cp target/classes sa.Main initial_peers.txt $IP:5000; exec bash"
done
