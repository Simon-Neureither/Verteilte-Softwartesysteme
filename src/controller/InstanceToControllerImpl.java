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
		System.err.println("addInstance: " + instance);
		instances.add(instance);
		instanceScores.add((float) 1.0); // TODO what is the best score for an empty instance?
	}

	@Override
	public synchronized void removeInstance(InstanceHandle instance) throws RemoteException {
		System.err.println("removeInstance: " + instance);
		int index = instances.indexOf(instance);
		
		// TODO updatenext/change fork etc..
		instances.remove(index);
		instanceScores.remove(index);
	}

	@Override
	public InstanceHandle nextInstance(InstanceHandle instance) throws RemoteException {
		// TODO Auto-generated method stub
		System.err.println("nextInstance");
		return null;
	}

	@Override
	public void log(String tag, long timestamp, String message) throws RemoteException {
		// TODO Auto-generated method stub
		System.err.println("[" + tag + "@" + timestamp + "] " + message);
	}

}
