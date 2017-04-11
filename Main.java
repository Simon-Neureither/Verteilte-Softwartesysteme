
public class Main {

	public static void main(String...args) throws InterruptedException
	{
		int philosophers = 6;
		int seats = 6;
		
		Philosopher[] phils = new Philosopher[philosophers];
		Vielfrasse[] vielfra�e = new Vielfrasse[philosophers];
		
		Table table = new Table(seats);
		
		for (int i = 0; i < philosophers; i++)
		{
			phils[i] = new Philosopher(table);
			phils[i].start();
			
			vielfra�e[i] = new Vielfrasse(table);
			vielfra�e[i].start();
		}
		
		Thread.sleep(5000);
		
		for (int i = 0; i < philosophers; i++)
		{
			phils[i].interrupt();
			vielfra�e[i].interrupt();
		}
		
		for (int i = 0; i < table.m_seats.length; i++)
		{
			System.out.println(table.m_seats[i]);
		}
	}
	
}
