package FileSystem;

import java.util.HashMap;

public class File {
	private String id;
    private HashMap<Integer, Chunk> chunks;
    private String path;
    
    public File(String id, String path) {
        this.id = id;
        this.path = path;
        this.chunks = new HashMap<>();
        FileSystem.instance().addFile(id, this);   
    }

    public void addChunk(Chunk chunk) {
        chunks.put(chunk.getChunk_nr(), chunk);
    }

    public Chunk getChunk(int chunkNo) {
        return chunks.get(chunkNo);
    }

    public boolean hasChunk(int chunkNo) {
        return chunks.containsKey(chunkNo);
    }
}
