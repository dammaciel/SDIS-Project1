package Handler;

import java.util.concurrent.LinkedBlockingQueue;

import Handler.CommandHandler;
import Message.Message;
import Service.Peer;

public class CommandHandler extends Thread{
	private static Peer peer;
	private LinkedBlockingQueue<byte[]> commands;
    private static CommandHandler commandHandler = null;
	
    private CommandHandler(Peer peer){
        this.peer = peer;
        commands = new LinkedBlockingQueue<>();
    }
    
    public static CommandHandler getInstance(){
        return commandHandler;
    }
    
    public static CommandHandler getInstance(Peer peer){
        if(commandHandler == null){
            commandHandler = new CommandHandler(peer);
        }
        return commandHandler;
    }
    
	public void run(){
		System.out.println("Command Handler");
		boolean done = false;
		while(!done){
			System.out.println("darling");
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
	
	 private String handleCommand(byte[] commandPacket){
	        try{
	        	System.out.println("x para ti");
	            Message msg = new Message(commandPacket);
	            String requestName = msg.getHeader().getFileId() + "_" + msg.getHeader().getChunkNo();
	            System.out.println("MESSAGE: " + msg.getHeader().toString());
	            switch (msg.getHeader().getMessageType()){
	                case "PUTCHUNK":
	                	
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
	        } catch (IllegalArgumentException e){
	            System.err.println(e.getMessage());
	        }
	        return "ERROR";
	    }
}
