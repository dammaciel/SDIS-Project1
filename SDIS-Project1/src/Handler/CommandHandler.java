package Handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import FileSystem.Chunk;
import FileSystem.FileSystem;
import Handler.CommandHandler;
import Message.Header;
import Message.Message;
import Service.Peer;

public class CommandHandler extends Thread {
	private static Peer peer;
	private LinkedBlockingQueue<byte[]> commands;
	private static CommandHandler commandHandler = null;

	private CommandHandler(Peer peer) {
		this.peer = peer;
		this.commands = new LinkedBlockingQueue<>();
	}

	public static CommandHandler getInstance() {
		return commandHandler;
	}

	public static CommandHandler getInstance(Peer peer) {
		if (commandHandler == null) {
			commandHandler = new CommandHandler(peer);
		}
		return commandHandler;
	}

	public void addCommand(byte[] command) {
		try {
			commands.put(command);
		} catch (InterruptedException e) {
			return;
		}
	}

	public void run() {
		boolean done = false;
		while (!done) {
			try {
				byte[] command = commands.take();

				new Thread(new Runnable() {
					@Override
					public void run() {
						String handledCommand = handleCommand(command);
					}
				}).start();
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private String handleCommand(byte[] commandPacket) {
		try {
			Message msg = new Message(commandPacket);
			String requestName = msg.getHeader().getFileId() + "_" + msg.getHeader().getChunkNo();
			switch (msg.getHeader().getMessageType()) {
			case "PUTCHUNK":
				handlePutChunk(msg);
				break;
			case "STORED":
				handleStored(msg);
				break;
			case "GETCHUNK":
				handleGetChunk(msg);
				break;
			case "CHUNK":
				handleChunk(msg);
				break;
			case "DELETE":
				handleDelete(msg);
				break;
			case "REMOVED":

				break;
			default:
				System.out.println("Unrecognized command. Disregarding");
				break;
			}

			return msg.getHeader().getMessageType();
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {

		}
		return "ERROR";
	}

	private void handlePutChunk(Message m) throws IOException {
		if (m.getHeader().getSenderId() != peer.getId()) {
			System.out.println("Received PUTCHUNK :" + m.getHeader().getFileId() + " - " + m.getHeader().getChunkNo());
			try {
                peer.getFileSystem().saveChunk(peer.getId(), m.getHeader().getFileId(), m.getHeader().getChunkNo(), m.getHeader().getReplicationDeg(), m.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
			
			
			Header response_header = new Header("STORED", "1.0", peer.getId(), m.getHeader().getFileId(), m.getHeader().getChunkNo(), m.getHeader().getReplicationDeg());
			Message response = new Message(response_header, null);
			
			Random rand = new Random();
			try {
	            Thread.sleep(rand.nextInt(401));
	        } catch (InterruptedException e) { e.printStackTrace(); }
			
			MulticastSocket socket = peer.getMC().getSocket();
			DatagramPacket packet = new DatagramPacket(response.getBytes(), response.getBytes().length,
					peer.getMC().getAddress(), peer.getMC().getPort());
			socket.send(packet);

		}else{
			System.out.println("Received mine PUTCHUNK");
		}
		
		try {
            peer.getFileSystem().saveFileSystem(peer.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void handleStored(Message msg){

        String fileId = msg.getHeader().getFileId();
        int senderId = msg.getHeader().getSenderId();
        int chunkNo = msg.getHeader().getChunkNo();

        System.out.println("Received STORED:" + fileId + " - " + chunkNo);

        peer.getFileSystem().incrementReplication(senderId, fileId, chunkNo);
        try {
            peer.getFileSystem().saveFileSystem(peer.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void handleDelete(Message msg){
		System.out.println("Received DELETE: " + msg.getHeader().getFileId());
		String fileId=msg.getHeader().getFileId();
		if (peer.getFileSystem().getFile(fileId) != null) {
            for (int chunkNo : peer.getFileSystem().getChunks(fileId).keySet()) {
                peer.getFileSystem().deleteChunk(fileId, chunkNo);
                peer.getFileSystem().removeSpaceUsed(peer.getFileSystem().getChunk(fileId, chunkNo).getSize());
            }
            peer.getFileSystem().deleteFile(fileId);
        }
	}

	public void handleGetChunk(Message m){

        System.out.println("Received GETCHUNK :" + m.getHeader().getFileId() + " " + m.getHeader().getChunkNo());
			
			
		Header response_header = new Header("CHUNK", "1.0", peer.getId(), m.getHeader().getFileId(), m.getHeader().getChunkNo());
		Message response = new Message(response_header, m.getBody());
			
		Random rand = new Random();
			try {
	            Thread.sleep(rand.nextInt(401));
	        } catch (InterruptedException e) { e.printStackTrace(); }
			
		MulticastSocket socket = peer.getMC().getSocket();
		DatagramPacket packet = new DatagramPacket(response.getBytes(), response.getBytes().length,
					peer.getMC().getAddress(), peer.getMC().getPort());
	
		try {
            socket.send(packet);
        } catch (IOException e) {
        	 System.out.println("Cannot send packet");
            e.printStackTrace();
        }
		
		try {
        	peer.getFileSystem().saveFileSystem(peer.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
      /*
        Chunk recoverChunk = Database.getChunk(chunk);
            Message chunkMsg = new Message(MessageType.CHUNK, peerID, recoverChunk.fileID, recoverChunk.chunkNo);
            chunkMsg.body = recoverChunk.data;

            ChunkHandler chunkHandler = new ChunkHandler(chunk);
            mdr.addHandler(chunkHandler);

            int timeoutDelay = Utils.random(0, 400);
            Thread t = new Thread(new Timeout(timeoutDelay));
            t.start();

            while (true) {
                if (chunkHandler.received()) {
                    break;
                }
                if (!t.isAlive()) {
                    Log.info("Sending chunk ("+recoverChunk+")");
                    mdr.send(chunkMsg);
                    break;
                }
            }

            mdr.removeHandler(chunkHandler);
        */
	}
	
	
	public void handleChunk(Message msg){
		String fileId = msg.getHeader().getFileId();
      	int chunkNo = msg.getHeader().getChunkNo();
      	int senderId = msg.getHeader().getSenderId();
        
		System.out.println("Received FileRestore: " + fileId + " - " + chunkNo);
		
		try {
            peer.getFileSystem().saveFileSystem(peer.getId());
        } catch (IOException e) {
        	 System.out.println("Cannot save file system");
            e.printStackTrace();
        }
	}

	public static Peer getPeer() {
		return peer;
	}
	
	
}
