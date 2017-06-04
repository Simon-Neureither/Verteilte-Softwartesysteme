package controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import shared_interfaces.InstanceHandle;
import shared_interfaces.InstanceToController;

public class InstanceToControllerImpl extends UnicastRemoteObject implements InstanceToController {
	
	private final List<InstanceHandle> instances;
	
	private final List<Float> instanceScores;
	
	/**
	 * Counter for instances to determine which instance is which.
	 */
	private int uniqueID = 0;
	
	public InstanceToControllerImpl(final List<InstanceHandle> instances, final List<Float> instanceScores) throws RemoteException{
		super();
		this.instances = instances;
		this.instanceScores = instanceScores;
	}
	
	@Override
	public synchronized int addInstance(InstanceHandle instance) throws RemoteException {
		instances.add(instance);
		instanceScores.add(0F);
		return uniqueID++;
	}

	@Override
	public synchronized void removeInstance(InstanceHandle instance) throws RemoteException {
		int index = instances.indexOf(instance);
		instances.remove(index);
		instanceScores.remove(index);
		instances.forEach(inst -> {
			try {
				inst.updateNeighbours(instances);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void log(String tag, long timestamp, String message) throws RemoteException {
		System.err.println("[" + tag + "@" + timestamp + "] " + message);
	}

}
