
public class Main {

	public static void main(String...args) throws InterruptedException
	{
		int philosophers = 6;
		int seats = 6;
		
		Philosopher[] phils = new Philosopher[philosophers];
		Vielfrasse[] vielfraﬂe = new Vielfrasse[philosophers];
		
		Table table = new Table(seats);
		
		for (int i = 0; i < philosophers; i++)
		{
			phils[i] = new Philosopher(table);
			phils[i].start();
			
			vielfraﬂe[i] = new Vielfrasse(table);
			vielfraﬂe[i].start();
		}
		
		Thread.sleep(5000);
		
		for (int i = 0; i < philosophers; i++)
		{
			phils[i].interrupt();
			vielfraﬂe[i].interrupt();
		}
		
		for (int i = 0; i < table.m_seats.length; i++)
		{
			System.out.println(table.m_seats[i]);
		}
	}
	
}
