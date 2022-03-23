# Distributed Chat Application - Serverside Implementation

The distributed chat application consists of 2 main components: chat clients and chat servers. Chat servers accept multiple incoming TCP connections from chat clients. The servers operate simultaneously. Currently, the system supports the operation of 6 servers. Each server is responsible of maintaining its own active chat client list and room list. For maintaining the global consistency of the application, for all the servers to ensure the global uniqueness of client ids and room ids, a leader is elected via _Fast Bully Algorithm_. A _gossip-style failure detection_ with _leader based consensus_ is implemented to secure a fault proof system.

## instructions on how to execute
