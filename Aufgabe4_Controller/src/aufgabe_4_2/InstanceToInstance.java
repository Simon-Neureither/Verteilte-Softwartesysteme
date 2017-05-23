package aufgabe_4_2;

public interface InstanceToInstance {	
	
	// self = zyklusdurchbruch.
	// Holt sich instanz, die Platz frei hat.
	ControllerToInstance getAvailable(InstanceHandle self);
	
	// Lockt den sitzplatz (beide gabeln).
	int getSeat(InstanceHandle instance);
	// -> getSeat(getAvailable(self));
	
	// Aufgerufen auf der jeweiligen Instanz.
	void freeSeat(int index);
	
	// Lockt die erste Gabel der Instanz die aufgerufen wurde.
	void lockNext();
	void freeNext();
}
