# ChatDistributionSystem


## Iniciar os SP iniciais
`./start_sp_cluster.sh`  

Portas utilizadas: {Port, Port+1}

### Para adicionarmos basta correr
`./SP/add_node.sh <nome node> <porta>`

## Iniciar os SC
`java -cp target/SC-1.0-SNAPSHOT.jar pt.uminho.di.ChatServer <port> <ip>` 

Portas utilizadas: {Port, Port-1, Port+100, Port+200}

Enquanto não temos a conexão ao SA utilizar o SAMock

## Correr o Cliente
`java -jar target/Aula4-1.0-SNAPSHOT.jar -h <SP_IP> -p <SP port>` dentro de ./Client
