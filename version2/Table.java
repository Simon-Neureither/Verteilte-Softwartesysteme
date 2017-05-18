import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Table {

	Seat[] m_seats;

	public Table(int seats) {

		m_seats = new Seat[seats];

		Object startFork = new Object();
		Object currentFork = new Object();
		for (int i = 0; i < seats; i++) {
			if (i == 0) {
				m_seats[i] = new Seat(startFork, currentFork);
			} else if (i + 1 == seats) {
				m_seats[i] = new Seat(startFork, currentFork);
			} else if (i % 2 == 0) {
				m_seats[i] = new Seat(currentFork, (currentFork = new Object()));
			} else {
				Object f = new Object();
				m_seats[i] = new Seat(f, currentFork);
				currentFork = f;
			}
		}
	}

	private int getSeatMod(int index) {
		while (index < 0) {
			index = m_seats.length - index;
		}
		return index % m_seats.length;
	}
	
	public int m_globalMinimum = 0;
	public Philosopher m_globalMinimumPhilosph = null;

	public Seat getSeat(Philosopher philosopher) {


		List<Seat> bestSeats = new ArrayList<Seat>();
		List<Seat> freeSeat = new ArrayList<Seat>();

		boolean isFree;
		for (int i = 0; i < m_seats.length; i++)
		{
			isFree = m_seats[getSeatMod(i)].getSemaphore().availablePermits() == 1;

			if (isFree)
			{
				freeSeat.add(m_seats[i]);
				if (m_seats[getSeatMod(i - 1)].getSemaphore().availablePermits() == 1
						&& m_seats[getSeatMod(i + 1)].getSemaphore().availablePermits() == 1) {
					bestSeats.add(m_seats[i]);
				}
			}
		}

		Seat toReturn;

		if (bestSeats.size() > 0)
		{
			toReturn = bestSeats.get((int)(Math.random()*bestSeats.size()));
		}
		else if (freeSeat.size() > 0)
		{
			toReturn = freeSeat.get((int)(Math.random()*freeSeat.size()));
		}
		else
		{
			toReturn = m_seats[(int)(Math.random()*m_seats.length)];
		}		

		if (philosopher.getEaten() - 10 > toReturn.getMinimum())
		{
			return null;
		}
		else
		{
			return toReturn;
		}
	}

	public class Seat {
		private Object m_fork1;
		private Object m_fork2;

		int used = 0;

		int count = 0;
		List<Philosopher> philosophersThatHaveEatenHere = new ArrayList<Philosopher>(20);

		private Semaphore m_sem = new Semaphore(1, true);

		public Seat(Object f1, Object f2) {
			m_fork1 = f1;
			m_fork2 = f2;
		}

		public int getEaten() {
			return used;
		}

		public Object getFork1() {
			return m_fork1;
		}

		public Object getFork2() {
			return m_fork2;
		}

		public void eat(Philosopher philosopher)
		{
			if (m_globalMinimumPhilosph == philosopher || m_globalMinimumPhilosph == null || m_globalMinimum > philosopher.eatCount)
			{
				m_globalMinimum = philosopher.eatCount;
				m_globalMinimumPhilosph = philosopher;
			}
		}

		public int getMinimum()
		{
			return m_globalMinimum;
		}

		@Override
		public String toString() {
			return "Seat: " + m_fork1 + " " + m_fork2 + " used: " + used;
		}

		public Semaphore getSemaphore() {
			return m_sem;
		}


	}
}
