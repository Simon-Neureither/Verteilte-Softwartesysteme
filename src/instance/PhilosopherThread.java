package instance;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhilosopherThread extends Thread {

	private PhilosopherData data;
	private AtomicBoolean stop = new AtomicBoolean(false);

	public PhilosopherThread(PhilosopherData data) {
		this.data = data;
	}

	public void stopPhilosopher() {
		stop.set(true);
	}

	@Override
	public void run() {
		List<PhilosopherData.State> dailyRoutine;
		
		while (!stop.get()) {
			try {
				dailyRoutine = data.getDailyRoutine();
				for (int i = 0; i < dailyRoutine.size(); i++) {
					switch (dailyRoutine.get(i)) {
					case EAT:
						data.eat();
						break;
					case MEDITATE:
						sleep(data.meditateTime);
						break;
					case SLEEP:
						sleep(data.sleepTime);
						break;
					default:
						System.err.println("Unhandled daily Routine: " + dailyRoutine.get(i));
						break;
					}
					if (stop.get()) {
						break; // condition will break outer loop too.
					}
				}
			} catch (InterruptedException e) {
				System.err.println("That was unexpected.");
				e.printStackTrace();
			}
		}
	}
}
