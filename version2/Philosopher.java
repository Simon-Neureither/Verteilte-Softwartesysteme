import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Philosopher extends Thread {

	boolean m_done = false;
	Table m_table;
	
	public Philosopher(Table t)
	{
		m_table = t;
	}
	
	String time()
	{
		Date date = new Date(System.currentTimeMillis());
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
		String dateFormatted = formatter.format(date);
		return dateFormatted;
	}
	
	void meditate(int ms)
	{
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			m_done = true;
		}
	}
	
	
	int m_overallWaitingTime = 0;
	boolean eat(int ms)
	{
		long waitTime = System.currentTimeMillis();

		
		Table.Seat s = m_table.getSeat(this);
		
		if (s == null)
		{
			return false;
		}
		
		boolean aquired = false;
		try
		{
			
			s.getSemaphore().acquireUninterruptibly();
			
			aquired = true;
			
			synchronized (s.getFork1())
			{
				synchronized (s.getFork2())
				{
					s.eat(this);
					waitTime = System.currentTimeMillis() - waitTime;
					m_overallWaitingTime += waitTime;
					try {
						Thread.sleep(ms);
					} catch (InterruptedException e) {
						m_done = true;
					}
				}
			}
		} finally
		{
			if (aquired)
			{
				s.getSemaphore().release();
			}
		}
		
		return true;
	}
	
	void sleep(int ms)
	{
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			m_done = true;
		}
	}
	protected int eatCount = 0;
	
	@Override
	public void run()
	{
		int sleepTime = 10;
		int meditationTime = 5;
		int eatTime = 1;
		
		while (!m_done)
		{
			meditate(meditationTime);
			if (m_done) break;
			if (eat(eatTime)) eatCount++;
			if (m_done) break;
			meditate(meditationTime);
			if (m_done) break;
			if (eat(eatTime)) eatCount++;
			if (m_done) break;
			meditate(meditationTime);
			if (m_done) break;
			if (eat(eatTime)) eatCount++;
			if (m_done) break;
			sleep(sleepTime);
		}
		System.err.println(time() + " " + Thread.currentThread().getId() + String.format(" overall waiting time: %d, i ate %d times", m_overallWaitingTime, eatCount));
	}

	public int getEaten() {
		return eatCount;
	}
	
}
