package aufgabe_4_2;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class Controller extends UnicastRemoteObject implements InstanceToController {
	
	protected Controller() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	private static Map<String, Consumer<List<String>>> actions = new HashMap<>();
	private static String localIP;
	
	private static Controller controller;
	
	static {
		try {
			controller = new Controller();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static{
		actions.put("register", Controller::register );
		actions.put("add", Controller::add);
		actions.put("remove", Controller::remove);
	}
	
	public static void main(String... args) {
		
		
		if (args.length < 1)
		{
			System.err.println("Need ip.");
			return;
		}
		
		localIP = args[0];
		
		
		Registry registry;
		try {
			registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			registry.bind("controller", (InstanceToController)controller);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
			return;
		}
		
		try (Scanner scanner = new Scanner(System.in)) {
			while (true)
			{
				String next = scanner.nextLine();
				String[] splitted = next.split(" ");
				System.out.println(actions.get(splitted[0]));
				actions.getOrDefault(splitted[0], in -> System.out.println("falsche Eingabe")).accept(Arrays.asList(splitted));
			}
		}
	}
	
	static void register(List<String> list)
	{
		System.err.println("register: " + list);
		String ip = list.get(0);
		
	}
	
	static void add(List<String> list)
	{
		System.err.println("add: " + list);
	}
	
	static void remove(List<String> list)
	{
		System.err.println("remove: " + list);
	}

	@Override
	public void addInstance(InstanceHandle instance) throws RemoteException {
		// TODO Auto-generated method stub
		System.err.println("addInstance: " + instance);
	}

	@Override
	public void removeInstance(InstanceHandle instance) throws RemoteException {
		// TODO Auto-generated method stub
		System.err.println("removeInstance: " + instance);
	}

	@Override
	public InstanceHandle nextInstance() throws RemoteException {
		// TODO Auto-generated method stub
		System.err.println("nextInstance");
		return null;
	}

	
}