package aufgabe4_2;/*
* Created by Robin Wismeth (robinwismeth@gmail.com) on 5/16/17.
*
* ------------------------ System ------------------------
* | Intel Core i7-5500U (2Cores, 4Threads @3GHz)         |
* | Intel HD Graphics                                    |
* | 8GB (2x4096) SO-DIMM DDR3 RAM 1600MHz Crucial        |
* | ArchLinux (4.9.8-1-ARCH)                             |
* | OpenJDK Runtime Environment (build 1.8.0_121-b13)    |
* --------------------------------------------------------
*/

//TODO Complete Javadoc

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Robin Wismeth, robinwismeth@gmail.com
 * @version 5/16/17
 */
public class HelloWorldImpl implements HelloWorld{

	@Override
	public void sayHello(){
		System.out.println("Hello World");
	}

}
