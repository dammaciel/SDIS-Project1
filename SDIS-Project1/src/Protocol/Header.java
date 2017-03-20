package Protocol;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Header {
	public static final byte CR = 0xD;
    public static final byte LF = 0xA;
    private static final String[] types = {"PUTCHUNK","STORED", "GETCHUNK", "CHUNK", "DELETE", "REMOVED"};
    private String messageType;
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNo;
    private int replicationDeg;
    
    public Header(String messageType, String version, int senderId, String fileId, int chunkNo, int replicationDegree) throws IllegalArgumentException{
        if(!Arrays.asList(types).contains(messageType)){
            throw new IllegalArgumentException("Invalid <MessageType>");
        }
        Pattern r = Pattern.compile("[0-9]*.[0-9]");
        if(!r.matcher(version).matches()){
            throw new IllegalArgumentException("Invalid <Version>");
        }

        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDegree;

    }
    
    public byte[] getBytes(){
        return toString().getBytes();
    }
    
    public Header(String message) throws  IllegalArgumentException{
        String[] fields = message.split("\\s+");

        if(fields.length < 4 || fields.length > 6){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        messageType = fields[0];
        version = fields[1];
        senderId = Integer.parseInt(fields[2]);
        fileId = fields[3];
        if(fields.length >= 5){
            chunkNo = Integer.parseInt(fields[4]);
        }else{
            chunkNo = 999999;
        }

        if(fields.length == 6){
            replicationDeg = Integer.parseInt(fields[5]);
        }else{
            replicationDeg = 999999;
        }

        if(!Arrays.asList(types).contains(messageType))
            throw new IllegalArgumentException("Invalid <MessageType>");

        Pattern r = Pattern.compile("[0-9]*.[0-9]");
        if(!r.matcher(version).matches())
            throw new IllegalArgumentException("Invalid <Version>");

    }
}
