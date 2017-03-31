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

package aufgabe3_3;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Robin Wismeth, robinwismeth@gmail.com
 * @version 3/30/17
 */
public class LockManager{

	private final BlockingQueue<Integer> releasedSlots = new LinkedBlockingQueue<>();

	public LockManager(final int slots){
		for(int index = 0; index < slots-1; index += 2){
			releasedSlots.add(index);
		}
		TimeStampPrinter.print(String.format("Create table with [%d] seats...%n", slots));
	}

	public int acquireSlot(){
		int acquiredSlot = -1;
		try{acquiredSlot = releasedSlots.take();}
		catch(InterruptedException e){e.printStackTrace();}
		TimeStampPrinter.print(String.format("\t#%d (Acquire [%d])%n", Thread.currentThread().getId(), acquiredSlot));
		return acquiredSlot;
	}

	public void releaseSlot(final int slot){
		try{releasedSlots.put(slot);}
		catch(InterruptedException e){e.printStackTrace();}
		TimeStampPrinter.print(String.format("\t#%d (Release [%d])%n", Thread.currentThread().getId(), slot));
	}

}
