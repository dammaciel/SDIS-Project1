package FileSystem;

import java.io.Serializable;
import java.util.HashMap;

public class FileChunk implements Serializable{
	private FileAttributes attributes;
	private HashMap<Integer, Chunk> chunks;

	public FileChunk() {
		attributes = null;
		chunks = new HashMap<>();
	}

	public FileChunk(FileAttributes attributes) {
		this.attributes = attributes;
		chunks = new HashMap<>();
	}

	public FileAttributes getAttributes() {
		return attributes;
	}

	public HashMap<Integer, Chunk> getChunks() {
		return chunks;
	}

	public Chunk getChunk(Integer chunkNo) {
		return chunks.get(chunkNo);
	}

	public void addChunk(int chunkNo, int desiredReplicationDeg) {
		chunks.put(chunkNo, new Chunk(desiredReplicationDeg));
	}

	public void addChunk(int chunkNo, int peerId, int desired) {
		chunks.put(chunkNo, new Chunk(peerId, desired));
	}
}
