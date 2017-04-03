/*
* Created by Robin Wismeth (robinwismeth@gmail.com) on 4/1/17.
*
* ------------------------ System ------------------------
* | Intel Core i7-5500U (2Cores, 4Threads @3GHz)         |
* | Intel HD Graphics                                    |
* | 8GB (2x4096) SO-DIMM DDR3 RAM 1600MHz Crucial        |
* | ArchLinux (4.9.8-1-ARCH)                             |
* | OpenJDK Runtime Environment (build 1.8.0_121-b13)    |
* --------------------------------------------------------
*/

package aufgabe3_3;

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Robin Wismeth, robinwismeth@gmail.com
 * @version 4/1/17
 */
public class Worker extends Thread{

	private static final ReentrantLock ACTIVE_STATE_CHANGE = new ReentrantLock(true);

	private final int cycles;
	
	private final long thinkTime;
	
	private final long eatTime;

	private final Semaphore passiveStateChange = new Semaphore(0);

	private final Distributor distributor;

	private int counter;

	private State state = State.IN_QUEUE;

	public Worker(final int thinkTime, final int eatTime, final int cycles, final Distributor distributor){
		this.thinkTime = thinkTime;
		this.eatTime = eatTime;
		this.distributor = distributor;
		this.cycles = cycles;
	}

	private static void takeForks(final int index, final Distributor distributor) throws InterruptedException{
		final Worker worker;

		try{
			ACTIVE_STATE_CHANGE.lock();
			worker = distributor.get(index).get();
			worker.state = State.HUNGRY;
			test(index, distributor);
		}finally{
			ACTIVE_STATE_CHANGE.unlock();
		}

		worker.passiveStateChange.acquire();
	}

	private static void test(final int index, final Distributor distributor){
		final Optional<Worker> worker = distributor.get(index);
		final Optional<Worker> left = distributor.get(index-1);
		final Optional<Worker> right = distributor.get(index+1);

		if(worker.isPresent() && worker.get().state == State.HUNGRY &&
				(!left.isPresent() || left.get().state != State.EATING) &&
				(!right.isPresent() || right.get().state != State.EATING)){

			worker.get().state = State.EATING;
			worker.get().passiveStateChange.release();
		}
	}

	private static void putForks(final int index, final Distributor distributor) throws InterruptedException{
		try{
			ACTIVE_STATE_CHANGE.lock();
			distributor.get(index).get().state = State.IN_QUEUE;
			test(index-1, distributor);
			test(index+1, distributor);
		}finally{
			ACTIVE_STATE_CHANGE.unlock();
		}
	}

	@Override
	public void run(){
		while(counter++ < cycles){
			try{

				TimeStampPrinter.print(String.format("#%d (Thinking)%n", getId()));
				Thread.sleep(thinkTime); // Thinking

				final int index = distributor.acquireSlot(this); // Wait for slot

				takeForks(index, distributor); // Wait for Forks
				TimeStampPrinter.print(String.format("\t#%d[%d] (take)%n", getId(), index));

				Thread.sleep(eatTime); // Eating

				TimeStampPrinter.print(String.format("#%d (Eating) {no. %d}%n", getId(), counter));
				putForks(index, distributor); // Put down forks

				distributor.releaseSlot(index); // sit up and walk away
				TimeStampPrinter.print(String.format("\t#%d[%d] (put)%n", getId(), index));

			}catch(InterruptedException e){
				e.printStackTrace();
				break;
			}
		}
	}

	private enum State{
		IN_QUEUE, HUNGRY, EATING;
	}

}
