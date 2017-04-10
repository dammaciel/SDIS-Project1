package Protocol;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import FileSystem.FileSystem;
import Message.Header;
import Message.Message;
import Service.Peer;

public class ReclaimProtocol {
	private Peer peer;
	private FileSystem fileSystem;


	public ReclaimProtocol(Peer peer) throws RemoteException {
		this.peer = peer;
		this.fileSystem = peer.getFileSystem();
	}

	public void reclaimSpace(int space) throws IOException {
		
		this.fileSystem.reclaimSpace(space);
		HashMap<String, Integer> toRemove = this.fileSystem.getChunksReclaim();
		System.out.println(toRemove.size());
		for (Map.Entry<String, Integer> pair : toRemove.entrySet()) {
			System.out.println("Entrei no reclaim2");
			String fileId = pair.getKey();
			int chunkNo = pair.getValue();

			System.out.println("\nDeleting in order to reclaim space...");

			this.fileSystem.deleteFileOfChunk(fileId, chunkNo);
			this.fileSystem.removeSpaceUsed(this.fileSystem.getChunk(fileId, chunkNo).getSize());
			this.fileSystem.deleteChunk(fileId, chunkNo);

			Header header = new Header("REMOVED", "1.0", peer.getId(), fileId, chunkNo);
			Message msg = new Message(header, null);
			this.peer.getMC().send(msg);

			if ((fileSystem.getSpace() - fileSystem.getSpaceUsed()) >= 0) {
				break;
			}
		}
		
		while((fileSystem.getSpace() - fileSystem.getSpaceUsed()) <0){
			String fileId=this.fileSystem.getHighestReplicationDegreeChunkFileId();
			int chunkNo = this.fileSystem.getHighestReplicationDegreeChunkNr();
			this.fileSystem.deleteFileOfChunk(fileId, chunkNo);
			this.fileSystem.removeSpaceUsed(this.fileSystem.getChunk(fileId, chunkNo).getSize());
			this.fileSystem.deleteChunk(fileId, chunkNo);

			Header header = new Header("Removed", "1.0", peer.getId(), fileId, chunkNo);
			Message msg = new Message(header, null);
			this.peer.getMC().send(msg);
		}
		
		try {
            this.fileSystem.saveFileSystem(peer.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
