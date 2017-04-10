# Project 1 -- Distributed Backup Service
## Source Files

All the source code files required to compile and run the project are in the `src` folder

## How to compile

The `.java` files can be compiled manually with the `javac` utility

- javac Service/Peer.java
- javac TestApp/TestApp.java

## How to run

### Peer

In order to start a peer the user needs to run the associated class in `Service.Peer`

```
Usage:
	java Service.Peer <server_id>
	java Service.Peer <server_id> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>
```

When the multicast addresses and ports *are not specified* the following defaults are used:

*Note: In linux <server_id> must be higher than 1024.*

|MC            |MDB           |MDR           |
|--------------|--------------|--------------|
|224.0.0.0:8000|224.0.0.0:8001|224.0.0.0:8002| (*Address:Port*)

#### Local files

Every peer is going to store their backup files in /storage.

### Interface

In order to communicate with peer (initiator peer)
the user must run the associated class in `TestApp.TestApp`

```
Usage:
	java TestApp.TestApp <peer_ap> [options]

Options
--------------------
	BACKUP <file_name> <replication_degree>            Backup file with specified replication degree
	RESTORE <file_name>                                Restore file that was previously replicated
	DELETE <file_name>                                 Delete file from backup service
	RECLAIM <new_space>                                Reclaim the space 
	STATE                                              Manage local storage
```
