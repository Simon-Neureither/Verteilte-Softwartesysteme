/*
* Created by Robin Wismeth (robinwismeth@gmail.com) on 3/31/17.
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

/**
 * @author Robin Wismeth, robinwismeth@gmail.com
 * @version 3/31/17
 */
public class Main{

	public static void main(String... args) throws InterruptedException{
		int workerCount = 6;
		int slots = 4;

		if(args.length == 2){
			workerCount = Integer.valueOf(args[0]);
			slots = Integer.valueOf(args[1]);
		}

		TimeStampPrinter.print(String.format("Starting with %d slots for %d workers...%n", slots, workerCount));

		final Thread[] workers = new Thread[workerCount];
		final Distributor distributor = new Distributor(slots);

		final long startTime = System.currentTimeMillis();
		for(int index = 0; index < workerCount; index++){
			workers[index] = new Worker(1000, 1000, 3, distributor);
			workers[index].start();
		}

		for(int index = 0; index < workerCount; index++){
			workers[index].join();
		}

		final long duration = System.currentTimeMillis() - startTime;
		System.out.printf("Total time: %d%n", duration);
	}

}
