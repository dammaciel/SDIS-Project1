package FileSystem;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import Message.Message;

public class Chunk implements Serializable{
	private ArrayList<Integer> replication;
    private int desired;
    private byte[] data;
    private int size;

    public Chunk(){}
    
    public Chunk(int desired) {
        this.replication = new ArrayList<>();
        this.desired = desired;
        data = null;
        size = 0;
    }

    public Chunk(int peerId, int desired) {
        replication = new ArrayList<>();
        this.replication.add(peerId);
        this.desired = desired;
        data = null;
        size = 0;
    }
    
	public byte[] getData() {
		return data;
	}

	public int getReplication() {
		return replication.size();
	}

	public int getDesired() {
		return desired;
	}

	public int getSize() {
		return size;
	}

    public void setData(byte[] data) {
        this.data = data;
    }
	
	public void incrementReplication(int peerId) {
        if (!replication.contains(peerId)) {
            replication.add(peerId);
        }
    }
	
	public void decreaseReplication(int peerId) {
        if (replication.contains(peerId)) {
            replication.remove(new Integer(peerId));
        }
    }
}
