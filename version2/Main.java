
public class Main {

	public static void main(String...args) throws InterruptedException
	{
		int philosophers = 6;
		int seats = 6;
		int hungry = 6;
		
		if (args.length == 3)
		{
			philosophers = Integer.valueOf(args[0]);
			hungry = Integer.valueOf(args[1]);
			seats = Integer.valueOf(args[2]);
		}
		
		Philosopher[] phils = new Philosopher[philosophers];
		HungryPhilosopher[] hungryPhilosophers = new HungryPhilosopher[hungry];
		
		Table table = new Table(seats);
		
		for (int i = 0; i < philosophers; i++)
		{
			phils[i] = new Philosopher(table);
			phils[i].start();
		}
		
		for (int i = 0; i < hungry; i++)
		{
			hungryPhilosophers[i] = new HungryPhilosopher(table);
			hungryPhilosophers[i].start();
		}
		
		Thread.sleep(60000);
		
		for (int i = 0; i < philosophers; i++)
		{
			phils[i].interrupt();
			hungryPhilosophers[i].interrupt();
		}
	}
	
}
