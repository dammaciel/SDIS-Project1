package Handler;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class PackageHandler extends Thread {
	private MulticastSocket mc_socket;
    private InetAddress mc_address;
    private int mc_port;
    private String name;
    
    public PackageHandler(MulticastSocket mc_socket, InetAddress mc_address, int mc_port, String name){
        this.mc_socket = mc_socket;
        this.mc_address = mc_address;
        this.mc_port = mc_port;
        this.name = name;
    }
    
    public void run() {
        CommandHandler commandHandler = CommandHandler.getInstance();

        System.out.println("Listening on multicast group " + name);
        while(true) {
            try {
                byte[] packetData = new byte[1024 * 64];
                DatagramPacket reception_packet = new DatagramPacket(packetData, packetData.length);
                mc_socket.receive(reception_packet);
                byte[] dataRead = Arrays.copyOf(reception_packet.getData(),reception_packet.getLength());
                commandHandler.addCommand(dataRead);
            } catch (Exception e) {
                System.err.println("Error: Message dropped");
            }
        }
    }

}
