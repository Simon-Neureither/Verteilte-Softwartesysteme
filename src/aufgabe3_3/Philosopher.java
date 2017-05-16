package aufgabe3_3;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Philosopher extends Thread{


	private Table m_table;

	private boolean stopped;

	public Philosopher(Table t){
		m_table = t;
	}

	String time(){
		Date date = new Date(System.currentTimeMillis());
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
		String dateFormatted = formatter.format(date);
		return dateFormatted;
	}

	void meditate(int ms){
		//	System.out.println(time() + " "  + Thread.currentThread().getId() + " meditate.");
		try{
			Thread.sleep(ms);
		}catch(InterruptedException e){
			stopped = true
		}
		//	System.out.println(time() + " " + Thread.currentThread().getId() + " meditate finished.");
	}


	int m_overallWaitingTime = 0;

	void eat(int ms){
		long waitTime = System.currentTimeMillis();
		Table.Seat s = m_table.getSeat();
		boolean aquired = false;
		try{
			s.getSemaphore().acquireUninterruptibly();
			aquired = true;
			synchronized(s.getFork1()){
				synchronized(s.getFork2()){
					waitTime = System.currentTimeMillis() - waitTime;
					//	System.out.println(time() + " " + Thread.currentThread().getId() + String.format(" eating i waited: %d ms.", waitTime));
					m_overallWaitingTime += waitTime;
					try{
						Thread.sleep(ms);
					}catch(InterruptedException e){
						stopped = true;
					}
					//	System.out.println(time() + " " + Thread.currentThread().getId() + String.format(" stopped eating."));
				}
			}
		}finally{
			if(aquired){
				s.getSemaphore().release();
			}
		}
	}

	void sleep(int ms){
		//System.out.println(time() + " " + Thread.currentThread().getId() + " sleeping.");
		try{
			Thread.sleep(ms);
		}catch(InterruptedException e){
			this.interrupt();
		}
		//System.out.println(time() + " " + Thread.currentThread().getId() + " wake up.");
	}

	@Override
	public void run(){
		int sleepTime = 10;
		int meditationTime = 5;
		int eatTime = 1;

		int eatCount = 0;

		while(true){
			meditate(meditationTime);
			if(isInterrupted()) break;
			eat(eatTime);
			eatCount++;
			if(isInterrupted()) break;
			meditate(meditationTime);
			if(isInterrupted()) break;
			eat(eatTime);
			eatCount++;
			if(isInterrupted()) break;
			meditate(meditationTime);
			if(isInterrupted()) break;
			eat(eatTime);
			eatCount++;
			if(isInterrupted()) break;
			sleep(sleepTime);
			if(isInterrupted()) break;
		}

		System.err.println(time() + " " + Thread.currentThread().getId() + String.format(" overall waiting time: %d, i ate %d times", m_overallWaitingTime, eatCount));
	}

}
