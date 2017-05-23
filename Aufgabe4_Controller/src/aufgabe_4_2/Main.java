package aufgabe_4_2;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class Main {

	private static Map<String, Consumer<List<String>>> actions = new HashMap<>();
	private static String localIP;

	static{
		actions.put("register", Main::register );
		actions.put("add", instance -> {});
		actions.put("remove", instance -> {});
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
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		
		try (Scanner scanner = new Scanner(System.in)) {
			while (true)
			{
				String next = scanner.nextLine();
				String[] splitted = next.split(" ");
				actions.getOrDefault(splitted[0], in -> System.out.println("falsche Eingabe")).accept(Arrays.asList(splitted));
			}
		}
	}
	
	static void register(List<String> list)
	{
		String ip = list.get(0);
		
	}
	
	static void add(List<String> list)
	{
		
	}
	
	static void remove(List<String> list)
	{
		
	}
}
