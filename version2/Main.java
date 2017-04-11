
public class Main {

	public static void main(String...args) throws InterruptedException
	{
		int philosophers = 6;
		int seats = 6;
		
		Philosopher[] phils = new Philosopher[philosophers];
		HungryPhilosopher[] hungryPhilosophers = new HungryPhilosopher[philosophers];
		
		Table table = new Table(seats);
		
		for (int i = 0; i < philosophers; i++)
		{
			phils[i] = new Philosopher(table);
			phils[i].start();
			
			hungryPhilosophers[i] = new HungryPhilosopher(table);
			hungryPhilosophers[i].start();
		}
		
		Thread.sleep(60000);
		
		for (int i = 0; i < philosophers; i++)
		{
			phils[i].interrupt();
			hungryPhilosophers[i].interrupt();
		}
		
		for (int i = 0; i < table.m_seats.length; i++)
		{
			System.out.println(table.m_seats[i]);
		}
	}
	
}
