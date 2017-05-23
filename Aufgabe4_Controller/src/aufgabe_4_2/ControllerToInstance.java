package aufgabe_4_2;

import java.io.Serializable;
import java.rmi.Remote;

public interface ControllerToInstance extends Remote, Serializable {
	
	
	// Controller to Instance
	// Rueckgabewert = Verhaeltnis Philosoph / Seat
	float addPhilosoph(boolean hungry);
	float removePhilosoph(boolean hungry);
	
	float addSeat();
	float removeSeat();
	
	void updateNext();
	
	void start();
	void stop();
	void exit();
	
	void log(String tag, long timestamp, String message);
	
}
