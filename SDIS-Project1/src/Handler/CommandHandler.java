package Handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import FileSystem.Chunk;
import FileSystem.File;
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

				break;
			case "GETCHUNK":

				break;
			case "CHUNK":

				break;
			case "DELETE":

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

	private void handlePutChunk(Message m) {
		File file_chunk;
		String fileId = m.getHeader().getFileId();
		if (FileSystem.instance().hasFile(fileId)) {
			file_chunk = FileSystem.instance().getFile(fileId);
		} else {
			String dir_path = peer.getId() + "/" + fileId + "/";
			file_chunk = new File(fileId, dir_path);
		}
		Random rand = new Random();
		try {
            Thread.sleep(rand.nextInt(401));
        } catch (InterruptedException e) { e.printStackTrace(); }

		int chunk_nr = m.getHeader().getChunkNo();
		int replD = m.getHeader().getReplicationDeg();
		try {
			if (!file_chunk.hasChunk(chunk_nr)) {
				String chunk_path = peer.getId() + "/" + fileId + "/" + chunk_nr + ".txt";
				Chunk chunk = new Chunk(chunk_nr, replD, m.getBody(), chunk_path);
				chunk.saveData();
			}
			Header response_header = new Header("STORED", "1.0", peer.getId(), fileId, chunk_nr, replD);
			Message response = new Message(response_header, null);
			MulticastSocket socket = peer.getMC().getSocket();
			DatagramPacket packet = new DatagramPacket(response.getBytes(), response.getBytes().length,
					peer.getMC().getAddress(), peer.getMC().getPort());

			socket.send(packet);

		} catch (IOException e) {
			System.err.println("Error: Couldn't create chunk file");
		}
	}
}
