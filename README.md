#To test SA and SC

Para cada SA existente o seu SC correspondente corre na porta SA_Port - 3 (Se SA na porta 7000 então SC correspondente corre na porta 6997).
O SA usa tambem as portas:
    -SA_Port - 1 para receber ligações de pedidos de criação de tópicos (6999 no exemplo acima).
    -SA_Port - 2 é usada para correr o gossip com agregação.
    -SA_Port é usada para correr o cyclon.

Os offsets de portas no SC continuam iguais ao que estavam antes, há vários sitios no código onde esses calculos são feitos então tenham cuidado e qq cena digam.

O Cyclon e a agregação estão a dar, o SA pede ao SC o nr de clientes e de tópicos e cria o novo topico nos que tem menos load (neste caso esta hardcoded para escolher os 2 melhores, mas é possivel depois mudar isto).

Se quiserem alterar a viewSize e o TTL no SA é ir à classe Config.

Para testarem isto podem:
    - Correr os Scripts bash dentro da pasta do SA e do SC (vai lançar 5 instancias de cada).
    - Enviar: echo "CREATE_TOPIC gajas joao" | nc 127.0.0.1 9999 (cria o topico no SA 10000 (a porta que recebe clientes é 10000-1))  
    - Ver nos logs dos SCs ou do SA que recebeu o pedido em que SCs o tópico foi criado e podem lançar um cliente para cada um deles para ver a replicação.

TODO falta a parte de o SA comunicar ao SP que criou os tópicos em X SCs, mas para isso preciso da ajuda do gonçalo terrestre.


