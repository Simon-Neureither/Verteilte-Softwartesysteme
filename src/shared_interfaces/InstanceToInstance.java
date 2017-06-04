package shared_interfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import instance.SnapshotEntry;

public interface InstanceToInstance extends Remote, Serializable {	
	
	// self = zyklusdurchbruch.
	// Holt sich instanz, die Platz frei hat.
	SnapshotEntry checkAvailable(int instanceID, final int freeSeats, final int eatCount) throws RemoteException;
	
	// Lockt den sitzplatz (und beide gabeln).
	int getSeat(int instanceID) throws RemoteException;
	// -> getSeat(getAvailable(self));
	
	// Aufgerufen auf der jeweiligen Instanz.
	void freeSeat(int index) throws RemoteException;
	
	// Lockt die erste Gabel der Instanz die aufgerufen wurde.
	void lockFirst(int instanceID) throws RemoteException;
	void freeFirst(int instanceID) throws RemoteException;
	
	int getUniqueID() throws RemoteException;
}
