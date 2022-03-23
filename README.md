# Distributed Chat Application - Serverside Implementation

The distributed chat application consists of 2 main components: chat clients and chat servers. Chat servers accept multiple incoming TCP connections from chat clients. The servers operate simultaneously. Currently, the system supports the operation of 6 servers. Each server is responsible of maintaining its own active chat client list and room list. For maintaining the global consistency of the application, for all the servers to ensure the global uniqueness of client ids and room ids, a leader is elected via _Fast Bully Algorithm_. A _gossip-style failure detection_ with _leader based consensus_ is implemented to secure a fault proof system.

## instructions on how to execute

**<u>Execute using jar</u>** 

The executable ```chat-server-1.0-SNAPSHOT-shaded.jar``` is provided in the folder ```Executable jar```.
A chat server can be executed using the following command:

```java -jar chat-server-1.0-SNAPSHOT-shaded.jar <server-id> <path-to-serverInfo.txt>```

- ```<path-to-serverInfo.txt>``` - path to serverInfo.txt file. The file is available inside the ```Executable jar``` folder itself.

- ```<server-id>```  - id of the server to be started. This server id should be one that is included in the serverInfo.txt.

**<u>Execute using docker</u>**

You should have docker and docker-compose installed in your PC for this.

1. Locate into the folder ```src\main\resources```.
2. Execute ```docker-compose up -d``` to start all the servers in the ServerInfo.txt file in separate docker containers.
3. If you want to shut down a server:
   1. Execute ```docker ps``` and check all the running container instances and find the instance related to server that you want to shut down (use the ports to identify the relevant server).
   2. Execute ```docker stop <container-id>```, where ```<container-id>``` is the id related to the container in which the server is currently running.