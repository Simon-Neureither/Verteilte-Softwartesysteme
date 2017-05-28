package controller;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import shared_interfaces.InstanceHandle;
import shared_interfaces.InstanceToController;

public class Controller extends UnicastRemoteObject implements InstanceToController {

	protected Controller() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	private static Map<String, Consumer<List<String>>> actions = new HashMap<>();
	private static String localIP;

	private static Controller controller;

	// Holds all instances.
	private  static List<InstanceHandle> instances = new ArrayList<InstanceHandle>();
	// Holds the "score" of an instance.
	private static List<Float> instanceScore = new ArrayList<Float>();

	static {
		try {
			controller = new Controller();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static {
		actions.put("register", Controller::register);
		actions.put("add", Controller::add);
		actions.put("remove", Controller::remove);
		
		actions.put("addP", in -> { add(Arrays.asList("add", "1", "0")); });
		actions.put("addH", in -> { add(Arrays.asList("add", "1", "1")); });
		actions.put("addS", in -> { add(Arrays.asList("add", "0")); });
		
		actions.put("score", in -> {
			
			for (int i = 0; i < instances.size(); i++)
			{
				System.out.println(i + ": " + instanceScore.get(i));
			}
		});
	}

	public static void main(String... args) {

		if (args.length < 1) {
			System.err.println("Need ip.");
			return;
		}

		localIP = args[0];

		Registry registry;
		try {
			registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			registry.bind("controller", (InstanceToController) controller);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
			return;
		}

		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				String next = scanner.nextLine();
				String[] splitted = next.split(" ");
				actions.getOrDefault(splitted[0], in -> System.out.println("falsche Eingabe"))
						.accept(Arrays.asList(splitted));
			}
		}
	}

	static void register(List<String> list) {
		System.err.println("register: " + list);
		String ip = list.get(0);

	}

	private static boolean parseStringToBoolean(String string) {
		boolean bool;
		try {
			bool = Integer.parseInt(string) != 0;
		} catch (NumberFormatException e) {
			bool = false;

			bool = string.equalsIgnoreCase("y") || string.equalsIgnoreCase("j");
		}
		return bool;
	}

	static void add(List<String> list) {
		synchronized (controller) {
			System.err.println("add: " + list);
			
			if (instanceScore.size() == 0)
			{
				System.err.println("add failed no instance available.");
				return;
			}
			
			if (list.size() < 2) {
				System.err.println("add excpects min 1 arg");
				return;
			}

			boolean isPhilosopher = parseStringToBoolean(list.get(1));

			if (isPhilosopher) {
				if (list.size() < 3) {
					System.err.println("add [philosopher] expects min 2 args");
					return;
				}

				int index = 0;
				boolean isHungry = parseStringToBoolean(list.get(2));
				for (int i = 1; i < instances.size(); i++)
				{
					// Add a philosopher -> LOWER is better since it "highers" the score.
					if (instanceScore.get(i) < instanceScore.get(index))
					{
						index = i;
					}
				}
				System.out.println("Adding philosopher to instance #" + index);
				try {
					instanceScore.set(index, instances.get(index).addPhilosoph(isHungry));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// TODO
			} else {
				
				int index = 0;
				for (int i = 1; i < instances.size(); i++)
				{
					// Add a seat -> HIGHER is better since it "lowers" the score.
					if (instanceScore.get(i) > instanceScore.get(index))
					{
						index = i;
					}
				}
				System.out.println("Adding seat to instance #" + index);
				try {
					instanceScore.set(index, instances.get(index).addSeat());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	static void remove(List<String> list) {
		synchronized (controller) {
			System.err.println("remove: " + list);
			
			if (list.size() < 2)
			{
				System.err.println("remove expects min 1 arg");
				return;
			}
			
			if (instanceScore.size() == 0)
			{
				System.err.println("add failed no instance available.");
				return;
			}
			
			boolean isPhilosopher = parseStringToBoolean(list.get(1));
			
			if (isPhilosopher) {
				if (list.size() < 3) {
					System.err.println("remove [philosopher] expects min 2 args");
					return;
				}

				int index = 0;
				boolean isHungry = parseStringToBoolean(list.get(2));
				for (int i = 1; i < instances.size(); i++)
				{
					// Add a philosopher -> LOWER is better since it "highers" the score.
					if (instanceScore.get(i) < instanceScore.get(index))
					{
						index = i;
					}
				}
				System.out.println("removing philosopher from instance #" + index);
				try {
					float score = instances.get(index).removePhilosoph(isHungry);
					boolean removed = false;
					if (score == instanceScore.get(index))
					{
						System.err.println("removed failed on instance #" + index);
						
						for (int i = 0; i < instances.size(); i++)
						{
							score = instances.get(i).removePhilosoph(isHungry);
							if (score != instanceScore.get(i))
							{
								System.out.println("removed philosopher from instance #" + i);
								instanceScore.set(i, score);
								removed = true;
								break;
							}
						}
					}
					else
					{
						System.out.println("removed philosopher sucessfully.");
						instanceScore.set(index, score);
					}
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				// REMOVE SEAT TODO
			}
		}
	}

	@Override
	public synchronized void addInstance(InstanceHandle instance) throws RemoteException {
		System.err.println("addInstance: " + instance);
		instances.add(instance);
		instanceScore.add((float) 1.0); // TODO what is the best score for an empty instance?
	}

	@Override
	public synchronized void removeInstance(InstanceHandle instance) throws RemoteException {
		System.err.println("removeInstance: " + instance);
		int index = instances.indexOf(instance);
		
		// TODO updatenext/change fork etc..
		instances.remove(index);
		instanceScore.remove(index);
	}

	@Override
	public InstanceHandle nextInstance(InstanceHandle instance) throws RemoteException {
		// TODO Auto-generated method stub
		System.err.println("nextInstance");
		return null;
	}

	@Override
	public void log(String tag, long timestamp, String message) throws RemoteException {
		// TODO Auto-generated method stub
		System.err.println("[" + tag + "@" + timestamp + "] " + message);
	}

}