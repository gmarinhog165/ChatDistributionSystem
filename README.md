# Chat Distribution System

![Java](https://img.shields.io/badge/Backend-Java-blue?logo=openjdk)
![Erlang](https://img.shields.io/badge/Overlay-Erlang-A90533?logo=erlang)
![gRPC](https://img.shields.io/badge/Communication-gRPC-4285F4?logo=google)
![ZeroMQ](https://img.shields.io/badge/Messaging-ZeroMQ-DF0000)

This project implements a **distributed publish/subscribe chat system** with a peer-to-peer overlay network. Users can create topics, exchange messages in real time, and receive updates through a streaming subscription model. The architecture is designed for scalability and fault tolerance, leveraging a gossip-based information dissemination protocol and conflict-free replicated data structures (CRDTs).

---

## Goal

To build a resilient, decentralized messaging platform where:
- Users join and leave **topics** dynamically;
- Messages are distributed across multiple **chat servers (SC)** using replication;
- A **peer-to-peer overlay (SP)**, built on a DHT, handles node discovery and routing;
- A **service aggregator (SA)** manages peer gossip and coordinates server awareness;
- Consistency is maintained through **ORSet CRDTs** even in the presence of concurrent updates.

---

## Tech Stack

**Chat Server (SC):**
- Java 16+
- gRPC 1.61.1 with reactive streaming (RxJava 3.1.8 / rx3grpc 1.2.4)
- Protocol Buffers 3.25.3
- ORSet CRDT for conflict-free user set replication

**P2P Overlay (SP):**
- Erlang/OTP
- Custom DHT implementation with TCP-based node communication
- Gossip-based topology maintenance

**Service Aggregator (SA):**
- Java
- ZeroMQ (JeroMQ 0.6.0)
- Cyclon gossip protocol for peer discovery

**Client:**
- Java CLI
- gRPC (connects to SP for discovery, SC for chat operations)

**Build:**
- Maven
- Bash scripts

---

## Deployment Instructions

### 1. Start the SP Cluster

Compiles all Erlang modules and launches 4 nodes (bootstrap + 3 peers):

```bash
./start_sp_cluster.sh
```

Default ports used:

| Node            | Port  | 
|-----------------|-------|
| bootstrap_node  | 30000 |
| node1           | 30100 |
| node2           | 30200 |
| node3           | 30300 |

Each node occupies ports `{Port, Port+1}`.

To add a new SP node to the running cluster:

```bash
./SP/add_node.sh <node_name> <port>
```

### 2. Start a Chat Server (SC)

From the `SC/` directory, after building with Maven:

```bash
java -cp target/SC-1.0-SNAPSHOT.jar pt.uminho.di.ChatServer <port> <ip>
```

Ports used per SC instance: `{Port, Port-1, Port+100, Port+200}`

### 3. Start the Service Aggregator (SA)

From the `SA/` directory, after building with Maven:

```bash
java -jar target/SA-1.0-SNAPSHOT.jar
```

### 4. Run the Client

From the `Client/` directory, after building with Maven:

```bash
java -jar target/Aula4-1.0-SNAPSHOT.jar -h <SP_IP> -p <SP_port>
```

---

## Directory Structure

```
.
├── SC/                  # Chat Server — handles topics, users, messages, and subscriptions
│   └── src/             # ChatServer, ChatServiceImpl, ChatDistributionManager, ORSet, ...
├── SP/                  # P2P Overlay — Erlang DHT cluster for node discovery and routing
│   └── *.erl            # sp_dht, sp_node_comm, sp_server, sp_tcp_listener, ...
├── SA/                  # Service Aggregator — Cyclon gossip peer discovery via ZeroMQ
│   └── src/             # Aggregator, CyclonPeer, GossipRequestHandler, ...
├── Client/              # CLI Chat Client — connects to SP and SC via gRPC
│   └── src/             # ChatClientApp, ChatClientMenu, SPClient, SCClient, SAClient
└── start_sp_cluster.sh  # Script to compile Erlang modules and launch the SP cluster
```

---

## Contributors

This project was developed by students from the University of Minho.

| Name | ID Universitário |
|------|--------|
| [Gonçalo Marinho](https://github.com/gmarinhog165) | PG55945 |
| [Henrique Vaz](https://github.com/Vaz7) | PG55947 |
| [Mike Pinto](https://github.com/mrmikept) | PG55987 |
| [Simão Antunes](https://github.com/simantunes008) | PG57903 |

---

## License

This project is licensed under the **MIT License**.
