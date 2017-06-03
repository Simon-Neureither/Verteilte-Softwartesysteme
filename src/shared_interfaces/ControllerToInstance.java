package shared_interfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ControllerToInstance extends Remote, Serializable {
	
	// Controller to Instance
	// Rueckgabewert = Verhaeltnis Philosoph / Seat
	float addPhilosoph(boolean hungry) throws RemoteException;
	float removePhilosoph(boolean hungry) throws RemoteException;
	
	float addSeat() throws RemoteException;
	float removeSeat() throws RemoteException;
	
	void updateNeighbours(final List<InstanceHandle> neighbours) throws RemoteException;
		
	void start() throws RemoteException;
	void stop() throws RemoteException;

	int seatCount() throws RemoteException;
	String debug_getSeatsAsString() throws RemoteException;
	String getPhilosophersAsString() throws RemoteException;
}
