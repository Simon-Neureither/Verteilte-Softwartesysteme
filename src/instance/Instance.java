package instance;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import shared_interfaces.InstanceHandle;
import shared_interfaces.InstanceToController;
import shared_interfaces.InstanceToInstance;

public class Instance extends UnicastRemoteObject implements InstanceHandle {
	
	/** Returncode for getSeat if the instance has no seats. */
	final public static int NO_SEAT_AVAILABLE = -1;
	/** Returncode for getSeatLocal if no free seat was found. */
	final public static int NO_SEAT_FREE = -2;
	
	/** Returncode for getSeatForLocals if no instance has a seat. */
	final public static int NO_INSTANCE_HAS_A_SEAT = -3;
	
	/** List of all instances (in the right order). */
	private final List<InstanceHandle> neighbours = new ArrayList<>();
	/** Snapshot of each instance. */
	private final Map<Integer, SnapshotEntry> snapshots = new HashMap<>();
	/** Index of this instance in the neighbour list. */
	private int neighbourIndex = -1;
	/** Keeps snapshots up to date. */
	private SnapshotUpdater updater;
	
	/**
	 * Semaphore to lock 'hadSeatWhileLocked'.
	 */
	private Semaphore prevSeatSemaphore = new Semaphore(1, true);
	/**
	 * Flag for "lockNext" and "freeNext".
	 */
	private boolean hadSeatWhileLocked = false;
	private boolean hadMoreThanOneSeatWhileLocked = false;
	
	/**
	 * Semaphore to lock 'hasSeats'
	 */
	private Semaphore hasSeatsSemaphore = new Semaphore(1, true);
	/**
	 * Flag for "lockNext" and free Next.
	 */
	private boolean hasSeats = false;
	
