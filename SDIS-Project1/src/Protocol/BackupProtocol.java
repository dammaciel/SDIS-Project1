package Protocol;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import FileSystem.FileSystem;
import Message.Header;
import Message.Message;
import Service.Channel;
import Service.Peer;

public class BackupProtocol {
	private Peer peer;
    private FileSystem fileSystem;
    
    public BackupProtocol(Peer peer) throws RemoteException {
        this.peer = peer;
        this.fileSystem = peer.getFileSystem();
    }
	
    public void backupFile(String path, int replicationDeg) throws IOException {
    	File file = new File(path);
		
		String filename = file.getName();
		
		Path filePath = Paths.get(path);
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        String modified = String.valueOf(attr.lastModifiedTime());
        String owner = String.valueOf(Files.getOwner(filePath));
        String fileId=filename+modified+owner;
        String hashFileId=Message.buildHash(fileId); 
        
        ArrayList<byte[]> chunks = getFileChunks(path, 64000);

        fileSystem.saveFile(path, hashFileId);
        for (int i = 0; i < chunks.size(); i++) {
            final int chunkNo = i;
            final byte[] chunk = chunks.get(i);
            fileSystem.saveChunk(hashFileId, chunkNo, replicationDeg);
            init(peer, hashFileId, chunkNo, chunk, replicationDeg);
        }
    }
    
    public static ArrayList<byte[]> getFileChunks(String filepath, int size) {
        ArrayList<byte[]> chunks = new ArrayList<>();
        int numRead = 0, read = 0;
        try {
            InputStream stream = new FileInputStream(filepath);
            byte[] temp = new byte[size];
            while ((read = stream.read(temp)) != -1) {
                byte[] chunk = Arrays.copyOfRange(temp, 0, read);
                chunks.add(chunk);
                numRead += read;
            }
            if (numRead % size == 0) {
                byte[] chunk = new byte[0];
                chunks.add(chunk);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chunks;
    }
    
    public void init (Peer peer, String fileId, int chunkNo, byte[] chunkData, int replication){
    	int attempt = 0;
        int time= 1000;
        int i=0;
        boolean done=false;
        
        Thread thread = new Thread();
        thread.start();
        
        while(attempt < 5 && !done){
        	 Header header = new Header("PUTCHUNK", "1.0", peer.getId(), fileId, chunkNo, replication);
        	 byte[] body = chunkData;
             Message msg = new Message(header, body);
             peer.getMDB().send(msg);
             
             try {
            	 Thread.sleep((long) (time * Math.pow(2, attempt)));
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             
             int currentReplication = fileSystem.getChunkReplication(fileId, chunkNo);
             System.out.println("Verificando replication: " + currentReplication + " - " + replication);
             if (currentReplication >= replication) {
                 done = true;
             } else {                
                 attempt ++;
             }
        }
        
        if (done) {
            System.out.println("Successfully stored <" + fileId + ", " + chunkNo + ">");
        }
        
        try {
            fileSystem.saveFileSystem(peer.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
