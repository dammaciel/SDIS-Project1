package Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Handler.CommandHandler;
import Handler.PackageHandler;
import Protocol.BackupProtocol;
import Handler.BackupHandler;

public class Peer implements PeerInterface{
	private int id;
	private Channel MC;
	private Channel MDB;
	private Channel MDR;
	
	public static void main(String[] args) throws Exception{
		 if (args.length != 7) {
			System.out.println("Usage:");
	        System.out.println("\tjava Service.Peer <server_id> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>");
			return; 
		 }
		 
            Peer peer = new Peer(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
            try {
                PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peer, Integer.parseInt(args[0]));
                Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
                registry.rebind("Peer",stub);
            } catch (RemoteException e) {
                System.err.println("Cannot export RMI Object");
                System.exit(-1);
            }
            
            peer.run();
            
    }
	
	/**
     * Open sockets and have them join their multicast groups
     */
	public Peer(String id, String mc_address,String mc_port,String mdb_address, String mdb_port,String mdr_address,String mdr_port) throws Exception {
		this.id = Integer.parseInt(id);		
		
        this.MC = new Channel(InetAddress.getByName(mc_address), Integer.parseInt(mc_port),"MC");
        this.MDB = new Channel(InetAddress.getByName(mdb_address), Integer.parseInt(mdb_port),"MDB");
        this.MDR = new Channel(InetAddress.getByName(mdr_address), Integer.parseInt(mdr_port),"MDR");

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                MC.getSocket().close();
                MC.shutdown();
                MDB.getSocket().close();
                MDB.shutdown();
            }
        });
    }
	
	public void run(){
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
	
    public void putFile(String filePath, int replication) {
    	try{
    		BackupProtocol.run(MDB, filePath,"1.0",id,replication);  //enquando o handler n funciona
            }catch(IOException e){
            	e.printStackTrace();
            }
    }
    
    
	
	
}
