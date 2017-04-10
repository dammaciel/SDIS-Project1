package TestApp;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Service.PeerInterface;

public class TestApp {
	public static void main(String[] args) {
		if (args.length < 3 || args.length > 4) {
			System.err.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
			System.exit(-1);
		}

		String port = args[0];
		String protocol = args[1];
		String fileName = args[2];

		try {
			Registry registry = LocateRegistry.getRegistry();
			PeerInterface peer = (PeerInterface) registry.lookup("Peer");

			switch (protocol) {
			case "BACKUP":
				int repDegree = 0;
				if (args.length == 4) {
					repDegree = Integer.parseInt(args[3]);
				} else {
					System.err.println("Usage: java TestApp <peer_ap> BACKUP <filePath> <repDegree>");
					return;
				}
				peer.putFile(fileName, repDegree);
				System.out.println("Bem vindo ao Backup");
				break;
			case "RESTORE":
				peer.fileRestore(fileName);
				System.out.println("Bem vindo ao Restore");
				break;
			case "DELETE":
				peer.deleteFile(fileName);
				System.out.println("Bem vindo ao Delete");
				break;
			case "RECLAIM":
				int space = Integer.parseInt(fileName);
				peer.reclaimSpace(space);
				System.out.println("Bem vindo ao Reclaim");
				break;
			default:
				System.err.println("Unkown subprotocol!");
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();
			System.err.println("Could not access to RMI register");
		} catch (NotBoundException e1) {
			e1.printStackTrace();
			System.err.println("Impossible to acess peer!");
		}

	}
}
