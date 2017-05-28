package shared_interfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InstanceToController extends Remote, Serializable {
	
	// Instance to Controller
	void addInstance(InstanceHandle instance) throws RemoteException;
	void removeInstance(InstanceHandle instance) throws RemoteException;
	InstanceHandle nextInstance(InstanceHandle instance) throws RemoteException; // Naechste oder eigene.
	
	
	void log(String tag, long timestamp, String message) throws RemoteException;

}
