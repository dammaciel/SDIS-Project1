package Protocol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.util.ArrayList;

import FileSystem.FileSystem;
import Message.Header;
import Message.Message;
import Service.Peer;

public class DeleteProtocol {
	private Peer peer;
    private FileSystem fileSystem;
    
    public DeleteProtocol(Peer peer) throws RemoteException {
        this.peer = peer;
        this.fileSystem = peer.getFileSystem();
    }
    
    public void deleteFile(String path) throws IOException {
    	File file = new File(path);
		
		String filename = file.getName();
		
		Path filePath = Paths.get(path);
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        String modified = String.valueOf(attr.lastModifiedTime());
        String owner = String.valueOf(Files.getOwner(filePath));
        String fileId=filename+modified+owner;
        String hashFileId=Message.buildHash(fileId); 

        Header header = new Header("DELETE", "1.0", peer.getId(), hashFileId);
        Message msg = new Message(header, null);
        peer.getMDB().send(msg);
        
        try {
            fileSystem.saveFileSystem(peer.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
