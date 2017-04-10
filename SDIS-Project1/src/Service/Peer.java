package Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import FileSystem.Chunk;
<<<<<<< HEAD
import FileSystem.FileChunk;
import FileSystem.FileAttributes;
=======
import FileSystem.FileAttributes;
import FileSystem.FileChunk;
>>>>>>> 54640ba26e7198bf92ca12b570e9c54313d4c2ae
import FileSystem.FileSystem;
import Handler.CommandHandler;
import Handler.PackageHandler;
import Protocol.BackupProtocol;
import Protocol.FileRestoreProtocol;
import Protocol.ReclaimProtocol;
import Protocol.DeleteProtocol;
import Handler.BackupHandler;

public class Peer implements PeerInterface {

	/**
	*	ServerID , Channels used, Protocols and File System
	*/
	private int id;
	private Channel MC;
	private Channel MDB;
	private Channel MDR;
	private FileSystem fileSystem;
	private BackupProtocol backup;
	private FileRestoreProtocol restore;
	private DeleteProtocol delete;
	private ReclaimProtocol reclaim;

	public static void main(String[] args) throws Exception {
		if (args.length != 7 && args.length != 2) {
			System.out.println("Usage:");
			System.out.println(
					"\tjava Service.Peer <server_id> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>");
			return;
		}

		/**
		*	Default values for channel addresses and ports
		*	Only need to insert ServerID
		*/
		if (args.length == 2) {
			Peer peer = new Peer(args[0], "224.0.0.0", "8000", "224.0.0.0", "8001", "224.0.0.0", "8002");
			try {

				/**
				*	RMI connection
				*/ 
				PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peer, 0);
				Registry registry = LocateRegistry.getRegistry();
	            registry.rebind(args[1], stub);
	            
	            System.err.println("Server ready");
			} catch (RemoteException e) {
				System.err.println("Cannot export RMI Object");
				System.exit(-1);
			}

			peer.run();

		}

		/**
		*	Insert full information - values for serverID, channel addresses and ports
		*/
		else if (args.length == 7) {
			Peer peer = new Peer(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
			try {

				/**
				*	RMI connection
				*/ 
				Registry registry = LocateRegistry.createRegistry(this.id);
				PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(this, this.id);
		
	            registry.bind("Peer", stub);
	            
	            System.err.println("Server ready");
			} catch (RemoteException e) {
				System.err.println("Cannot export RMI Object");
				System.exit(-1);
			}

			peer.run();
		}

	}

