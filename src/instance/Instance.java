package instance;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import shared_interfaces.ControllerToInstance;
import shared_interfaces.InstanceHandle;
import shared_interfaces.InstanceToController;

public class Instance extends UnicastRemoteObject implements InstanceHandle {
	
	// Set if the instance was in one time the first instance.
	// Used to determine the lock order for the first seat.
	// So that no deadlock can occur.
	private boolean instanceWasLonely = false;
	
	
	/**
	 * Class to represent a seat, it contains two forks.
	 * If the second fork (fork2) is null it means that the next instance has the fork
	 * and that nextInstance.lockNext() should be called to lock it.
	 */
	public class Seat {
		
		private boolean lockOrder;
		private Semaphore fork1;
		private Semaphore fork2;
		
		final private Semaphore seatLock = new Semaphore(1, true);
		private boolean seatValid = true;
		
		public void setLockOrder(boolean order)
		{
			lockOrder = order;
		}
		
		public boolean getLockOrder()
		{
			return lockOrder;
		}
		
		public void setFork1(Semaphore fork)
		{
			fork1 = fork;
		}
		
		public void setFork2(Semaphore fork)
		{
			fork2 = fork;
		}
		
		private boolean lock1()
		{
			fork1.acquireUninterruptibly();
			return true;
		}
		private void release1()
		{
			fork1.release();
		}
		private void release2()
		{
			if (fork2 == null)
			{
				try
				{
					nextInstance.freeNext();
				}
				catch (RemoteException e)
				{
					// TODO critical error
					System.err.println("CRITICAL ERROR");
					e.printStackTrace();
				}
			}
			else
			{				
				fork2.release();
			}
		}
	
		private boolean lock2()
		{
			if (fork2 == null)
			{
				try {
					nextInstance.lockNext();
				} catch (RemoteException e) {
					return false;
				}
			}
			else
			{
				fork2.acquireUninterruptibly();
			}
			return true;
		}
		
		
		/**
		 * Locks the forks.
		 * @return true if successful, false if not (false only if RemoteException occured).
		 */
		public boolean lockForks()
		{
			boolean locked1 = false;
			boolean locked2 = false;
			
			if (!lockOrder)
			{
				locked1 = lock1();
				if (!locked1)
					return false;
				locked2 = lock2();
				if (!locked2)
					release1();
				return true;
			}
			else
			{
				locked2 = lock2();
				if (!locked2)
					return false;
				locked1 = lock1();
				if (!locked1)
					release2();
				return true;
			}
		}
		
		
		/**
		 * Releases forks.
		 */
		public void releaseForks()
		{
			release1();
			release2();
		}
		
		/**
		 * Locks the seat.
		 */
		public void lockSeat()
		{
			seatLock.acquireUninterruptibly();
		}
		
		public void releaseSeat()
		{
			seatLock.release();
		}
		
		/**
		 * Checks if the seat is still valid.
		 * 
		 * May only be called when the seatLock (lockSeat) is held.
		 * 
		 * @return true if valid, false otherwise.
		 */
		public boolean isSeatValid()
		{
			return seatValid;
		}
		
		/**
		 * Sets the seat valid flag.
		 * 
		 * May only be called when the seatLock (lockSeat) is held.
		 * 
		 * @param flag state.s
		 */
		public void setSeatValid(boolean flag)
		{
			seatValid = flag;
		}
		
		@Override
		public String toString()
		{
			return "Seat: "  + fork1 + "  : " + fork2 + " : valid: " + isSeatValid();
		}
	
		public Semaphore getFork1() {
			return fork1;
		}
		
		public Semaphore getFork2()
		{
			return fork2;
		}
	}

	
	private Semaphore leftFork = new Semaphore(0);
	
	private String controllerAddress;
	private Registry registry;
	
	private InstanceToController controller;
	
	private List<PhilosopherData> philosophers = new ArrayList<PhilosopherData>();
	private List<Seat> seats = new ArrayList<Seat>();
	
	private boolean hasStarted = false;
	
	private InstanceHandle nextInstance;
	
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
	
	/**
	 * Simplifies controller log message calls.
	 * @param tag
	 * @param message
	 */
	private void controllerLog(String tag, String message)
	{
		tag += this.hashCode();
		System.out.println("[" + tag + "] " + message);
		try
		{
			controller.log(tag, System.currentTimeMillis(), message);
		
		}
		catch (RemoteException e)
		{
			// TODO
			e.printStackTrace();
		}
	}

	@Override
	public synchronized float addPhilosoph(boolean hungry) {
		// TODO Auto-generated method stub
		
		controllerLog("addPhilosoph", "adding philosoph " + hungry);
		
		synchronized (philosophers)
		{
			// TODO replace 0 with minimum eaten + some offset.
			philosophers.add(new PhilosopherData(false, 0));
		}
		
		controllerLog("addPhilosoph", "added philosoph " + hungry);
		
		return calcRatio(philosophers.size(), seats.size());
	}
	


