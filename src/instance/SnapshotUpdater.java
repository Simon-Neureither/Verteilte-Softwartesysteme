package instance;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;

import shared_interfaces.InstanceToInstance;

public class SnapshotUpdater extends Thread {

	private static final long INTERVALL_RANGE[] = {500, 5000};

	private static final long EXPIRE_TIME = 3000;

	private final Instance caller;

	private boolean stopped;

	private boolean firstIteration = true;

	SnapshotUpdater(final Instance caller){
		this.caller = caller;
	}

	@Override
	public void run() {
				
		// instantiate snapshots
		caller.getNeighbours().parallelStream()
		.filter(inst -> !caller.areInstancesEqual(inst))
		.forEach(inst -> {
			try {
				caller.getSnapshots().put(caller.getUniqueIDOfHandle(inst), inst.checkAvailable(
						caller.getUniqueIDLocal(), 
						caller.getFreeSeats(), 
						caller.getEatCount()));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});

		// notify after instantiation
		synchronized(this){
			firstIteration = false;
			notifyAll();
		}

		// update loop
		while(!stopped){
			try {
				sleep(INTERVALL_RANGE[0] + (long)Math.random()*(INTERVALL_RANGE[1]-INTERVALL_RANGE[0]));
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			
			final long currentTime = System.currentTimeMillis();
			final int freeSeats = caller.getFreeSeats();
			final int eatCount = caller.getEatCount();

			caller.getSnapshots().entrySet().parallelStream()
			.filter(entry -> currentTime - entry.getValue().lastUpdated > EXPIRE_TIME)
			.filter(entry -> !caller.areInstancesEqual(entry.getKey()))
			.forEach(entry -> {
				try {
					SnapshotEntry snapshotEntry = caller.getInstanceByID(entry.getKey()).checkAvailable(caller.getUniqueIDLocal(), freeSeats, eatCount);
					entry.setValue(snapshotEntry);
					caller.updateEaten(entry.getKey(), snapshotEntry.eatCount);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
		}
		
		// reset iterations
		synchronized(this){firstIteration = true;}
	}

	public synchronized void waitForFirstIteration(){
		while(firstIteration){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void stopThread(){
		stopped = true;
	}

}
