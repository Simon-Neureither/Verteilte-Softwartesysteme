package aufgabe3_2;

import java.util.LinkedList;
import java.util.List;

public class BlockingQueue {
	
	private final List<Integer> storage = new LinkedList<>();
	
	public synchronized void push(int value){
		storage.add(value);
		notifyAll();
		System.out.printf("%d pushed%n", value);
	}
	
	public synchronized int pop(){
		while(storage.isEmpty()){
			try{wait();}
			catch(InterruptedException e){}
		}
		final int result = storage.remove(0);
		System.out.printf("%d poped%n", result);
		return result;
	}

}
