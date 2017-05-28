package aufgabe4_1;

import java.rmi.RemoteException;

public class HelloWorldImpl implements HelloWorld {

	@Override
	public void sayHello() throws RemoteException {
		System.out.println("Hello World!");
	}

}
