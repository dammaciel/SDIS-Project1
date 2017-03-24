package Protocol;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import Message.Header;
import Message.Message;
import Service.Channel;

public class BackupProtocol {
	public static boolean run(Channel MDB, String path, String version, int senderId, int replicationDeg) throws IOException {
		File file = new File(path);
		FileInputStream is = new FileInputStream(file);
		
		String filename = file.getName();

		Path filePath = Paths.get(path);
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        String lastModified = String.valueOf(attr.lastModifiedTime());
        String owner = String.valueOf(Files.getOwner(filePath));
        String fileId=filename+owner+lastModified;
        System.out.println(fileId);
        String hashFileId=Message.buildHash(fileId);  //tentar mudar
        
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        
        int i=0;
        while (true) {
            byte[] chunk = new byte[Message.CHUNK_SIZE];
            int bytesRead = bis.read(chunk);
            if (bytesRead == -1) {
                break;
            }
            Header header = new Header("PUTCHUNK", "1.0", 1, hashFileId, i, replicationDeg);

            byte[] body = Arrays.copyOf(chunk, bytesRead);
            Message msg = new Message(header, body);
            MDB.send(msg);
            
            i++;
        }
        bis.close();

        return true;
	}
}
