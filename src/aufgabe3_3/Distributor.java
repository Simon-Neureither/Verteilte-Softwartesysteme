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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robin Wismeth, robinwismeth@gmail.com
 * @version 4/1/17
 */
public class Distributor{

	private final static AtomicInteger GLOBAL_INDEX = new AtomicInteger();

	private final List<Queue<Worker>> queues = new ArrayList<>();

	public Distributor(final int slots){
		for(int count = 0; count < slots; count++){
			queues.add(new LinkedList<>());
		}
	}

	public int acquireSlot(final Worker worker) throws InterruptedException{
		final int index = GLOBAL_INDEX.updateAndGet(in -> (in + 1) % queues.size());
		final Queue<Worker> selected = queues.get(index);

		synchronized(selected){
			selected.add(worker);
			while(selected.peek() != worker){
				selected.wait();
			}
		}

		return index;
	}

	public void releaseSlot(final int index){
		final Queue<Worker> selected = queues.get(index);
		synchronized(selected){
			selected.poll();
			selected.notifyAll();
		}
	}

	public Optional<Worker> get(final int index){
		final Queue<Worker> selected = queues.get(((index % queues.size())+queues.size())%queues.size());
		final Optional<Worker> result;

		synchronized(selected){
			result = Optional.ofNullable(selected.peek());
		}

		return result;
	}

}
