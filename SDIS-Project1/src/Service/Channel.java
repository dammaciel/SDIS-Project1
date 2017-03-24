package Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import Message.Header;
import Message.Message;
import Handler.Handler;

public class Channel implements Runnable {
	
	private MulticastSocket socket;
    private InetAddress address;
    private int port;
    private String name;
    
    public ArrayList<Handler> handlers = new ArrayList<>();
    
    public Channel(InetAddress address, int port, String name) {
        this.address = address;
        this.port = port;
        this.name = name;
        open();
    }
	
	public MulticastSocket getSocket() {
		return socket;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public String getName() {
		return name;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[64512];
		boolean done = false;
		while (!done) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
            	System.out.println(this.name + " Estou pronto a receber");
                socket.receive(packet);
                System.out.print("Vamos para o handle?");
                handle(packet);
            } catch (IOException e) {
            	System.out.println(e);
                e.printStackTrace();
            }

        }
		
	}
	
	private void open() {
        try {
            socket = new MulticastSocket(port);
            socket.setBroadcast(true);
            socket.setTimeToLive(1);
            socket.joinGroup(address);
            System.out.println(this.name + " - Join group Complete - "+ port);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void send(Message message) {
        send(message.getBytes());
    }
	
	public void send(byte[] packet) {
        DatagramPacket p = new DatagramPacket(packet, packet.length, address, port);
        try {
            socket.send(p);
            System.out.print(this.name + " está a enviar");
        } catch (IOException e) {
            e.printStackTrace();
        }
}
	
	public void addHandler(Handler h) {
        h.channel_type = name;
        handlers.add(h);
        System.out.println(handlers);
    }
	
	private void handle(DatagramPacket packet) {
		System.out.println("estou no handle");
        try {
            Message m = new Message(packet);
            for (int i = 0; i < handlers.size(); ++i) {
                handlers.get(i).message = m;
                new Thread(handlers.get(i)).start();
            }
        }
        catch (IllegalArgumentException e) {
        	e.printStackTrace();
        }
    }
	


}
