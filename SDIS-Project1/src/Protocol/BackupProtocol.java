package Protocol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import Service.Peer;

import Utils.Utils;

public class BackupProtocol implements Runnable {
	private Peer peer;
	File file;
    int replD;

    
    public BackupProtocol(Peer peer, File file, int replD) {
    	this.peer= peer;
        this.file = file;
        this.replD = replD;
    }
    
	@Override
	public void run() {
		try{
		FileSpliter spliter = new FileSpliter (file, replD);
		Chunk[] chunks = spliter.getChunks();
		
		for (Chunk c : chunks) {
			Header messageHeader = new Header("PUTCHUNK", "1.0", peer.getId(), c.fileID, c.chunkNo, c.replDeg);
	        Message msg = new Message(messageHeader, c.data);
	        DatagramPacket requestPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, peer.getMDB_address(), peer.getMDB_port());

        }
		
		}catch(Exception e){
			
		}
	}

}