	/**
	 * Open sockets and have them join their multicast groups
	 */
	public Peer(String id, String mc_address, String mc_port, String mdb_address, String mdb_port, String mdr_address,
			String mdr_port) throws Exception {
		this.id = Integer.parseInt(id);		
		File file = new File("./storage/" + ".info" + this.id);
        if (file.exists()) {
            FileInputStream in = new FileInputStream(file);
            ObjectInputStream oin = new ObjectInputStream(in);
            this.fileSystem = (FileSystem) oin.readObject();
            this.fileSystem.init();
            oin.close();
            in.close();
        } else {
        	this.fileSystem = new FileSystem();
        }
        this.backup = new BackupProtocol(this);
        this.restore = new FileRestoreProtocol(this);
        this.delete = new DeleteProtocol(this);
        this.reclaim = new ReclaimProtocol(this);

		this.MC = new Channel(InetAddress.getByName(mc_address), Integer.parseInt(mc_port), "MC");
		this.MDB = new Channel(InetAddress.getByName(mdb_address), Integer.parseInt(mdb_port), "MDB");
		this.MDR = new Channel(InetAddress.getByName(mdr_address), Integer.parseInt(mdr_port), "MDR");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				MC.getSocket().close();
				MC.shutdown();
				MDB.getSocket().close();
				MDB.shutdown();
			}
		});
		try {
<<<<<<< HEAD

			/**
			*	RMI connection
			*/
			LocateRegistry.createRegistry(1099);
			Registry registry = LocateRegistry.getRegistry();
			PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(this, 0);
=======
			PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(this, this.id);
			Registry registry= LocateRegistry.createRegistry(this.id);
>>>>>>> 54640ba26e7198bf92ca12b570e9c54313d4c2ae
			
            registry.rebind("Peer", stub);
            
            System.err.println("Server ready");
		} catch (RemoteException e) {
			e.printStackTrace();
			System.err.println("Cannot export RMI Object");
			System.exit(-1);
		}
	}

	public void run() {
		Thread mc_thread = new Thread(MC);
		mc_thread.start();

		CommandHandler handler = CommandHandler.getInstance(this);
		handler.start();

		PackageHandler mcChannelHandler = new PackageHandler(MC);
		mcChannelHandler.start();

		PackageHandler mdbChannelHandler = new PackageHandler(MDB);
		mdbChannelHandler.start();

		PackageHandler mdrChannelHandler = new PackageHandler(MDR);
		mdrChannelHandler.start();
	}

	/**
	*	Auxiliar functions
	*/ 
	public int getId() {
		return id;
	}

	public Channel getMC() {
		return MC;
	}

	public Channel getMDB() {
		return MDB;
	}

	public Channel getMDR() {
		return MDR;
	}

	public FileSystem getFileSystem() {
		return fileSystem;
	}

	/**
	*	Backup Protocol
	*/
	public void putFile(String path, int replication) {
    	try{
    		backup.backupFile(path, replication);
            }catch(IOException e){
            	e.printStackTrace();
            }
	}

	/**
	*	Restore Protocol
	*/ 
	public void fileRestore(String path) {
    	try{
    		restore.restoreFile(path);
            }catch(IOException e){
            	e.printStackTrace();
            }
	}
	
	/**
	*	Delete Protocol
	*/
	public void deleteFile(String path) {
    	try{
    		delete.deleteFile(path);
            }catch(IOException e){
            	e.printStackTrace();
            }
	}
	
	/**
	*	Space Reclaim Protocol
	*/
	public void reclaimSpace(int space) {
    	try{
    		reclaim.reclaimSpace(space);
            }catch(IOException e){
            	e.printStackTrace();
            }
	}
	
	public String getStatus(){
<<<<<<< HEAD
		String ret = "";
        HashMap<String, FileChunk> backedUpFiles = fileSystem.getBackedUpFiles();
		ret += "\n--------- Backed up files ---------\n\n";
		
=======
        String ret = "";
        HashMap<String, FileChunk> backedUpFiles = fileSystem.getBackedUpFiles();
	ret += "\n--------- Backed up files ---------\n\n";
>>>>>>> 54640ba26e7198bf92ca12b570e9c54313d4c2ae
        for (String fileId : backedUpFiles.keySet()) {
            FileAttributes metadata = fileSystem.getFileAttributesByFileId(fileId);
            ret += "Pathname = '" + metadata.getPath() + "'\n";
            ret += "\tFile Id = " + fileId + "\n";
            ret += "\tDesired Replication Degree = " + fileSystem.getChunkDesiredReplicationDegree(fileId, 0) + "\n";
            HashMap<Integer, Chunk> chunks = fileSystem.getChunks(fileId);
<<<<<<< HEAD
            
			for (int chunkNo : chunks.keySet()) {
=======
            for (int chunkNo : chunks.keySet()) {
>>>>>>> 54640ba26e7198bf92ca12b570e9c54313d4c2ae
                ret += "\tChunk <" + fileId + ", " + chunkNo + ">\n";
                ret += "\t\tPerceived Replication Degree = " + fileSystem.getChunkReplication(fileId, chunkNo) + "\n";
            }
        }
<<<<<<< HEAD
 
        HashMap<String, FileChunk> storedChunks = fileSystem.getStoredChunks();
        if (storedChunks.size() != 0) {
             ret += "\n---------- Stored chunks ----------\n\n";
        }
        
		for (String fileId : storedChunks.keySet()) {
=======

        HashMap<String, FileChunk> storedChunks = fileSystem.getStoredChunks();
        if (storedChunks.size() != 0) {
            ret += "\n---------- Stored chunks ----------\n\n";
        }
        for (String fileId : storedChunks.keySet()) {
>>>>>>> 54640ba26e7198bf92ca12b570e9c54313d4c2ae
            HashMap<Integer, Chunk> chunks = fileSystem.getChunks(fileId);
            for (int chunkNo : chunks.keySet()) {
                ret += "Chunk <" + fileId + ", " + chunkNo + ">\n";
                ret += "\tSize = " + fileSystem.getChunk(fileId, chunkNo).getSize() / 1000.0f + " KByte\n";
                ret += "\tPerceived Replication Degree = " + fileSystem.getChunkReplication(fileId, chunkNo) + "\n";
            }
        }
<<<<<<< HEAD
 
        ret += "\nStorage = (" + fileSystem.getSpaceUsed() / 1000.0f + "/" + fileSystem.getSpace() / 1000.0f + ") KByte\n";
        return ret;
	}

	/**
	*	internal State Information
	*/
	public void intState(){
		System.out.println("IntState nao esta em funcionamento.");
	}
=======

        ret += "\nStorage = (" + fileSystem.getSpaceUsed() / 1000.0f + "/" + fileSystem.getSpace() / 1000.0f + ") KByte\n";
        return ret;
    }
>>>>>>> 54640ba26e7198bf92ca12b570e9c54313d4c2ae

}