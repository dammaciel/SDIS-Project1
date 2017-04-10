package Handler;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import Service.Channel;

/**
*	Main class to handle the packages - Datagram and Multicast
*/
public class PackageHandler extends Thread{
	 private Channel channel;
	 
	 public PackageHandler(Channel channel){
	        this.channel=channel;
	    }
	 
	 public void run() {
	        CommandHandler commandHandler = CommandHandler.getInstance();

	        System.out.println("Listening on multicast group " + channel.getName());
	        while(true) {
	            try {
	                byte[] packetData = new byte[1024 * 64];
	                DatagramPacket receptionPacket = new DatagramPacket(packetData, packetData.length);
	                channel.getSocket().receive(receptionPacket);
	                byte[] dataRead = Arrays.copyOf(receptionPacket.getData(),receptionPacket.getLength());
	                commandHandler.addCommand(dataRead);
	            } catch (Exception e) {
	                System.err.println("Error: Message dropped");
	            }
	        }
	    }
}
