package aufgabe_4_2;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ControllerToInstance extends Remote, Serializable {
	
	
	// Controller to Instance
	// Rueckgabewert = Verhaeltnis Philosoph / Seat
	float addPhilosoph(boolean hungry) throws RemoteException;
	float removePhilosoph(boolean hungry) throws RemoteException;
	
	float addSeat() throws RemoteException;
	float removeSeat() throws RemoteException;
	
	void updateNext() throws RemoteException;
	
	void start() throws RemoteException;
	void stop() throws RemoteException;
	void exit() throws RemoteException;
		
}
