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
				handleRemoved(msg);
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
				peer.getFileSystem().saveChunk(peer.getId(), m.getHeader().getFileId(), m.getHeader().getChunkNo(),
						m.getHeader().getReplicationDeg(), m.getBody());
			} catch (IOException e) {
				e.printStackTrace();
			}

			Header response_header = new Header("STORED", "1.0", peer.getId(), m.getHeader().getFileId(),
					m.getHeader().getChunkNo(), m.getHeader().getReplicationDeg());
			Message response = new Message(response_header, null);

			Random rand = new Random();
			try {
				Thread.sleep(rand.nextInt(401));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			MulticastSocket socket = peer.getMC().getSocket();
			DatagramPacket packet = new DatagramPacket(response.getBytes(), response.getBytes().length,
					peer.getMC().getAddress(), peer.getMC().getPort());
			socket.send(packet);

		} else {
			System.out.println("Received mine PUTCHUNK");
		}

		try {
			peer.getFileSystem().saveFileSystem(peer.getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handleStored(Message msg) {

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

	public void handleDelete(Message msg) {
		System.out.println("Received DELETE: " + msg.getHeader().getFileId());
		String fileId = msg.getHeader().getFileId();
		if (peer.getFileSystem().getFile(fileId) != null) {
			for (int chunkNo : peer.getFileSystem().getChunks(fileId).keySet()) {
				peer.getFileSystem().deleteChunk(fileId, chunkNo);
				peer.getFileSystem().removeSpaceUsed(peer.getFileSystem().getChunk(fileId, chunkNo).getSize());
			}
			peer.getFileSystem().deleteFile(fileId);
		}
	}

	public void handleGetChunk(Message m) {

		System.out.println("Received GETCHUNK :" + m.getHeader().getFileId() + " " + m.getHeader().getChunkNo());
		if (m.getHeader().getSenderId() != peer.getId()) {
			byte[] data = peer.getFileSystem().retrieveChunkData(m.getHeader().getFileId(), m.getHeader().getChunkNo());
			if (data != null) {
				Header response_header = new Header("CHUNK", "1.0", peer.getId(), m.getHeader().getFileId(),
						m.getHeader().getChunkNo());
				Message response = new Message(response_header, data);
				Random rand = new Random();
				try {
					Thread.sleep(rand.nextInt(401));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				peer.getMDR().send(response);
			}
		}

		try {
			peer.getFileSystem().saveFileSystem(peer.getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handleChunk(Message msg) {
		String fileId = msg.getHeader().getFileId();
		int chunkNo = msg.getHeader().getChunkNo();
		int senderId = msg.getHeader().getSenderId();

		System.out.println("Received FileRestore: " + fileId + " - " + chunkNo);
		peer.getFileSystem().restoreChunk(fileId, chunkNo, msg.body);
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

	public void handleRemoved(Message msg) {
		System.out.println("Received REMOVED: " + msg.getHeader().getFileId() + msg.getHeader().getChunkNo());
		FileSystem fs = peer.getFileSystem();
		if (msg.getHeader().getSenderId() != peer.getId()) {
			if (fs.getChunk(msg.getHeader().getFileId(), msg.getHeader().getChunkNo()) != null) {
				fs.decreaseReplicationDegree(msg.getHeader().getSenderId(), msg.getHeader().getFileId(),
						msg.getHeader().getChunkNo());
				if (fs.getFileAttributesByFileId(msg.getHeader().getFileId()) != null) {
					return;
				}
				int actual = fs.getChunkReplication(msg.getHeader().getFileId(), msg.getHeader().getChunkNo());
				int desired = fs.getChunkDesiredReplicationDegree(msg.getHeader().getFileId(),
						msg.getHeader().getChunkNo());

				if (actual < desired) {
					Random rand = new Random();
					try {
						Thread.sleep(rand.nextInt(401));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					byte[] data = fs.retrieveChunkData(msg.getHeader().getFileId(), msg.getHeader().getChunkNo());
					boolean done = false;
					int attempt = 0;
					int time = 1000;

					while (attempt < 5 && !done) {
						Header header = new Header("PUTCHUNK", "1.0", peer.getId(), msg.getHeader().getFileId(),
								msg.getHeader().getChunkNo(), msg.getHeader().getReplicationDeg());

						Message new_msg = new Message(header, data);
						peer.getMC().send(new_msg);

						try {
							Thread.sleep((long) (time * Math.pow(2, attempt)));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						int currentReplication = fs.getChunkReplication(msg.getHeader().getFileId(),
								msg.getHeader().getChunkNo());
						int desiredReplication = fs.getChunkDesiredReplicationDegree(msg.getHeader().getFileId(),
								msg.getHeader().getChunkNo());
						if (currentReplication >= desiredReplication) {
							done = true;
						}
					}
					if (done) {
						System.out.println("Successfully stored <" + msg.getHeader().getFileId() + ", "
								+ msg.getHeader().getChunkNo() + ">");
					}

					try {
						fs.saveFileSystem(peer.getId());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
