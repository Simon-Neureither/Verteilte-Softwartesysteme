package instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhilosopherData {

	private boolean m_isHungry;
	final private List<State> dailyRoutine;

	public PhilosopherData(boolean isHungry, int eatenOffset) {
		this.m_isHungry = isHungry;
		this.eatenOffset = eatenOffset;
		dailyRoutine = Collections.unmodifiableList(this.m_isHungry ? hungry : normal);
	}

	enum State {
		MEDITATE, EAT, SLEEP
	}

	final private static List<State> normal;
	final private static List<State> hungry;
	static {
		// Instructions to execute by a Philosopher Thread per "day".
		normal = new ArrayList<State>();
		normal.add(State.MEDITATE);
		normal.add(State.EAT);
		normal.add(State.MEDITATE);
		normal.add(State.EAT);
		normal.add(State.MEDITATE);
		normal.add(State.EAT);
		normal.add(State.SLEEP);

		hungry = new ArrayList<State>();
		hungry.add(State.MEDITATE);
		hungry.add(State.EAT);
		hungry.add(State.MEDITATE);
		hungry.add(State.EAT);
		hungry.add(State.EAT);
		hungry.add(State.MEDITATE);
		hungry.add(State.EAT);
		hungry.add(State.EAT);
		hungry.add(State.SLEEP);
	}

	private int eaten = 0;
	private int eatenOffset = 0; // offset to add to eat (used when the philosopher joins later).
	public int waited = 0;

	public int sleepTime = 10;
	public int eatTime = 1;
	// TODO hungry less meditate time?
	public int meditateTime = 5;

	public List<State> getDailyRoutine() {
		return dailyRoutine;
	}
	
	public boolean isHungry()
	{
		return m_isHungry;
	}
	
	// Eat handling.
	public int getEaten()
	{
		return eaten + eatenOffset;
	}
	public int getRealEaten()
	{
		return eaten;
	}

	// Thread handling.
	private PhilosopherThread thread;

	public boolean hasThread() {
		return thread != null;
	}

	public PhilosopherThread getThread() {
		return thread;
	}

	public void stop() {
		if (thread != null) {
			thread.stopPhilosopher();
			thread = null;
		}
	}

	public void start() {
		if (thread == null) {
			thread = new PhilosopherThread(this);
			thread.start();
		}
	}
}
