package FileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;

public class FileSystem implements Serializable {
	private HashMap<String, FileChunk> files;
	private int space = 1024*1000;
	private int spaceUsed = 0;

	public FileSystem() {
		new File("./storage/").mkdir();
		this.files = new HashMap<>();
	}

	/**
	*	Creates Storage folder and calculates space used
	*/
	public void init() {
		for (String fileId : files.keySet()) {
			if (getFileAttributesByFileId(fileId) == null) {
				for (int chunkNo : getChunks(fileId).keySet()) {
					String filename = getChunkName(fileId, chunkNo);
					File f = new File("./storage/" + filename);
					if (!f.exists()) {
						spaceUsed -= getChunk(fileId, chunkNo).getSize();
						getChunks(fileId).remove(chunkNo);
					}
				}
			}
		}
	}

	/**
	*	Auxiliar functions
	*/
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

	/**
	*	Saves a file in a certain path
	*/
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

	/**
	*	Saves chunk for a certain replication degree
	*/
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

	/**
	*	Calculates chunk replication degree
	*/
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

	/**
	*	Saves peerID in the filesystem
	*/
	public void saveFileSystem(int peerId) throws IOException {
		FileOutputStream out = new FileOutputStream("./storage/" + ".info" + peerId);
		ObjectOutputStream oOut = new ObjectOutputStream(out);
		oOut.writeObject(this);
		oOut.close();
		out.close();
	}

	/**
	*	Saves chunk in /storage
	*/
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
			spaceUsed += data.length;
			file.getChunk(chunkNo).setSize(data.length);
		}
	}

	/**
	*	Creates new file
	*/
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

	/**
	*	Deletes a chunk in /storage
	*/
	public void deleteChunk(String fileId, int chunkNo) {
		String path = "./storage/" + getChunkName(fileId, chunkNo);
		try {
			Files.delete(Paths.get(path));
		} catch (NoSuchFileException e) {

		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		FileChunk file = getFile(fileId);
        if (file != null) {
            file.getChunks().remove(chunkNo);
        }

	}

	public void deleteFile(String fileId) {
		files.remove(fileId);
	}

	public int getSpace() {
		return space;
	}

	public int getSpaceUsed() {
		return spaceUsed;
	}

	public void removeSpaceUsed(int removedSpace) {
		this.spaceUsed -= removedSpace;
	}

	/**
	*	Gets a specific replication degree for a chunk
	*/
	public int getChunkDesiredReplicationDegree(String fileId, int chunkNo) {
		Chunk chunk = getChunk(fileId, chunkNo);
		if (chunk != null) {
			return chunk.getDesired();
		}
		return -1;
	}

	/**
	*	Restores a file
	*/
	public void restFile(int peerID, String fileId) {
		FileChunk file = getFile(fileId);
		if (file == null) {
			System.out.println("FileID vazio");
			return;
		}

	}

	public void getFile(String fileId, int chunkNo) {
		return;
	}

	/**
	*	Reclaims space
	*/
	public void reclaimSpace(int reclaimedSpace) {
		if (space >= reclaimedSpace) {
			space -= reclaimedSpace;
		} else {
			space = 0;
		}
	}

	public HashMap<String, Integer> getChunksForReclaim() {
		HashMap<String, Integer> chunks = new HashMap<>();
		for (String fileId : files.keySet()) {
			for (int nr : getChunks(fileId).keySet()) {
				int rd = getChunkReplication(fileId, nr);
				int desiredRd = getChunkDesiredReplicationDegree(fileId, nr);
				/*System.out.println(rd + "vs");
				System.out.println(desiredRd);*/
				if (rd > desiredRd) {
					chunks.put(fileId, nr);
				}
			}
		}
		return chunks;
	}

	public String getHighestReplicationDegreeChunkFileId() {
		String highest = null;
		int max = 0;
		for (String fileId : files.keySet()) {
			for (int nr : getChunks(fileId).keySet()) {
				int replicationDegree = getChunkReplication(fileId, nr);
				if (replicationDegree > max) {
					highest = fileId;
					max = replicationDegree;
				}
			}
		}
		return highest;
	}

	public int getHighestReplicationDegreeChunkNr() {
		int highest = -1;
		int max = 0;
		for (String fileId : files.keySet()) {
			for (int nr : getChunks(fileId).keySet()) {
				int replicationDegree = getChunkReplication(fileId, nr);
				if (replicationDegree > max) {
					highest = nr;
					max = replicationDegree;
				}
			}
		}
		return highest;
	}

	public void decreaseReplicationDegree(int peerId, String fileId, int chunkNo) {
		FileChunk file = getFile(fileId);
		if (file == null) {
			return;
		}
		Chunk chunk = file.getChunk(chunkNo);
		if (chunk != null) {
			chunk.decreaseReplication(peerId);
		}
	}
	
	public byte[] recoverChunk(String fileId, int chunkNo) {
        FileChunk file = getFile(fileId);
        if (file == null) {
            return null;
        }
        Chunk chunk = file.getChunk(chunkNo);
        if (chunk == null) {
            return null;
        }
        byte[] data;
        String path = "./storage/" + getChunkName(fileId, chunkNo);
        try {
            data = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }
	
	public FileAttributes getFileAttributes(String path) {
		System.out.println(files.size());
        for (FileChunk data : files.values()) {
            if (data.getAttributes().getPath().equals(path)) {
                return data.getAttributes();
            }
        }
        return null;
    }
	
	public void restoreChunk(String fileId, int chunkNo, byte[] data) {
        FileChunk file = getFile(fileId);
        if (file != null) {
            Chunk chunk = file.getChunk(chunkNo);
            if (chunk == null) {
                chunk = new Chunk();
                file.getChunks().put(chunkNo, chunk);
            }
            chunk.setData(data);
        }
    }
	
	  public HashMap<String, FileChunk> getBackedUpFiles() {
	        HashMap<String, FileChunk> backedUpFiles = new HashMap<>();
	        for (String fileId : files.keySet()) {
	            if (getFileAttributesByFileId(fileId)!= null) {
	                backedUpFiles.put(fileId, files.get(fileId));
	            }
	        }
	        return backedUpFiles;
	    }

	    public HashMap<String, FileChunk> getStoredChunks() {
	        HashMap<String, FileChunk> storedChunks = new HashMap<>();
	        for (String fileId : files.keySet()) {
	            if (getFileAttributesByFileId(fileId) == null) {
	                storedChunks.put(fileId, files.get(fileId));
	            }
	        }
	        return storedChunks;
	    }

}
