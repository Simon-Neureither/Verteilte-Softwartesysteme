package aufgabe3_3;/*
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

/**
 * @author Robin Wismeth, robinwismeth@gmail.com
 * @version 4/1/17
 */
public interface TimeStampPrinter{

	long startTime = System.currentTimeMillis();

	static void print(final String msg){
		System.out.printf("[%7.3fs] %s", (System.currentTimeMillis()-startTime)/1000F, msg);
	}

}
