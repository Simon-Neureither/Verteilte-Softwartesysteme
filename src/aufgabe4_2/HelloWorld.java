package aufgabe4_2;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Robin Wismeth, robinwismeth@gmail.com
 * @version 5/16/17
 */
public interface HelloWorld extends Serializable, Remote{

	void sayHello() throws RemoteException;

}
