package aufgabe4_1;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {

	public static void main(String[] args) throws AccessException, RemoteException, AlreadyBoundException, NotBoundException {

		LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

		// server
		final Registry registry = LocateRegistry.getRegistry();
		registry.bind("HelloWorld", new HelloWorldImpl());
		
		// client
		final HelloWorld helloSayer = (HelloWorld) registry.lookup("HelloWorld");
		helloSayer.sayHello();
		
		// stop server
		registry.unbind("HelloWorld");
	}

}