	private boolean hasMoreThanOneSeat = false;
	
	
	/** Eat count. */
	private int eatCount = 0;
	/** Last philosopher or Snapshot that updated it. */
	private Object eatCountLastUpdater = null;
	
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
				if (neighbours.size() == 1)
				{
					// Only one instance, this.
					rightFork.release();
				}
				else
				{
					try {
						neighbours.get((neighbourIndex + 1) % neighbours.size()).freeFirst(Instance.this.uniqueID);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
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
					neighbours.get((neighbourIndex + 1) % neighbours.size()).lockFirst(Instance.this.uniqueID);
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
			if(neighbourIndex == -1){
				System.err.println("List of neighbours not up to date");
				return;
			}else{						
				release1();
				release2();
			}
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
	
	private Semaphore leftFork = new Semaphore(1, true);
	private Semaphore rightFork = new Semaphore(1, true);
		
	private String controllerAddress;
	
	private InstanceToController controller;
	
	private List<PhilosopherData> philosophers = new ArrayList<PhilosopherData>();
	private List<Seat> seats = new ArrayList<Seat>();
	
	private boolean hasStarted = false;
	
	private final int uniqueID;
	
	private boolean startLockOrder;
		
	private Instance(String controllerAddress) throws RemoteException, NotBoundException
	{
		this.controllerAddress = controllerAddress;
		final Registry registry = LocateRegistry.getRegistry(controllerAddress);
		controller = (InstanceToController) registry.lookup("controller");
		synchronized (this)
		{
			uniqueID = controller.addInstance(this);
			startLockOrder = uniqueID % 2 == 0;
		}
		System.err.println("INSTANCE UNIQUEID: " + uniqueID + " startLockOrder: " + startLockOrder);
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
			e.printStackTrace();
		}
	}
	
	
	/** Ignored debug tags. */
	private static List<String> ignoredTags = new ArrayList<String>();
	
	static
	{
		ignoredTags.add("freeFirst");
		ignoredTags.add("lockFirst");
		ignoredTags.add("addPhilosoph");
		ignoredTags.add("updateNeighbours");
	}
	
	/**
	 * Simplifies controller log message calls.
	 * @param tag
	 * @param message
	 */
	private void controllerLog(String tag, String message)
	{
		
		if (ignoredTags.contains(tag))
			return;
		
		tag += this.hashCode();
		System.out.println("[" + uniqueID + "][" + tag + "] " + message);
		try
		{
			controller.log(tag, System.currentTimeMillis(), message);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
	}
	
	public Map<Integer, SnapshotEntry> getSnapshots(){
		return snapshots;
	}
	
	public List<InstanceHandle> getNeighbours(){
		return neighbours;
	}
	
	public int getFreeSeats(){
		int count  = 0;
		for (int i = 0; i < seats.size(); i++)
		{
			seats.get(i).seatLock.availablePermits();
			count++;
		}
		return count;
	}
	
	public int getEatCount(){
		return eatCount;
	}

	@Override
	public synchronized float addPhilosoph(boolean hungry) {		
		controllerLog("addPhilosoph", "adding philosoph " + hungry);
		
		synchronized (philosophers)
		{
			philosophers.add(new PhilosopherData(hungry, eatCount, this));
			
			if (hasStarted)
				philosophers.get(philosophers.size() - 1).start();
		}
		
		controllerLog("addPhilosoph", "added philosoph " + hungry);
		
		return calcDensity(philosophers.size(), seats.size());
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

		return calcDensity(philosophers.size(), seats.size());
	}

	@Override
	public synchronized float addSeat() {
		
		Seat seat = new Seat();
		seat.setFork2(null); // -> null means next fork (from next instance).
		
		if (seats.size() == 0)
		{
			// No seat...
			seat.setLockOrder(uniqueID == 0); // Only one instance may start with this flag set.
			seat.setFork1(leftFork);
		}
		else
		{
			// Add new seat as last seat.
			Seat prevSeat = seats.get(seats.size() - 1);
			// Switch the order the seat locks the forks -> better performance, no deadlocks.
			seat.setLockOrder(!prevSeat.lockOrder);
			// Lock seat.
			
			// Remap the "right" fork of the previous Seat
			// To a new shared fork.
			prevSeat.lockSeat();
			prevSeat.setFork2(new Semaphore(1, true));
			seat.setFork1(prevSeat.getFork2());
			prevSeat.releaseSeat();
		}
		
		// Finally release the seat to the philosophers.
		hasSeatsSemaphore.acquireUninterruptibly();
		hasSeats = true;
		hasMoreThanOneSeat = seats.size() > 0;
		seats.add(seat);
		hasSeatsSemaphore.release();
		
		return calcDensity(philosophers.size(), seats.size());
	}

	@Override
	public synchronized float removeSeat() {
		if (seats.size() == 0)
		{
			controllerLog("removeSeat", "no seat available.");
		}
		else if (seats.size() > 0)
		{
			int idx = seats.size() - 1;
			Seat seat = seats.get(idx);
			
			
			// Make seat no longer visible for the next philosophers.
			seat.lockSeat();
			seat.setSeatValid(false);
			seat.releaseSeat();
			seats.remove(idx);
			
			if (idx > 0)
			{
				// There is a previous seat in this instance.
				Seat prevSeat = seats.get(idx -1);
				prevSeat.lockSeat();
				
				// Null means use fork of next instance.
				prevSeat.setFork2(null);
				
				prevSeat.releaseSeat();
			}
			
			controllerLog("removeSeat", "removed seat");
			
			if (idx == 1)
			{
				hasMoreThanOneSeat = seats.size() > 0;
			}
			
			if (idx == 0)
			{
				controllerLog("removeSeat", "removed Seat has removed last seat");
				hasSeatsSemaphore.acquireUninterruptibly();
				hasSeats = false;
				hasSeatsSemaphore.release();
			}
		}
		return calcDensity(philosophers.size(), seats.size());
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
			updater = new SnapshotUpdater(this);
			updater.start();
			updater.waitForFirstIteration();
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
			e1.printStackTrace();
		}
		if (hasStarted)
		{
			updater.stopThread();
			for (int i = 0; i < philosophers.size(); i++)
			{
				philosophers.get(i).stop();
			}
			hasStarted = false;
		}
		try {
			controller.log(this.toString(), System.currentTimeMillis(), "has stopped");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public SnapshotEntry checkAvailable(final int neighbour, final int freeSeats, final int eatCount) throws RemoteException {
		InstanceToInstance instance = getInstanceByID(neighbour);
		
		synchronized(instance){
			
			SnapshotEntry entry = snapshots.get(neighbour);
			if (entry == null)
			{
				entry = new SnapshotEntry();
				snapshots.put(neighbour, entry);
			}
			entry.freeSeats = freeSeats;
			entry.eatCount = eatCount;
			entry.lastUpdated = System.currentTimeMillis();
		}
		
		final SnapshotEntry result = new SnapshotEntry();
		result.freeSeats = getFreeSeats();
		result.eatCount = getEatCount();
		
		return result;
	}
	
	/**
	 * Gets a local seat.
	 * 
	 * Seat is NOT locked.
	 * 
	 * @return
	 */
	private int getSeatLocal(boolean force)
	{
		// seat list is changed while iterated (last seat removed)
		// so a array.. can occur.
		while (true)
		{
			if (seats.size() == 0)
				return NO_SEAT_AVAILABLE;
			
			try
			{
				List<Integer> indices = new ArrayList<Integer>();
				for (int i = 0; i < seats.size(); i++)
				{
					if (seats.get(i).seatLock.availablePermits() == 1)
					{
						indices.add(i);
					}
				}
				
				if (indices.size() != 0)
				{
					return indices.get((int)(Math.random() * indices.size()));
				}
				if (force)
					return(int)(Math.random() * seats.size());
				
				return NO_SEAT_FREE;
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				// No error handling required, just redo this operation.
			}
		}
	}
	
	
	public class HandleSeatPair
	{
		public HandleSeatPair(InstanceHandle handle, int seatIndex) { this.handle = handle; this.seatIndex = seatIndex; }
		public InstanceHandle handle;
		public int seatIndex;
	}
	
	private HandleSeatPair getSeatOfRandomInstance()
	{
		// No local seat... so we have to lock a seat from another instance.
		int index;
		
		for (int i = 0; i < neighbours.size(); i++)
		{
			if (neighbourIndex == i)
				continue;
			
			try {
				index = neighbours.get(i).getSeat();
			} catch (RemoteException e) {
				e.printStackTrace();
				index = NO_SEAT_AVAILABLE;
			}
			
			if (index == NO_SEAT_AVAILABLE)
			{
				continue;
			}
			
			return new HandleSeatPair(neighbours.get(i), index);
		}
		
		// Here comes the part what to do if no instance has a seat (controller should have handled this)
		controllerLog("getSeatForLocals", "no instance has a seat");
		return new HandleSeatPair(null, NO_INSTANCE_HAS_A_SEAT);
	}
	
	/**
	 * Gets a seat and locks it.
	 * @return index.
	 */
	public HandleSeatPair getSeatForLocals()
	{
		boolean force = neighbours.size() == 1; // Only force to get a seat on this table if no neighbour exists.
		while (true)
		{
			
			int index = getSeatLocal(force);
			if (index < 0)
			{
				// No seat available/free.
				List<Integer> handlesWithFreeSeats = new ArrayList<Integer>();
				
				for (Entry<Integer, SnapshotEntry> e : snapshots.entrySet())
				{
					if (e.getValue().freeSeats > 0)
						handlesWithFreeSeats.add(e.getKey());
					
				}
				
				if (handlesWithFreeSeats.size() == 0)
				{
					if (this.seats.size() > 0)
					{
						// Don't use network since the network has no free seats.
						force = true;
						continue; // Restart with local seat.
					}
					else
					{
						return getSeatOfRandomInstance();
					}
				}
				
				InstanceHandle instance = neighbours.get(handlesWithFreeSeats.get((int)(Math.random() * handlesWithFreeSeats.size())));
				
				try
				{
				index = instance.getSeat();
				} catch (RemoteException e)
				{
					continue;
				}
				
				if (index == NO_SEAT_AVAILABLE)
				{
					force = true;
					continue;
				}
				
				return new HandleSeatPair(instance, index);
			}
			else
			{
				Seat seat;
				try
				{
					seat = seats.get(index);
					seat.lockSeat();
					if (!seat.isSeatValid())
					{
						seat.releaseSeat();
						continue;
					}
					seat.lockForks();
					return new HandleSeatPair(this, index);
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					continue; // redo.
				}
			}
		}
	}
	
	/**
	 * Gets and locks a seat.
	 * 
	 * @return NO_SEAT_AVAILABLE if no seat is was found in this instance OR a valid index.
	 */
	@Override
	public int getSeat() {
		if (seats.size() == 0)
		{
			return NO_SEAT_AVAILABLE;
		}
		
		while (true)
		{
			int local = getSeatLocal(true);
			
			if (local == NO_SEAT_AVAILABLE)
				return NO_SEAT_AVAILABLE;
			
			Seat seat;
			
			try
			{
				seat = seats.get(local);
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				// Seat was removed in the meantime, redo.
				continue;
			}
			
			seat.lockSeat();
			if (!seat.isSeatValid())
			{
				seat.releaseSeat();
				continue;
			}
			seat.lockForks();
			return local;
		}
	}

	public void releaseHandleSatPair(HandleSeatPair pair)
	{
		try {
			pair.handle.freeSeat(pair.seatIndex);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Frees a seat.
	 */
	@Override
	public void freeSeat(int index) {
		// Since the seat was locked it is guaranteed that the index still exists in the seats list.
		seats.get(index).releaseForks();
		seats.get(index).releaseSeat();
	}

	@Override
	public void lockFirst(int ID) throws RemoteException {
		prevSeatSemaphore.acquireUninterruptibly();
		hasSeatsSemaphore.acquireUninterruptibly();
		
		controllerLog("lockFirst", uniqueID + " initial caller: " + ID);
		
		if (areInstancesEqual(ID))
		{
			controllerLog("lockFirst", "rightFork");
			rightFork.acquireUninterruptibly();
		}
		else if (hasSeats)
		{
			hadSeatWhileLocked = true;
			controllerLog("lockFirst", "leftFork");
			leftFork.acquireUninterruptibly();
		}
		else
		{
			hadSeatWhileLocked = false;
			controllerLog("lockFirst", "nextFork hadNoSeats -> redirecting call to : " + ((neighbourIndex + 1) % neighbours.size()));
			neighbours.get((neighbourIndex + 1) % neighbours.size()).lockFirst(ID);
		}
		
		controllerLog("lockFirst", "end of lock first reached.");
		
		hasSeatsSemaphore.release();
		prevSeatSemaphore.release();
	}
	
	@Override
	public void freeFirst(int ID) throws RemoteException {
		prevSeatSemaphore.acquireUninterruptibly();
				
		if (areInstancesEqual(ID))
		{
			controllerLog("freeFirst", "rightFork");
			rightFork.release();
		}
		else if (hadSeatWhileLocked)
		{
			controllerLog("freeFirst", "leftFork");
			leftFork.release();
		}
		else
		{
			controllerLog("freeFirst", "nextFork");
			neighbours.get((neighbourIndex + 1) % neighbours.size()).freeFirst(ID);
		}
		
		prevSeatSemaphore.release();
	}
	
	
	/**
	 * Calculates the "score" of the instance.
	 * The lower the number, the better.
	 * @param amountPhilosophers
	 * @param amountSeats
	 * @return
	 */
	private float calcDensity(int amountPhilosophers, int amountSeats)
	{
		if (amountSeats == 0)
		{
			return (float) (amountPhilosophers == 0 ? 1.0 : 1.0 / 0);
		}
		return ((float)amountPhilosophers) / amountSeats;
	}
	
	@Override
	public synchronized int seatCount() throws RemoteException{
		return seats.size();
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

	@Override
	public void updateNeighbours(List<InstanceHandle> neighbours) throws RemoteException {
		this.neighbours.clear();
		this.neighbours.addAll(neighbours);
		
		for (int i = 0; i < neighbours.size(); i++)
		{
			handles.put(neighbours.get(i), neighbours.get(i).getUniqueID());
			if (areInstancesEqual(neighbours.get(i)))
			{
				neighbourIndex = i;
				controllerLog("updateNeighbours", "Neighbour ID of instance " + getUniqueIDLocal()  + " " + uniqueID + " set to: " + i);
			}
			
		}
	}

	public void updateEaten(Object trigger, int eaten) {
		if (eatCount > eaten || eatCountLastUpdater == trigger || eatCountLastUpdater == null)
		{
			eatCount = eaten;
			eatCountLastUpdater = trigger;
		}
	}
	
	private Map<InstanceHandle, Integer> handles = new HashMap<InstanceHandle, Integer>();
	
	
	public boolean areInstancesEqual(int ID)
	{
		return uniqueID == ID;
	}
	
	public boolean areInstancesEqual(InstanceHandle handle)
	{
		return uniqueID == handles.get(handle);
	}
	
	@Override
	public int getUniqueID() throws RemoteException {
		return uniqueID;
	}
	
	public int getUniqueIDLocal()
	{
		return uniqueID;
	}

	public Integer getUniqueIDOfHandle(InstanceHandle inst) {
		return handles.get(inst);
	}

	public InstanceToInstance getInstanceByID(Integer key) {
		
		for (Entry<InstanceHandle, Integer> e : handles.entrySet())
		{
			if (e.getValue().equals(key))
				return e.getKey();
		}
		return null;
	}
	
	@Override
	public String toString2() throws RemoteException
	{
		return "Instance: s: " + seats.size() + " p:" + philosophers.size() + " uID:" + uniqueID + " leftFork: " + leftFork.availablePermits() + " rightFork: " + rightFork.availablePermits() + " eatCount: " + eatCount + " startLockOrder: " + startLockOrder;
	}

	@Override
	public synchronized boolean swapLockOrder(boolean lockOrder) throws RemoteException {
		
		if (seats.size() > 1)
		{
			controllerLog("swapLockOrder", "Seat count must be 0 or 1 but is: " + seats.size());
			return false;
		}
		
		controllerLog("swapLockOrder", "swapped lock order to: " + lockOrder);
		
		startLockOrder = lockOrder;
		if (seats.size() == 1)
		{
			seats.get(0).lockSeat();
			seats.get(0).setLockOrder(startLockOrder);
			seats.get(0).releaseSeat();
		}
		return true;
	}
	
}
