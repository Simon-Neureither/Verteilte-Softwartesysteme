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

package aufgabe3_3.worker;

/**
 * @author Robin Wismeth, robinwismeth@gmail.com
 * @version 3/31/17
 */
class DoneWorking implements Worker.State{

	@Override
	public Worker.State handle(Worker context){
		context.lockManager.releaseSlot(context.slot);
		context.slot = -1;

		if(context.blockCounter < 3){
			try{
				Thread.sleep(1000);
				return new DoneWaiting();
			}catch(InterruptedException e){
				return new ErrorState("Sleep interrupted!");
			}
		}else{
			return END_STATE;
		}
	}

}
