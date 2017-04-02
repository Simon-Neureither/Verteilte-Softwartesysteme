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

	private static final ReentrantLock OPT_IN_ACCESS = new ReentrantLock(true);

	private static final int CYCLES = 3;

	private final Semaphore optOutAccess = new Semaphore(0);

	private final Distributor distributor;

	private int counter;

	private State state = State.IN_QUEUE;

	public Worker(final Distributor distributor){
		this.distributor = distributor;
	}

	private static void takeForks(final int index, final Distributor distributor) throws InterruptedException{
		final Worker worker;

		try{
			OPT_IN_ACCESS.lock();
			worker = distributor.get(index).get();
			worker.state = State.HUNGRY;
			test(index, distributor);
		}finally{
			OPT_IN_ACCESS.unlock();
		}

		worker.optOutAccess.acquire();
	}

	private static void test(final int index, final Distributor distributor){
		final Optional<Worker> worker = distributor.get(index);
		final Optional<Worker> left = distributor.get(index-1);
		final Optional<Worker> right = distributor.get(index+1);

		if(worker.isPresent() && worker.get().state == State.HUNGRY &&
				(!left.isPresent() || left.get().state != State.EATING) &&
				(!right.isPresent() || right.get().state != State.EATING)){

			worker.get().state = State.EATING;
			worker.get().optOutAccess.release();
		}
	}

	private static void putForks(final int index, final Distributor distributor) throws InterruptedException{
		try{
			OPT_IN_ACCESS.lock();
			distributor.get(index).get().state = State.IN_QUEUE;
			test(index-1, distributor);
			test(index+1, distributor);
		}finally{
			OPT_IN_ACCESS.unlock();
		}
	}

	@Override
	public void run(){
		while(counter++ < CYCLES){
			try{

				TimeStampPrinter.print(String.format("#%d (Thinking)%n", getId()));
				Thread.sleep(1000); // Thinking

				final int index = distributor.acquireSlot(this); // Wait for slot

				takeForks(index, distributor); // Wait for Forks
				TimeStampPrinter.print(String.format("\t#%d[%d] (take)%n", getId(), index));

				Thread.sleep(1000); // Eating

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
