#!/bin/bash
# Script para adicionar um novo nó ao cluster SP
# Uso: ./add_node.sh <nome_do_nó> <porta>

if [ $# -ne 2 ]; then
    echo "Uso: ./add_node.sh <nome_do_nó> <porta>"
    exit 1
fi

NODE_NAME=$1
PORT=$2
BOOTSTRAP_IP="127.0.0.1"
BOOTSTRAP_PORT=30000

cd ./SP/
erl -eval "compile:file(start_servers), start_servers:start_with_config([{\"$NODE_NAME\", $PORT, {\"$BOOTSTRAP_IP\", $BOOTSTRAP_PORT}}])."
