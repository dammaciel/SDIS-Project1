package Handler;

import Message.Message;

public abstract class Handler implements Runnable {
	
	public String channel_type;
    public int peer;
    public Message message;
    
    public Handler() {}
    public Handler(int peer) { this.peer = peer; }
    public abstract void handle(Message m);

	@Override
	public void run() {
		handle(this.message); 
		
	}

}
