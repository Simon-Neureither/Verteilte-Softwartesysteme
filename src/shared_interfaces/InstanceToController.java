package shared_interfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface InstanceToController extends Remote, Serializable {
	
	// Instance to Controller
	void addInstance(InstanceHandle instance) throws RemoteException;
	void removeInstance(InstanceHandle instance) throws RemoteException;	
	void log(String tag, long timestamp, String message) throws RemoteException;

}
