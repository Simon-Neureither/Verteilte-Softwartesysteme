package aufgabe3_2;

public class Producer implements Runnable {
	
	private final BlockingQueue queue;
	
	public Producer(final BlockingQueue queue){
		this.queue = queue;
	}

	@Override
	public void run() {
		System.out.println("Producer started");
		while(!Thread.currentThread().isInterrupted()){
			queue.push((int)(Math.random()*10));
			try {
				Thread.sleep((int)(Math.random()*2000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
