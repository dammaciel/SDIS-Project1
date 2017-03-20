package Protocol;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import Utils.Utils;

public class FileSpliter {
	Chunk[] chunks;
	final int MAX_SIZE = 64000;
	
	public FileSpliter(File file, int replD) throws Exception{
		String fileId = Utils.getFileID(file);
		byte[] buffer = new byte[MAX_SIZE];
		
		int n_chunks = (int)(file.length() / MAX_SIZE) + 1;
		chunks = new Chunk[n_chunks];
		BufferedInputStream b = new BufferedInputStream(new FileInputStream(file));
		int read;
        for (int i = 0; i < n_chunks; ++i) {
            read = b.read(buffer);
            byte[] content = Arrays.copyOfRange(buffer, 0, (read>=0 ? read : 0));
            chunks[i] = new Chunk(fileId, i, replD, content);
        }
        b.close();
	}
	
	public Chunk[] getChunks() {
        return chunks;
    }
	
	public static void main(String[] args){
		try{
			FileSpliter spliter =  new FileSpliter(new File("example.txt"), 1); //colocar o nome correcto	
			Chunk[] chunks = spliter.getChunks();
			for(int i = 0; i< chunks.length; i++){
				Message put_m = new Message (
						new Header("PUTCHUNK", "1.0", 1, chunks[i].fileID, chunks[i].chunkNo, chunks[i].getReplDeg()), 
						chunks[i].data);
				Message received = new Message (put_m.getBytes());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
