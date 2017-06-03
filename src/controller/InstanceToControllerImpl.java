package controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import shared_interfaces.InstanceHandle;
import shared_interfaces.InstanceToController;

public class InstanceToControllerImpl extends UnicastRemoteObject implements InstanceToController {
	
	private final List<InstanceHandle> instances;
	
	private final List<Float> instanceScores;
	
	public InstanceToControllerImpl(final List<InstanceHandle> instances, final List<Float> instanceScores) throws RemoteException{
		super();
		this.instances = instances;
		this.instanceScores = instanceScores;
	}
	
	@Override
	public synchronized void addInstance(InstanceHandle instance) throws RemoteException {
		instances.forEach(inst -> {
			try {
				inst.updateNeighbours(instances);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
		instances.add(instance);
		instanceScores.add(0F);
	}

	@Override
	public synchronized void removeInstance(InstanceHandle instance) throws RemoteException {
		int index = instances.indexOf(instance);
		instances.remove(index);
		instances.forEach(inst -> {
			try {
				inst.updateNeighbours(instances);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
		instanceScores.remove(index);
	}

	@Override
	public void log(String tag, long timestamp, String message) throws RemoteException {
		System.err.println("[" + tag + "@" + timestamp + "] " + message);
	}

}
