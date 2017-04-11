import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Table {

	Seat[] m_seats;
	
	public Table(int seats) {

		m_seats = new Seat[seats];
		
		Fork startFork = new Fork();
		Fork currentFork = new Fork();
		for (int i = 0; i < seats; i++) {
			if (i == 0) {
				m_seats[i] = new Seat(startFork, currentFork);
			} else if (i + 1 == seats) {
				m_seats[i] = new Seat(startFork, currentFork);
			} else if (i % 2 == 0) {
				m_seats[i] = new Seat(currentFork, (currentFork = new Fork()));
			} else {
				Fork f = new Fork();
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

	public Seat getSeat(Philosopher philosopher) {
		
		if (false)
		{
			int start = 0;
			int end = 0;
			
			int goodSeats[] = new int[m_seats.length];
			int size = 0;
			int reversedSize = m_seats.length - 1;
			
			boolean isFree;
						
			for (int i = 0; i < m_seats.length; i++)
			{
				isFree = m_seats[getSeatMod(i)].getSemaphore().availablePermits() == 1;
				
				if (isFree)
				{
					if (end != i - 1)
					{
						start = i;
					}
					end = i;
					
					if (end - start >= 3)
					{
						goodSeats[size++] = i - 1;
					}
					else
					{
						goodSeats[reversedSize--] = i;
					}
				}
			}
			
			Seat toReturn;
			
			if (size > 0)
			{
				toReturn = m_seats[goodSeats[(int)(Math.random()*size)]];
			}
			else if (reversedSize != m_seats.length - 1)
			{
				toReturn = m_seats[goodSeats[(int)(Math.random() * (m_seats.length - reversedSize) + reversedSize)]];
			}
			else
			{
				// TODO Seat mit wenigster queue.
				toReturn = m_seats[(int)(Math.random() * m_seats.length)];
			}
			
			if (philosopher.getEaten() - 10  > toReturn.getAverage() && toReturn.getAverage() > 0)
			{
				return null;
			}
			else
			{
				return toReturn;
			}
		}
		
		
		{
			
			List<Seat> bestSeats = new ArrayList<Seat>();
			List<Seat> freeSeat = new ArrayList<Seat>();
			
			List<Seat> lowestQueue = new ArrayList<Seat>();
			
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
				toReturn =  m_seats[(int)(Math.random()*m_seats.length)];
			}
			
			if (philosopher.getEaten() - 10  > toReturn.getAverage() && toReturn.getAverage() > 0)
			{
				return null;
			}
			else
			{
				return toReturn;
			}
		}

		/*
		int freeSeats = 0;
		Seat freeSeat = null;
		for (int i = 0; i < m_seats.length; i++) {
			if (m_seats[i].getSemaphore().availablePermits() == 1) {
				freeSeats++;
				freeSeat = m_seats[i];
			}
		}

		if (freeSeats == 0) {
			int randomIndex = (int) (Math.random() * m_seats.length);

			return m_seats[randomIndex];
		} else if (freeSeats < 3) {
			return freeSeat;
		} else {
			List<Seat> available = new ArrayList<Seat>();
			for (int i = 0; i < m_seats.length; i++) {
				if (m_seats[getSeatMod(i - 1)].getSemaphore().availablePermits() == 1
						&& m_seats[getSeatMod(i)].getSemaphore().availablePermits() == 1
						&& m_seats[getSeatMod(i + 1)].getSemaphore().availablePermits() == 1) {
					available.add(m_seats[i]);
				}
			}
			
			if (available.size() > 0)
			{
				return available.get((int)(Math.random() * available.size()));
			}
			
			return freeSeat;
		}
		*/
	}

	public class Seat {
		private Fork m_fork1;
		private Fork m_fork2;

		int used = 0;
		
		int count = 0;
		List<Philosopher> philosophersThatHaveEatenHere = new ArrayList<Philosopher>(20);
		
		private Semaphore m_sem = new Semaphore(1, true);

		public Seat(Fork f1, Fork f2) {
			m_fork1 = f1;
			m_fork2 = f2;
		}

		public int getEaten() {
			return used;
		}

		public Fork getFork1() {
			return m_fork1;
		}

		public Fork getFork2() {
			return m_fork2;
		}
		
		public void eat(Philosopher philosopher)
		{
			used++;
			if (!philosophersThatHaveEatenHere.contains(philosopher) && philosophersThatHaveEatenHere.size() < 20)
			{
				philosophersThatHaveEatenHere.add(philosopher);
				count++;
			}
		}
		
		public int getAverage()
		{
			if (count == 0)
				return 0;
			
			int x = 0;
			for (int i = 0; i < philosophersThatHaveEatenHere.size(); i++)
			{
				x += philosophersThatHaveEatenHere.get(i).getEaten();
			}
			
			return x / count;
		}

		@Override
		public String toString() {
			return "Seat: " + m_fork1 + " " + m_fork2 + " used: " + used;
		}

		public Semaphore getSemaphore() {
			return m_sem;
		}
		
		
	}

	// dummy.
	public class Fork {

	}

}
