package aufgabe_4_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhilosopherData {

	private boolean isHungry;
	final private List<State> dailyRoutine;

	public PhilosopherData(boolean isHungry) {
		this.isHungry = isHungry;
		dailyRoutine = Collections.unmodifiableList(this.isHungry ? hungry : normal);
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

	public int eaten = 0;
	public int waited = 0;

	public int sleepTime = 10;
	public int eatTime = 1;
	// TODO hungry less meditate time?
	public int meditateTime = 5;
	
	public List<State> getDailyRoutine()
	{
		return dailyRoutine;
	}
}
