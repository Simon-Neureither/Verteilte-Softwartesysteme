package shared_interfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InstanceToInstance extends Remote, Serializable {	
	
	// self = zyklusdurchbruch.
	// Holt sich instanz, die Platz frei hat.
	InstanceToInstance getAvailable(InstanceHandle self) throws RemoteException;
	
	// Lockt den sitzplatz (und beide gabeln).
	int getSeat(InstanceHandle instance) throws RemoteException;
	// -> getSeat(getAvailable(self));
	
	// Aufgerufen auf der jeweiligen Instanz.
	void freeSeat(int index) throws RemoteException;
	
	// Lockt die erste Gabel der Instanz die aufgerufen wurde.
	void lockNext(InstanceHandle instance) throws RemoteException;
	void freeNext(InstanceHandle instance) throws RemoteException;
}
