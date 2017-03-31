package aufgabe3_2;

public class Consumer implements Runnable {
	
	private final BlockingQueue queue;
	
	public Consumer(final BlockingQueue queue){
		this.queue = queue;
	}

	@Override
	public void run() {
		System.out.println("Consumer started");
		while(!Thread.currentThread().isInterrupted()){
			queue.pop();
			try {
				Thread.sleep((int)(Math.random()*2000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
