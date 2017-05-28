package controller;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import shared_interfaces.InstanceHandle;
import shared_interfaces.InstanceToController;

public class Controller {

	private static Map<String, Consumer<List<String>>> actions = new HashMap<>();
	private static String localIP;

	private static InstanceToControllerImpl instanceToController;

	// Holds all instances.
	private  static List<InstanceHandle> instances = new ArrayList<InstanceHandle>();
	// Holds the "score" of an instance.
	private static List<Float> instanceScores = new ArrayList<Float>();

	static {
		actions.put("addP", args -> Controller.addPhilosoph(false));
		actions.put("addH", args -> Controller.addPhilosoph(true));
		actions.put("addS", Controller::addSeat);
		
		actions.put("rmP", args -> Controller.removePhilosoph(false));
		actions.put("rmH", args -> Controller.removePhilosoph(true));
		actions.put("rmS", Controller::removeSeat);

		actions.put("scores", in -> {
			for (int i = 0; i < instances.size(); i++)
			{
				System.out.printf("%d: %f", i, instanceScores.get(i));
			}
		});
		
		actions.put("ip", args -> System.out.println(Controller.localIP));
	}

	public static void main(String... args) {
		if (args.length < 1) {
			System.err.println("Need ip.");
			return;
		}

		localIP = args[0];

		Registry registry;
		try {
			instanceToController = new InstanceToControllerImpl(instances, instanceScores);
			registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			registry.bind("controller", (InstanceToController) instanceToController);
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

	//	private static boolean parseStringToBoolean(String string) {
	//		boolean bool;
	//		try {
	//			bool = Integer.parseInt(string) != 0;
	//		} catch (NumberFormatException e) {
	//			bool = false;
	//			bool = string.equalsIgnoreCase("y") || string.equalsIgnoreCase("j");
	//		}
	//		return bool;
	//	}

	private static void addPhilosoph(final boolean isHungry) {
		synchronized (instanceToController) {
			if (instanceScores.size() == 0) {
				System.err.println("add failed no instance available.");
				return;
			}

			int index = 0;
			for (int i = 1; i < instances.size(); i++)
			{
				// Add a philosopher -> LOWER is better since it "highers" the score.
				if (instanceScores.get(i) < instanceScores.get(index))
				{
					index = i;
				}
			}
			System.out.println("Adding philosopher to instance #" + index);
			try {
				instanceScores.set(index, instances.get(index).addPhilosoph(isHungry));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// TODO
		}
	}

	private static void addSeat(final List<String> args){
		synchronized (instanceToController) {
			if (instanceScores.size() == 0) {
				System.err.println("add failed no instance available.");
				return;
			}

			int index = 0;
			for (int i = 1; i < instances.size(); i++) {
				// Add a seat -> HIGHER is better since it "lowers" the score.
				if (instanceScores.get(i) > instanceScores.get(index)) {
					index = i;
				}
			}

			System.out.println("Adding seat to instance #" + index);
			try {
				instanceScores.set(index, instances.get(index).addSeat());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private static void removePhilosoph(final boolean isHungry){
		synchronized (instanceToController) {
			if (instanceScores.size() == 0) {
				System.err.println("add failed no instance available.");
				return;
			}

			int index = 0;
			for (int i = 1; i < instances.size(); i++)
			{
				if (instanceScores.get(i) < instanceScores.get(index))
				{
					index = i;
				}
			}
			System.out.println("removing philosopher from instance #" + index);
			try {
				float score = instances.get(index).removePhilosoph(isHungry);
				boolean removed = false;
				if (score == instanceScores.get(index))
				{
					System.err.println("removed failed on instance #" + index);

					for (int i = 0; i < instances.size(); i++)
					{
						score = instances.get(i).removePhilosoph(isHungry);
						if (score != instanceScores.get(i))
						{
							System.out.println("removed philosopher from instance #" + i);
							instanceScores.set(i, score);
							removed = true;
							break;
						}
					}
				}
				else
				{
					System.out.println("removed philosopher sucessfully.");
					instanceScores.set(index, score);
				}

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private static void removeSeat(final List<String> args) {
		synchronized (instanceToController) {
			// REMOVE SEAT TODO
		}
	}

}