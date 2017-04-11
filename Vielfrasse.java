
public class Vielfrasse extends Philosopher {

	public Vielfrasse(Table t) {
		super(t);
	}

	@Override
	public void run()
	{
		int sleepTime = 10;
		int meditationTime = 5;
		int eatTime = 1;
		
		int eatCount = 0;
		
		while (true)
		{
			
			meditate(meditationTime - eatTime);
			if (this.isInterrupted()) break;
			eat(eatTime);
			eatCount++;
			if (this.isInterrupted()) break;
			eat(eatTime);
			eatCount++;
			if (this.isInterrupted()) break;
			meditate(meditationTime);
			if (this.isInterrupted()) break;
			eat(eatTime);
			eatCount++;
			if (this.isInterrupted()) break;
			meditate(meditationTime - eatTime);
			if (this.isInterrupted()) break;
			eat(eatTime);
			eatCount++;
			if (this.isInterrupted()) break;
			eat(eatTime);
			eatCount++;
			if (this.isInterrupted()) break;
			sleep(sleepTime);
			if (this.isInterrupted()) break;
		}
		
		System.err.println(time() + " " + Thread.currentThread().getId() + " VIELFRASSE "+ String.format(" overall waiting time: %d, i ate %d times", m_overallWaitingTime, eatCount));

	}
}
