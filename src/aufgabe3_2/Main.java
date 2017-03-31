package aufgabe3_2;

public class Main{
	
	public static void main(String... args){
		final BlockingQueue queue = new BlockingQueue();
		for(int c = 0; c < (int)(Math.random()*10)+1; c++){
			new Thread(new Consumer(queue)).start();
		}
		for(int c = 0; c < (int)(Math.random()*10)+1; c++){
			new Thread(new Producer(queue)).start();
		}
	}

}
