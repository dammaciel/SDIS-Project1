package Service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerInterface extends Remote {
	 int getId() throws RemoteException;
	 void putFile(String filename, int repDegree) throws RemoteException;
	 void deleteFile(String filename) throws RemoteException;
	 void fileRestore(String path) throws RemoteException;
}
