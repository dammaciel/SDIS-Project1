package FileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class FileSystem implements Serializable {
	private HashMap<String, FileChunk> files;

	public FileSystem() {
		new File("./storage/").mkdir();
		this.files = new HashMap<>();
	}

	public void init() {
		for (String fileId : files.keySet()) {
			if (getFileAttributesByFileId(fileId) == null) {
				for (int chunkNo : getChunks(fileId).keySet()) {
					String filename = getChunkName(fileId, chunkNo);
					File f = new File("./storage/" + filename);
					if (!f.exists()) {
						// usedSpace -= getChunk(fileId, chunkNo).getSize();
						getChunks(fileId).remove(chunkNo);
					}
				}
			}
		}
	}

	public FileAttributes getFileAttributesByFileId(String fileId) {
		FileChunk file = getFile(fileId);
		if (file != null) {
			return file.getAttributes();
		}
		return null;
	}

	public FileChunk getFile(String fileId) {
		return files.get(fileId);
	}

	private String getChunkName(String fileId, int chunkNo) {
		return fileId + "_" + chunkNo;
	}

	public HashMap<Integer, Chunk> getChunks(String fileId) {
		FileChunk file = getFile(fileId);
		if (file != null) {
			return file.getChunks();
		}
		return null;
	}
	
	public void saveFile(String path, String fileId) {
        FileChunk file = getFile(fileId);
        if (file == null) {
            try {
                files.put(fileId, new FileChunk(new FileAttributes(path, fileId)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	public void saveChunk(String fileId, int chunkNo, int replicationDeg) {
        FileChunk file = getFile(fileId);
        if (file == null) {
            files.put(fileId, new FileChunk());
            file = getFile(fileId);
        }
        Chunk chunk = file.getChunk(chunkNo);
        if (chunk == null) {
            file.addChunk(chunkNo, replicationDeg);
        }
    }
	
	public int getChunkReplication(String fileId, int chunkNo) {
        Chunk chunk = getChunk(fileId, chunkNo);
        if (chunk != null) {
            return chunk.getReplication();
        }
        return -1;
    }
	
	public Chunk getChunk(String fileId, int chunkNo) {
        FileChunk file = files.get(fileId);
        if (file != null) {
            return file.getChunks().get(chunkNo);
        }
        return null;
    }
	
	public void saveFileSystem(int peerId) throws IOException {
        FileOutputStream out = new FileOutputStream("./storage/" +  ".info"  + peerId);
        ObjectOutputStream oOut = new ObjectOutputStream(out);
        oOut.writeObject(this);
        oOut.close();
        out.close();
    }
	
	public void saveChunk(int peerId, String fileId, int chunkNo, int replication, byte[] data) throws IOException {        
        FileChunk file = getFile(fileId);
        if (file == null) {
            files.put(fileId, new FileChunk());
            file = getFile(fileId);
        }
        Chunk chunk = file.getChunk(chunkNo);
        if (chunk == null) {
            String filepath = "./storage/" + getChunkName(fileId, chunkNo);
            createFile(filepath, data);
            file.addChunk(chunkNo, peerId, replication);
        }
    }
	
	public static void createFile(String filepath, byte[] data) throws IOException, FileNotFoundException {
        File file = new File(filepath);
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file, false);
        if (data != null) {
            out.write(data);
        }
        out.close();
    }
	
	public void incrementReplication(int peerId, String fileId, int chunkNo) {
        FileChunk file = getFile(fileId);
        if (file == null) {
            return;
        }
        Chunk chunk = file.getChunk(chunkNo);
        if (chunk != null) {
            chunk.incrementReplication(peerId);
        }
    }

}
