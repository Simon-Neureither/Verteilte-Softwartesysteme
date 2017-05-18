
public class HungryPhilosopher extends Philosopher {

	public HungryPhilosopher(Table t) {
		super(t);
	}

	@Override
	public void run()
	{
		int sleepTime = 10;
		int meditationTime = 5;
		int eatTime = 1;
		
		int meditationTimeMinusEat = Math.max(0, meditationTime - eatTime);
		
		while (!m_done)
		{
			meditate(meditationTimeMinusEat);
			if (m_done) break;
			if (eat(eatTime)) eatCount++;
			if (m_done) break;
			if (eat(eatTime)) eatCount++;
			if (m_done) break;
			meditate(meditationTime);
			if (m_done) break;
			if (eat(eatTime)) eatCount++;
			if (m_done) break;
			meditate(meditationTimeMinusEat);
			if (m_done) break;
			if (eat(eatTime)) eatCount++;
			if (m_done) break;
			if (eat(eatTime)) eatCount++;
			if (m_done) break;
			sleep(sleepTime);
		}
		
		System.err.println(time() + " " + Thread.currentThread().getId() + " VIELFRASSE "+ String.format(" overall waiting time: %d, i ate %d times", m_overallWaitingTime, eatCount));

	}
}