	@Override
	public synchronized float removePhilosoph(boolean hungry) {
		controllerLog("removePhilosoph", "removing philosoph " + hungry);
		
		boolean removed = false;
		synchronized (philosophers)
		{
			
			for (int i = 0; i < philosophers.size(); i++)
			{
				if (philosophers.get(i).isHungry() == hungry)
				{
					PhilosopherData data = philosophers.remove(i);
					controllerLog("removePhilosoph", "stopping philosopher to remove...");
					data.stop();
					controllerLog("removePhilosoph", "stopped philosopher to remove");
					removed = true;
					break;
				}
			}
		}
		
		controllerLog("removePhilosoph", "removed philosoph " + hungry + " removed: " + (removed ? "YES" : "NO"));

		return calcRatio(philosophers.size(), seats.size());
	}

	@Override
	public synchronized float addSeat() {
		
		Seat seat = new Seat();
		seat.setFork2(null); // -> null means next fork (from next instance).
		
		if (seats.size() == 0)
		{
			// No seat...
			seat.setLockOrder(instanceWasLonely); // Only one instance may start with this flag set.
			seat.setFork1(leftFork);
			seat.setFork2(null); // --> null means next fork (next instance)
		}
		else
		{
			
			// Add new seat as last seat.
			Seat prevSeat = seats.get(seats.size() - 1);
			// Switch the order the seat locks the forks -> better performance, no deadlocks.
			seat.setLockOrder(!prevSeat.lockOrder);
			// Lock seat.
			synchronized (prevSeat)
			{
				// Make them share a fork.
				prevSeat.setFork2(new Semaphore(1, true));
				seat.setFork1(prevSeat.getFork2());
			}
		}
		
		// Only instance?
		if (this.equals(nextInstance))
		{
			// Only one place, so it gets two forks.
			if (seats.size() == 0)
			{
				seat.setFork2(new Semaphore(1, true));
			}
			else
			{
				// #seats > 0 -> it gets first fork
				seat.setFork2(leftFork);
			}
		}
		
		// Finally release the seat to the philosophers.
		seats.add(seat);
		
		return calcRatio(philosophers.size(), seats.size());
	}

	@Override
	public synchronized float removeSeat() {
		if (seats.size() == 0)
		{
			controllerLog("removeSeat", "no seat available.");
		}
		else if (seats.size() > 1)
		{
			int idx = seats.size() - 1;
			Seat seat = seats.get(idx);
			seat.lockSeat();
			seat.setSeatValid(false);
			seat.releaseSeat();
			seats.remove(idx);
			controllerLog("removeSeat", "removed Seat");
		}
		return calcRatio(philosophers.size(), seats.size());
	}

	@Override
	public synchronized void updateNext() throws RemoteException  {
		
		// TODO
		// Since it is not required to handle changing instances,
		// there is no handling of philosophers eating at the last seat
		// while adding a new instance.
		nextInstance = controller.nextInstance(this);
		
		if (this.equals(nextInstance))
		{
			System.out.println("this instance is lonely");
			
			instanceWasLonely = true;
		}
		else
		{
			System.out.println("this instance has a 'real' next");
			
			// Update the last seat.
			if (seats.size() > 0)
			{
				seats.get(seats.size() - 1).setFork2(null);
			}
		}
		
		controllerLog("updateNext", "updated next to: " + nextInstance);
	}

	@Override
	public synchronized void start() {
		try {
			controller.log(this.toString(), System.currentTimeMillis(), "starting instance");
		} catch (RemoteException e1) {
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
	public void lockNext() throws RemoteException {
		// Lockt die erste "gabel"
		// Falls kein platz -> nextIntance.lockNext();		
		
		if (this.seats.size() == 0)
		{
			nextInstance.lockNext();
		}
		else
		{
			// TODO
		}
		
	}
	
	@Override
	public boolean equals(Object o)
	{
		try {
			return RemoteObject.toStub(this).equals(RemoteObject.toStub((Remote) o));
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void freeNext() throws RemoteException {
		// TODO Auto-generated method stub
		
		if (this.seats.size() == 0)
		{
			nextInstance.freeNext();
		}
		else
		{
			seats.get(0);
		}
		
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

	@Override
	public synchronized String debug_getSeatsAsString() throws RemoteException {
		return seats.toString();
	}

	@Override
	public String getPhilosophersAsString() throws RemoteException {
		
		String str = "";
		for (int i = 0; i < philosophers.size(); i++)
		{
			if (i != 0)
				str = str + System.lineSeparator();
			str = str + philosophers.get(i);
		}
		return str;
	}
}
