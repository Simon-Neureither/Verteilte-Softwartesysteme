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

import javax.swing.plaf.SliderUI;

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
	
	// Holds the lock order of the instances.
	private static List<Boolean> instanceLockOrder = new ArrayList<Boolean>();
	// Holds the seat coount of the instances.
	private static List<Integer> instanceSeatCount = new ArrayList<Integer>();

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
				System.out.printf("I%d: %f P/S", i, instanceScores.get(i));
			}
		});
		
		actions.put("ip", args -> System.out.println(Controller.localIP));
		
		actions.put("debug_printSeats", in -> Controller.debug_printSeats());
		
		actions.put("start", in -> Controller.start());
		
		actions.put("stop", in -> Controller.stop());
		
		actions.put("printP", in -> Controller.printP());
		
		actions.put("t", in -> Controller.testCase1());
		actions.put("t4", in -> Controller.testCase4Instances());
		
		actions.put("pI", in -> Controller.printInstances());
		
		actions.put("tS",  in -> Controller.test_Swap());
	}
	
	private static void printInstances()
	{
		for (int i = 0; i < instances.size(); i++)
		{
			try {
				System.out.println(i + ": " + instances.get(i).toString2());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void test_Swap()
	{
		try
		{
			
			printInstances();
			
			addSeat(Arrays.asList(new String[]{"addSeat", "0"}));
			// Swap required
			addSeat(Arrays.asList(new String[]{"addSeat", "2"}));
			
			// Should print true, false, false (was true);
			printInstances();
			addSeat(Arrays.asList(new String[]{"addSeat", "1"}));
			
			printInstances();
			
			// Should swap
			removeSeat(Arrays.asList(new String[]{"removeSeat", "0"}));
			
			printInstances();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void testCase1()
	{
		try
		{
			instances.get(0).addPhilosoph(false);
			instances.get(0).addSeat();
		//	instances.get(1).addSeat();

			start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void testCase4Instances()
	{
		try
		{
			instances.get(0).addPhilosoph(true);
			instances.get(0).addPhilosoph(true);
			instances.get(0).addPhilosoph(true);
			
			
			
			for (int i = 0; i < 4; i++)
			{
				instances.get(i).addPhilosoph(false);
				instances.get(i).addPhilosoph(false);
				instances.get(i).addPhilosoph(false);
				
			}
			
			
			instances.get(0).addSeat();
			instances.get(1).addSeat();
			instances.get(2).addSeat();
			instances.get(3).addSeat();
			instances.get(3).addSeat();
			instances.get(3).addSeat();
			
			printInstances();
			
			start();
			
			Thread.sleep(60000);
			
			stop();
			
			printInstances();
			printP();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void printP()
	{
		for (int i = 0; i < instances.size(); i++)
		{
			System.out.println("Instance: " + i);
			try {
				System.out.println(instances.get(i).getPhilosophersAsString());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void start()
	{
		instances.forEach(inst -> {
			try {
				inst.updateNeighbours(instances);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
		
		for (int i = 0; i < instances.size(); i++)
			try {
				instances.get(i).start();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	}
	
	private static void stop()
	{
		for (int i = 0; i < instances.size(); i++)
			try {
				instances.get(i).stop();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	}
	
	public static void main(String... args) {
		if (args.length < 1) {
			System.err.println("Need ip.");
			return;
		}

		localIP = args[0];

		Registry registry;
		try {
			instanceToController = new InstanceToControllerImpl(instances, instanceScores, instanceLockOrder, instanceSeatCount);
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
			System.out.printf("Adding%sphilosopher to instance #%d%n", isHungry ? " hungry ":" ", index);
			try {
				instanceScores.set(index, instances.get(index).addPhilosoph(isHungry));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private static void addSeat(final List<String> args){
		synchronized (instanceToController) {
			if (instanceScores.size() == 0) {
				System.err.println("add failed no instance available.");
				return;
			}

			int index = 0;
			
			if (args.size() >= 2)
			{
				index = Integer.parseInt(args.get(1));
			}
			else
			{
				for (int i = 1; i < instances.size(); i++) {
					// Add a seat -> HIGHER is better since it "lowers" the score.
					if (instanceScores.get(i) > instanceScores.get(index)) {
						index = i;
					}
				}
			}
			System.out.println("Adding seat to instance #" + index);
			try {
				
				InstanceHandle handle = instances.get(index);
				
				boolean swapNecessary = false;
				
				if (instanceSeatCount.get(index) > 0)
				{
					//TODO
					// SeatCount >= 1 -> no swap necessary.
				}
				else
				{
					int seatCount = 0;
					int lastIndexWithSeat = -1;
					for (int i = 0; i < instanceSeatCount.size(); i++)
					{
						seatCount += instanceSeatCount.get(i);
						
						if (instanceSeatCount.get(i) > 0)
						{
							lastIndexWithSeat = i;
						}
						
						if (seatCount > 2)
							break;
					}
					
					if (seatCount == 0)
					{
						//TODO
						// No swap necessary.
					}
					else if (seatCount == 1)
					{
						if (instanceLockOrder.get(lastIndexWithSeat) == instanceLockOrder.get(index))
						{
							// Same lock order -> swap.
							swapNecessary = true;
						}
					}
				}
				
				if (swapNecessary)
				{
					boolean oldLockOrder = instanceLockOrder.get(index);
					instances.get(index).swapLockOrder(!oldLockOrder);
					instanceLockOrder.set(index, !oldLockOrder);
				}
				
				instanceScores.set(index, instances.get(index).addSeat());
				instanceSeatCount.set(index, instanceSeatCount.get(index) + 1);
				
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
				if (instanceScores.get(i) > instanceScores.get(index))
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
			int allAvailableSeats = 0;
			int bestIndex = 0;
			float bestScore = 0;
			
			boolean otherInstanceHasMoreThanOneSeat = false;
			int instancesWithOneSeat = 0;
			
			for(int index = 0; index < instances.size(); index++){
				
				allAvailableSeats += instanceSeatCount.get(index);
								
				if (bestScore > instanceScores.get(index))
				{
					bestScore = instanceScores.get(index);
					bestIndex = index;
				}
			}
			
			if (args.size() > 1)
			{
				bestIndex = Integer.parseInt(args.get(1));
			}
			
			for (int i = 0; i < instanceSeatCount.size(); i++)
			{
				if (i != bestIndex)
				{
					if (instanceSeatCount.get(i) > 1)
					{
						otherInstanceHasMoreThanOneSeat = true;
						break;
					}
					else if (instanceSeatCount.get(i) == 1)
					{
						instancesWithOneSeat++;
					}
				}
			}
			
			if (allAvailableSeats <= 1){
				System.err.println("remove seat failed: no instance or extra seat available.");
				return;
			}
			
			float score;
			
			
			// Only instances with one seat -> we might have to swap (if we have 3 seats).
			if (!otherInstanceHasMoreThanOneSeat && instanceSeatCount.get(bestIndex) <= 2 && instancesWithOneSeat > 0)
			{
				boolean instancesHaveSameLockOrder = true;
				int firstInstance = -1;
				int lastIndex = -1;
				for (int i = 0; i < instanceSeatCount.size(); i++)
				{
					if (i != bestIndex)
					{
						if (instanceSeatCount.get(i) == 1)
						{
							if (firstInstance == -1)
							{
								firstInstance = i;
							}
							else if (instanceLockOrder.get(firstInstance) != instanceLockOrder.get(i))
							{
								instancesHaveSameLockOrder = false;
								break;
							}
							else
							{
								lastIndex = i;
							}
						}
					}
					else
					{
						if (instanceSeatCount.get(bestIndex) == 2)
						{
							// After remove the instance will have 1 seat.
							
							if (firstInstance == -1)
							{
								firstInstance = i;
							}
							else if (instanceLockOrder.get(firstInstance) != instanceLockOrder.get(i))
							{
								instancesHaveSameLockOrder = false;
								break;
							}
							else
							{
								lastIndex = i;
							}
						}
					}
				}
				
				if (firstInstance == bestIndex)
					firstInstance = lastIndex;
				
				if (instancesHaveSameLockOrder && lastIndex != -1)
				{
					boolean lockOrder = instanceLockOrder.get(firstInstance);
					try {
						instances.get(firstInstance).swapLockOrder(!lockOrder);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					instanceLockOrder.set(firstInstance, !lockOrder);
				}
			
			}
			try {
				score = instances.get(bestIndex).removeSeat();
				instanceSeatCount.set(bestIndex, instanceSeatCount.get(bestIndex) - 1);
			} catch (RemoteException e) {
				e.printStackTrace();
				return;
			}
			instanceScores.set(bestIndex, score);
		}
	}
	
	private static void debug_printSeats()
	{
		for (int i = 0; i < instances.size(); i++)
			try {
				System.out.println("Instance: " + i + ": " + instances.get(i).debug_getSeatsAsString());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	}

}