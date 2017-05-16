package aufgabe3_3;

public class Main{

	public static void main(String... args) throws InterruptedException{
		int num_philosophers = 6;
		int seats = 6;

		Philosopher[] philosophers = new Philosopher[num_philosophers];
		HungryPhilosopher[] hungryPhilosophers = new HungryPhilosopher[num_philosophers];

		Table table = new Table(seats);

		for(int i = 0; i < num_philosophers; i++){
			philosophers[i] = new Philosopher(table);
			philosophers[i].start();

			hungryPhilosophers[i] = new HungryPhilosopher(table);
			hungryPhilosophers[i].start();
		}

		Thread.sleep(5000);

		for(int i = 0; i < num_philosophers; i++){
			philosophers[i].interrupt();
			hungryPhilosophers[i].interrupt();
		}

		for(int i = 0; i < table.m_seats.length; i++){
			System.out.println(table.m_seats[i]);
		}
	}

}
