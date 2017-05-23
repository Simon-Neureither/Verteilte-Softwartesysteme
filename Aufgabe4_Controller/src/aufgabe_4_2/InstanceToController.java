package aufgabe_4_2;

import java.rmi.Remote;

public interface InstanceToController extends Remote {
	
	// Instance to Controller
	void addInstance(InstanceHandle instance);
	void removeInstance(InstanceHandle instance);
	InstanceHandle nextInstance(); // Naechste oder eigene.
	
}
