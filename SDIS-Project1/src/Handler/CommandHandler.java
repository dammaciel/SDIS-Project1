package Handler;

import java.util.concurrent.LinkedBlockingQueue;

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
    
    public void addCommand(byte[] command) {
        try {
            commands.put(command);
        } catch (InterruptedException e) {
            return;
        }
    }

}
