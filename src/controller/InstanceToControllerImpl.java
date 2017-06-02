package controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import shared_interfaces.InstanceHandle;
import shared_interfaces.InstanceToController;

public class InstanceToControllerImpl extends UnicastRemoteObject implements InstanceToController {
	
	private static final long serialVersionUID = 1L;

	private final List<InstanceHandle> instances;
	
	private final List<Float> instanceScores;
	
	public InstanceToControllerImpl(final List<InstanceHandle> instances, final List<Float> instanceScores) throws RemoteException{
		super();
		this.instances = instances;
		this.instanceScores = instanceScores;
	}
	
	@Override
	public synchronized void addInstance(InstanceHandle instance) throws RemoteException {
		System.err.println("addInstance: " + instance);
		instances.add(instance);
		instanceScores.add(0F);
		
		instances.get(instances.size() - 1).updateNext();
		
		if (instances.size() != 1)
		{
			instances.get(instances.size() - 2).updateNext();
		}
	}

	@Override
	public synchronized void removeInstance(InstanceHandle instance) throws RemoteException {
		System.err.println("removeInstance: " + instance);
		int index = instances.indexOf(instance);
		
		instances.get(index-1).updateNext();
		instances.remove(index);
		instanceScores.remove(index);
	}

	@Override
	public InstanceHandle nextInstance(InstanceHandle instance) throws RemoteException {
		int index = instances.indexOf(instance);
		return instances.get(index % instances.size());
	}

	@Override
	public void log(String tag, long timestamp, String message) throws RemoteException {
		System.err.println("[" + tag + "@" + timestamp + "] " + message);
	}

}
