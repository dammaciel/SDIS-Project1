package FileSystem;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import Message.Message;

public class Chunk implements Serializable{
	private int chunk_nr;
    private int replicationD;
    private byte[] data;
    private String path;
    
    public Chunk(int chunk_nr, int replicationD, byte[] _data, String path) {
        this.chunk_nr = chunk_nr;
        this.replicationD = replicationD;
        this.data = _data;
        this.path= path;
    }

	public int getChunk_nr() {
		return chunk_nr;
	}

	public int getReplicationD() {
		return replicationD;
	}

	public byte[] getData() {
		return data;
	}
    
	public void saveData() throws IOException {
        Path pathToFile = Paths.get(this.path);
        Files.createDirectories(pathToFile.getParent());
        Files.write(pathToFile, this.data);
        this.data = null;
    };

}
