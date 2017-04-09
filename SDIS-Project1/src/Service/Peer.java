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
import java.rmi.server.UnicastRemoteObject;

import FileSystem.FileSystem;
import Handler.CommandHandler;
import Handler.PackageHandler;
import Protocol.BackupProtocol;
import Protocol.FileRestoreProtocol;
import Protocol.DeleteProtocol;
import Handler.BackupHandler;

public class Peer implements PeerInterface {
	private int id;
	private Channel MC;
	private Channel MDB;
	private Channel MDR;
	private FileSystem fileSystem;
	private BackupProtocol backup;
	private FileRestoreProtocol restore;
	private DeleteProtocol delete;

	public static void main(String[] args) throws Exception {
		if (args.length != 7 && args.length != 1) {
			System.out.println("Usage:");
			System.out.println(
					"\tjava Service.Peer <server_id> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>");
			return;
		}

		if (args.length == 1) {
			Peer peer = new Peer(args[0], "224.0.0.0", "8000", "224.0.0.0", "8001", "224.0.0.0", "8002");
			try {
				PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peer, Integer.parseInt(args[0]));
				Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
				registry.rebind("Peer", stub);
			} catch (RemoteException e) {
				System.err.println("Cannot export RMI Object");
				System.exit(-1);
			}

			peer.run();

		}

		else if (args.length == 7) {
			Peer peer = new Peer(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
			try {
				PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peer, Integer.parseInt(args[0]));
				Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
				registry.rebind("Peer", stub);
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

	public void putFile(String path, int replication) {
    	try{
    		backup.backupFile(path, replication);
            }catch(IOException e){
            	e.printStackTrace();
            }
	}

	public void fileRestore(String path) {
    	try{
    		restore.restoreFile(path);
            }catch(IOException e){
            	e.printStackTrace();
            }
	}
	
	public void deleteFile(String path) {
    	try{
    		delete.deleteFile(path);
            }catch(IOException e){
            	e.printStackTrace();
            }
	}

}
