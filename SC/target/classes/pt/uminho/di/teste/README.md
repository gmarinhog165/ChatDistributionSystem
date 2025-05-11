# Testar o SC:

## Testar SC <-> Cliente:
- Iniciar ChatServer na porta 50052;
- Iniciar ChatClient na porta 50052;
- No Cliente tem um menu demonstrativo que permite: enviar mensagem (sem prefixo), consultar users ativos, obter logs [nº de logs] [de dado user]

## Testar SA -> SC | SC <-> Cliente | SC <-> SC
- Iniciar ChatServer na porta 50052;
- Iniciar ChatServer na porta 50062;
- Iniciar SAMock que vai conectar com estes dois SC em 50051 e 50052 e popular um tópico que será servido por estes dois SC;
- Iniciar ChatClient na porta 50052;
- Iniciar ChatClient na porta 50062;

Utilizar os comandos do cliente e ver que, apesar de ligados aos dois clientes diferentes, as mensagens estão a ser propagadas entre SCs e entregues aos respetivos clientes.