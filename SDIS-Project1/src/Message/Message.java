package Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Message.Header;

public class Message {
	public static final int CHUNK_SIZE = 64512;

	public Header header;
	public byte[] body = null;
	public int body_offset = -1;

	public Message(DatagramPacket packet) {
		String message = new String(packet.getData(), packet.getOffset(), packet.getLength(),
				StandardCharsets.US_ASCII);

		String[] tokens = message.split("(\\r\\n){2}");
		System.out.println(tokens[0]);
	}

	public Message(byte[] bytesReceived) throws Exception {
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
        }
        else {
            body = Arrays.copyOfRange(bytesReceived, i + 4, bytesReceived.length);
        }
	}

	public Message(Header header, byte[] body) {
		this.header = header;
		this.body = body;
	}

	public Header getHeader() {
		return header;
	}

	public byte[] getBody() {
		return body;
	}

	public static String buildHash(String fileId) {
		StringBuffer hexString;
		MessageDigest hashAlgorithm = null;
		try {
			hashAlgorithm = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] hash = new byte[0];
		try {
			hash = hashAlgorithm.digest(fileId.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		hexString = new StringBuffer();
		for (int j = 0; j < hash.length; j++) {
			String hex = Integer.toHexString(0xff & hash[j]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}

		return hexString.toString();
	}

	public byte[] getBytes(){
        byte[] headerBytes = header.getBytes();
        byte[] CRLF = new byte[]{Header.CR, Header.LF, Header.CR, Header.LF};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(headerBytes);
            outputStream.write(CRLF);
            if(body != null){
                outputStream.write(body);
            }
        } catch (IOException e) {
            System.err.println("Error: Could not get message bytes");
        }
        return outputStream.toByteArray();
    }
}
