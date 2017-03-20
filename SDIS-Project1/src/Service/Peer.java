package Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import Handler.CommandHandler;
import Handler.PackageHandler;
import Protocol.BackupProtocol;

//Represents a Server Peer
public class Peer {
	private int id;
	private ServerSocket serverSocket;
	
	// The UDP multicast Sockets that are run on each peer
	private MulticastSocket MC;
	private MulticastSocket MDB;
	private MulticastSocket MDR;
	
	// The multicast port of the corresponding socket
    private int MC_port;
    private int MDB_port;
    private int MDR_port;

    // The multicast address of the corresponding socket
    private InetAddress MC_address;
    private InetAddress MDB_address;
    private InetAddress MDR_address;
	
	
    public static void main(String[] args) {
    	
    	if(args.length != 7){
            System.err.println("Usage: <serverID> <mcIP> <mcPort> <mdbIP> <mdbPort> <mdrIP> <mdrPort>");
            System.exit(-1);
        }
    	
    	int peerId = Integer.parseInt(args[0]);
        String mcIP = args[1];
        String mdbIP = args[3];
        String mdrIP = args[5];
        int MC_port = Integer.parseInt(args[2]);
        int MDB_port = Integer.parseInt(args[4]);
        int MDR_port = Integer.parseInt(args[6]);
        InetAddress MC_address = null;
        InetAddress MDB_address = null;
        InetAddress MDR_address = null;
        try {
            MC_address = InetAddress.getByName(mcIP);
            MDB_address = InetAddress.getByName(mdbIP);
            MDR_address = InetAddress.getByName(mdrIP);
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        
        Peer p = new Peer(peerId, MC_port, MDB_port, MDR_port, MC_address, MDB_address, MDR_address);
        
        CommandHandler.getInstance(p);
        p.joinMulticastGroups();

        p.startHandlers();
    }
    
	public Peer(int _id, int mcPort, int mdbPort,int mdrPort, InetAddress mcAddress, InetAddress mdbAddress, InetAddress mdrAddress){
		this.id = _id;
	    this.MC_port = mcPort;
	    this.MDB_port = mdbPort;
	    this.MDR_port = mdrPort;
	    this.MC_address = mcAddress;
	    this.MDB_address = mdbAddress;
	    this.MDR_address = mdrAddress;
	}
	
	/*
	 * Open sockets and join multicast groups
	 */
	private void joinMulticastGroups() {
        try {
            MC = new MulticastSocket(MC_port);
            MC.setTimeToLive(1);
            MDB = new MulticastSocket(MDB_port);
            MDB.setTimeToLive(1);
            MDR = new MulticastSocket(MDR_port);
            MDR.setTimeToLive(1);

            MC.joinGroup(MC_address);
            MDB.joinGroup(MDB_address);
            MDR.joinGroup(MDR_address);
        } catch (IOException e) {
            System.err.println("Cannot join to all multicast channels");
            System.exit(-1);
        }

        System.out.println("Succesfully joined groups");
    }
	
	public void startHandlers(){
        CommandHandler handler = CommandHandler.getInstance(this);
        handler.start();

        PackageHandler mc_handler = new PackageHandler(MC, MC_address, MC_port, "MC");
        mc_handler.start();

        PackageHandler mdb_handler = new PackageHandler(MDB, MDB_address, MDB_port, "MDB");
        mdb_handler.start();

        PackageHandler mdr_handler = new PackageHandler(MDR, MDR_address, MDR_port, "MDR");
        mdr_handler.start();
    }
	
	void run(){
		boolean done = false;
		while(!done){
			Socket socket =null;
			try {
                socket = serverSocket.accept();
            } catch (Exception e) { 
            	e.printStackTrace(); 
            	}
			try{
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String request = br.readLine();
			String[] tokens = request.split(" ");
			switch(tokens[0]){
				case "BACKUP":
					int replicationDeg = Integer.parseInt(tokens[2]);
                    try {
                        File file = new File(tokens[1]);
                        backup(file, replicationDeg);
                    } catch (Exception e) { e.printStackTrace(); }
					break;
				case "RESTORE":
					break;
				case "DELETE":
					break;
				case "RECLAIM":
					break;
				case "CLEAR":
					break;
				default:
					break;
			}
			}catch(IOException e){
				
			}
		}
		try { 
			serverSocket.close(); 
			} catch (IOException e) { 
				e.printStackTrace(); 
			}
        System.exit(0);
	}
	
	private void backup (File f, int replD){
		new Thread(new BackupProtocol(this, f, replD)).start();
	}

	public int getId() {
		return id;
	}

	public int getMDB_port() {
		return MDB_port;
	}

	public InetAddress getMDB_address() {
		return MDB_address;
	}
	
	
}

