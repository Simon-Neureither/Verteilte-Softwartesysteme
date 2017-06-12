package instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import instance.Instance.HandleSeatPair;

public class PhilosopherData {

	private boolean m_isHungry;
	final private List<State> dailyRoutine;
	
	private int debug_eatDenied = 0;
	
	private Instance instance;

	public PhilosopherData(boolean isHungry, int eatenOffset, Instance instance) {
		this.m_isHungry = isHungry;
		this.eatenOffset = eatenOffset;
		dailyRoutine = Collections.unmodifiableList(this.m_isHungry ? hungry : normal);
		this.instance = instance;
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
	
	public String toString()
	{
		return "Philosopher: hungry: " + m_isHungry + " : eaten: " + eaten + " : eatenOffset: " + eatenOffset + " : waited: " + waited + " eatDenied: " + debug_eatDenied;
	}

	public void eat() {
		if (getEaten() > instance.getEatCount() + 10)
		{
			debug_eatDenied++;
			return;
		}
		else
		{
			long waitStart = System.currentTimeMillis();
			HandleSeatPair pair = instance.getSeatForLocals();
			long waitEnd = System.currentTimeMillis();
			waited += waitEnd - waitStart;
			if (pair.seatIndex == Instance.NO_INSTANCE_HAS_A_SEAT)
			{
				return;
			}
			eaten++;
			instance.updateEaten(this, eaten);
			instance.releaseHandleSatPair(pair);
		}
	}
}
