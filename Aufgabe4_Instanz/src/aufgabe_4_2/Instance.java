package aufgabe_4_2;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Instance extends UnicastRemoteObject implements InstanceHandle {

	private String controllerAddress;
	private Registry registry;
	
	private InstanceToController controller;
	
	private List<PhilosopherData> philosophers = new ArrayList<PhilosopherData>();
	private List<PhilosopherData> seats = new ArrayList<PhilosopherData>();
	
	private boolean hasStarted = false;
	
	private Instance(String controllerAddress) throws RemoteException, NotBoundException
	{
		this.controllerAddress = controllerAddress;
		registry = LocateRegistry.getRegistry(controllerAddress);
		controller = (InstanceToController) registry.lookup("controller");
		controller.addInstance(this);
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
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized float addPhilosoph(boolean hungry) {
		// TODO Auto-generated method stub
		
		try
		{
			controller.log("instance", System.currentTimeMillis(), "adding philosoph " + hungry);
		
		}
		catch (RemoteException e)
		{
			// TODO
			e.printStackTrace();
		}
		
		synchronized (philosophers)
		{
			// TODO replace 0 with minimum eaten + some offset.
			philosophers.add(new PhilosopherData(false, 0));
		}
		
		try
		{
			controller.log("instance", System.currentTimeMillis(), "added philosoph " + hungry);
		
		}
		catch (RemoteException e)
		{
			// TODO
			e.printStackTrace();
		}
		
		return calcRatio(philosophers.size(), seats.size());
	}

	@Override
	public synchronized float removePhilosoph(boolean hungry) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public synchronized float addSeat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public synchronized float removeSeat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public synchronized void updateNext() throws RemoteException  {
		controller.nextInstance(this);
	}

	@Override
	public synchronized void start() {
		try {
			controller.log(this.toString(), System.currentTimeMillis(), "starting instance");
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (!hasStarted)
		{
			for (int i = 0; i < philosophers.size(); i++)
			{
				philosophers.get(i).start();
			}
			hasStarted = true;
		}
		try {
			controller.log(this.toString(), System.currentTimeMillis(), "has started");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void stop() {
		try {
			controller.log(this.toString(), System.currentTimeMillis(), "stopping instance");
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (hasStarted)
		{
			for (int i = 0; i < philosophers.size(); i++)
			{
				philosophers.get(i).stop();
			}
			hasStarted = false;
		}
		try {
			controller.log(this.toString(), System.currentTimeMillis(), "has stopped");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void exit() {
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
	
	/**
	 * Calculates the "score" of the instance.
	 * The lower the number, the better.
	 * @param amountPhilosophers
	 * @param amountSeats
	 * @return
	 */
	private float calcRatio(int amountPhilosophers, int amountSeats)
	{
		if (amountSeats == 0)
		{
			return (float) (amountPhilosophers == 0 ? 1.0 : 1.0 / 0);
		}
		return ((float)amountPhilosophers) / amountSeats;
	}
}
