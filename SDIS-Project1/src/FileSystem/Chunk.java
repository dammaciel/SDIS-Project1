package FileSystem;

import java.io.Serializable;

import Message.Message;

public class Chunk implements Serializable{
	private int chunk_nr;
    private int replicationD;
    private byte[] data;
    
    public Chunk(int chunk_nr, int replicationD, byte[] _data) {
        this.chunk_nr = chunk_nr;
        this.replicationD = replicationD;
        data = _data;
    }
    
    public Chunk(Message m) {
        this(m.getHeader().getChunkNo(), m.getHeader().getReplicationDeg(), m.body);
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
    
    

}
