/*
* Created by Robin Wismeth (robinwismeth@gmail.com) on 3/30/17.
*
* ------------------------ System ------------------------
* | Intel Core i7-5500U (2Cores, 4Threads @3GHz)         |
* | Intel HD Graphics                                    |
* | 8GB (2x4096) SO-DIMM DDR3 RAM 1600MHz Crucial        |
* | ArchLinux (4.9.8-1-ARCH)                             |
* | OpenJDK Runtime Environment (build 1.8.0_121-b13)    |
* --------------------------------------------------------
*/

package aufgabe3_3.worker;

import aufgabe3_3.LockManager;
import aufgabe3_3.TimeStampPrinter;

/**
 * @author Robin Wismeth, robinwismeth@gmail.com
 * @version 3/30/17
 */
public class Worker implements Runnable{

	LockManager lockManager;

	State currentState = new DoneWaiting();

	int slot = -1;

	int blockCounter;

	public static Thread create(final LockManager lockManager){
		final Worker worker = new Worker();
		worker.lockManager = lockManager;

		return new Thread(worker);
	}

	@Override
	public void run(){
		TimeStampPrinter.print(String.format("Worker #%d spawned!%n", Thread.currentThread().getId()));

		while(currentState != State.END_STATE){
			final State oldState = currentState;
			currentState = currentState.handle(this);

			TimeStampPrinter.print(String.format(
					"#%d (%s -> %s)%n",
					Thread.currentThread().getId(),
					oldState.getClass().getSimpleName(),
					currentState.getClass().getSimpleName()));
		}
	}

	interface State{

		State END_STATE = new State(){
			@Override
			public State handle(Worker context){return this;}
		};

		State handle(final Worker context);

	}

}
