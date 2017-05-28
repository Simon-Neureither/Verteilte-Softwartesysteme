package aufgabe4_1;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HelloWorld extends Serializable, Remote {
	
	void sayHello() throws RemoteException;

}
