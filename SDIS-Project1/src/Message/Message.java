package Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import Message.Header;

public class Message {
	public static final int CHUNK_SIZE = 64512; 
	
	public Header header;
	public byte[] body = null;
	
	public Message(DatagramPacket packet) {
		System.out.println("entreiw2");
        String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.US_ASCII);

        String[] tokens = message.split("(\\r\\n){2}");
        System.out.println(tokens[0]);
        System.out.println(tokens[1]);
    }
	
	public Message(Header header, byte[] body){
        this.header = header;
        this.body = body;
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

	public Header getHeader() {
		return header;
	}

	public byte[] getBody() {
		return body;
	}
	
	public static String buildHash(String fileId){
        StringBuffer hexString;
        MessageDigest hashAlgorithm=null;
        try {
            hashAlgorithm = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash= new byte[0];
        try {
            hash = hashAlgorithm.digest(fileId.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        hexString=new StringBuffer();
        for (int j = 0; j < hash.length; j++) { 
            String hex = Integer.toHexString(0xff & hash[j]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
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
}
