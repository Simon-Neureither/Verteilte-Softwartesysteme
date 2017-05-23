package aufgabe_4_2;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Instance implements InstanceHandle {

	private String controllerAddress;
	private Registry registry;
	
	private Instance(String controllerAddress) throws RemoteException
	{
		this.controllerAddress = controllerAddress;
		registry = LocateRegistry.getRegistry(controllerAddress);
		registry.list(); // TODO
	}
	
	public static void main(String...args)
	{
		if (args.length < 1)
		{
			System.err.println("controller Adress needed.");
			return;
		}
		
		try {
			new Instance(args[0]);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public float addPhilosoph(boolean hungry) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float removePhilosoph(boolean hungry) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float addSeat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float removeSeat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateNext() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void log(String tag, long timestamp, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ControllerToInstance getAvailable(InstanceHandle self) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSeat(InstanceHandle instance) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void freeSeat(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lockNext() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void freeNext() {
		// TODO Auto-generated method stub
		
	}

}
