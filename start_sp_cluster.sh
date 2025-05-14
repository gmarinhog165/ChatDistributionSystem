#!/bin/bash

# Script para compilar módulos Erlang e iniciar o cluster SP
# Uso: ./start_sp_cluster.sh

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Compilar todos os módulos Erlang
echo -e "${BLUE}Compilando módulos Erlang...${NC}"
cd ./SP/
erlc sp_app.erl sp_node_comm.erl sp_dht.erl sp_server.erl sp_tcp_listener.erl start_servers.erl

# Verificar se a compilação foi bem-sucedida
if [ $? -ne 0 ]; then
    echo "Erro na compilação dos módulos Erlang!"
    exit 1
fi
echo -e "${GREEN}Compilação concluída com sucesso.${NC}"

# Função para iniciar um nó Erlang
start_node() {
    local node_name=$1
    local port=$2
    local bootstrap_ip=$3
    local bootstrap_port=$4
    local window_title="SP Node - $node_name"
    
    # Comando Erlang a ser executado
    if [ -z "$bootstrap_ip" ]; then
        # Nó bootstrap (inicial)
        CMD="erl -eval \"compile:file(start_servers), start_servers:start_with_config([{\\\"$node_name\\\", $port, none}]).\""
    else
        # Nó que se conecta ao bootstrap
        CMD="erl -eval \"compile:file(start_servers), start_servers:start_with_config([{\\\"$node_name\\\", $port, {\\\"$bootstrap_ip\\\", $bootstrap_port}}]).\""
    fi
    
    # Iniciar em um novo terminal
    if command -v gnome-terminal &> /dev/null; then
        gnome-terminal --title="$window_title" -- bash -c "$CMD; exec bash"
    elif command -v xterm &> /dev/null; then
        xterm -T "$window_title" -e "$CMD; exec bash" &
    elif command -v konsole &> /dev/null; then
        konsole --new-tab -p tabtitle="$window_title" -e "$CMD; exec bash" &
    elif command -v terminal &> /dev/null; then
        terminal -e "$CMD; exec bash" &
    elif command -v iTerm &> /dev/null; then
        # Para macOS
        osascript -e "tell application \"iTerm\"
            create window with default profile
            tell current window
                tell current session
                    set name to \"$window_title\"
                    write text \"cd $(pwd) && $CMD\"
                end tell
            end tell
        end tell"
    else
        echo "Nenhum terminal compatível encontrado. Instalando xterm..."
        sudo apt-get install xterm -y
        xterm -T "$window_title" -e "$CMD; exec bash" &
    fi
    
    echo -e "${GREEN}Nó $node_name iniciado na porta $port${NC}"
    sleep 1
}

# Iniciar o nó bootstrap
echo -e "${BLUE}Iniciando o nó bootstrap na porta 30000...${NC}"
start_node "bootstrap_node" 30000

# Esperar um pouco para garantir que o nó bootstrap está pronto
sleep 2

# Iniciar os outros nós
echo -e "${BLUE}Iniciando nós adicionais...${NC}"
start_node "node1" 30100 "127.0.0.1" 30000
start_node "node2" 30200 "127.0.0.1" 30000
start_node "node3" 30300 "127.0.0.1" 30000

echo -e "${GREEN}Cluster SP iniciado com sucesso!${NC}"
echo -e "Para adicionar um novo nó, use o comando:"
echo -e "${BLUE}./add_node.sh node_name porta${NC}"

# Criar script para adicionar novos nós
cat > ./add_node.sh << 'EOL'
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
EOL

chmod +x ./add_node.sh
