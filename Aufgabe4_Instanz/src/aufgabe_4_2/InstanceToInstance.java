package aufgabe_4_2;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InstanceToInstance extends Remote, Serializable {	
	
	// self = zyklusdurchbruch.
	// Holt sich instanz, die Platz frei hat.
	ControllerToInstance getAvailable(InstanceHandle self) throws RemoteException;
	
	// Lockt den sitzplatz (beide gabeln).
	int getSeat(InstanceHandle instance) throws RemoteException;
	// -> getSeat(getAvailable(self));
	
	// Aufgerufen auf der jeweiligen Instanz.
	void freeSeat(int index) throws RemoteException;
	
	// Lockt die erste Gabel der Instanz die aufgerufen wurde.
	void lockNext() throws RemoteException;
	void freeNext() throws RemoteException;
}
