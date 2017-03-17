package Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

//Represents a Server Peer
public class Peer {
	private int id;
	private FileSystem fs;
	
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
    }
    
	public Peer(int _id, int mcPort, int mdbPort,int mdrPort, InetAddress mcAddress, InetAddress mdbAddress, InetAddress mdrAddress){
		this.id = _id;
	    this.MC_port = mcPort;
	    this.MDB_port = mdbPort;
	    this.MDR_port = mdrPort;
	    this.MC_address = mcAddress;
	    this.MDB_address = mdbAddress;
	    this.MDR_address = mdrAddress;

		fs= new FileSystem(id);
	}

	
	/*
	 * Gets
	 */
	public int getId() {
		return id;
	}

	public MulticastSocket getMC() {
		return MC;
	}

	public MulticastSocket getMDB() {
		return MDB;
	}

	public MulticastSocket getMDR() {
		return MDR;
	}

	public int getMC_port() {
		return MC_port;
	}

	public int getMDB_port() {
		return MDB_port;
	}

	public int getMDR_port() {
		return MDR_port;
	}

	public InetAddress getMC_address() {
		return MC_address;
	}

	public InetAddress getMDB_address() {
		return MDB_address;
	}

	public InetAddress getMDR_address() {
		return MDR_address;
	}
	
	/*
	 * Open sockets and join multicast groups
	 */
	private void joinMulticastGroups() {
        try {
            MC = new MulticastSocket(MC_port);
            MDB = new MulticastSocket(MDB_port);
            MDR = new MulticastSocket(MDR_port);

            MC.setTimeToLive(1);
            MDB.setTimeToLive(1);
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
	
}

