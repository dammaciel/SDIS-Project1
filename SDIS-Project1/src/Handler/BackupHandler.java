package Handler;

import Message.Message;
import Service.Channel;

public class BackupHandler extends Handler {

	Channel MC;
	
	public BackupHandler(int peer, Channel MC) {
        super(peer);
        this.MC = MC;
    }
	
	@Override
	public void handle(Message m) {
		if (m.getHeader().getMessageType() != "PUTCHUNK") {
            return;
        }
        if (m.getHeader().getSenderId() == peer) return;
        
        System.out.println("foguetes");

	}

}
