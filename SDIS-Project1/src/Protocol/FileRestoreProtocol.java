package Protocol;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import FileSystem.Chunk;
import FileSystem.FileAttributes;
import FileSystem.FileSystem;
import Message.Header;
import Message.Message;
import Service.Channel;
import Service.Peer;

public class FileRestoreProtocol {
	private Peer peer;
	private FileSystem fileSystem;

	public FileRestoreProtocol(Peer peer) throws RemoteException {
		this.peer = peer;
		this.fileSystem = peer.getFileSystem();
	}

	public void restoreFile(String path) throws IOException {
		FileAttributes attributes = fileSystem.getFileAttributes(path);
		if (attributes == null) {
			return;
		}
		String fileId = attributes.getFileId();
		HashMap<Integer, Chunk> chunks = fileSystem.getFile(fileId).getChunks();

		boolean done = false;
		int attempt = 0;
		int time = 1000;
		while (attempt < 5 && !done) {
			for (int i = 0; i < chunks.size(); i++) {
				if (fileSystem.getFile(fileId).getChunk(i).getData() == null) {
					final int chunkNo = i;
					Header header = new Header("GETCHUNK", "1.0", peer.getId(), fileId, i);
					Message msg = new Message(header, null);
					peer.getMC().send(msg);

					try {
						fileSystem.saveFileSystem(peer.getId());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep((long) (time * Math.pow(2, attempt)));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			done = true;
			for (int j = 0; j < chunks.size(); j++) {
				if (fileSystem.getFile(fileId).getChunk(j).getData() == null) {
					done = false;
				}
			}
		}
		if (done) {
            try {
            	File file = new File(path);
            	file.createNewFile();
            	
            	
                for (int i = 0; i < chunks.size(); i++) {
                    byte[] data = fileSystem.getFile(fileId).getChunk(i).getData();
                    FileOutputStream out = new FileOutputStream(path, true);
                    out.write(data);
                    out.close();
                }
                Files.setLastModifiedTime(Paths.get(path), FileTime.fromMillis(attributes.getLastModified()));
                System.out.println("Recovered file: " + path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

	public static ArrayList<byte[]> getFileChunks(String filepath, int size) {
		ArrayList<byte[]> chunks = new ArrayList<>();
		int numRead = 0, read = 0;
		try {
			InputStream stream = new FileInputStream(filepath);
			byte[] temp = new byte[size];
			while ((read = stream.read(temp)) != -1) {
				byte[] chunk = Arrays.copyOfRange(temp, 0, read);
				chunks.add(chunk);
				numRead += read;
			}
			if (numRead % size == 0) {
				byte[] chunk = new byte[0];
				chunks.add(chunk);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return chunks;
	}

	public void init(Peer peer, String fileId, int chunkNo, byte[] chunkData) {

		Header header = new Header("GETCHUNK", "1.0", peer.getId(), fileId, chunkNo);
		byte[] body = chunkData;
		Message msg = new Message(header, body);
		peer.getMDR().send(msg);

		System.out.println("\tRESTORED: " + chunkNo);

		try {
			fileSystem.saveFileSystem(peer.getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}