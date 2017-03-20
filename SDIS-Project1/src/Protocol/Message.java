package Protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Message {
	public Header header;
	public byte[] body = null;
	
	public Message(Header header, byte[] body){
        this.header = header;
        this.body = body;
    }
	
	public byte[] getBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(header.getBytes());
            if (body != null) out.write(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
	
	public Message(byte[] bytesReceived) throws IllegalArgumentException{

        int i = 0;
        while(i < bytesReceived.length - 3){
            if(bytesReceived[i] == Header.CR && bytesReceived[i + 1] == Header.LF
                    && bytesReceived[i + 2] == Header.CR && bytesReceived[i + 3] == Header.LF)
                break;
            i++;
        }
        String messageHeader = new String (Arrays.copyOfRange(bytesReceived,0,i));
        header = new Header(messageHeader);
        if(i+4 >= bytesReceived.length) {
            body = null;
        }else {
            body = Arrays.copyOfRange(bytesReceived, i + 4, bytesReceived.length);
        }
    }
}
