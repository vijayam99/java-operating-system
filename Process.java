import java.util.concurrent.Semaphore;

public abstract class Process implements Runnable{
    private final Thread thread;
    private final Semaphore sem;

    // set true when timer expires for this process
    private volatile boolean quantumExpired;
    public Process() {
	// implement here
        // start with 0 permits so process is initially stopped
        sem = new Semaphore(0);          // start "stopped"
        quantumExpired = false;

        thread = new Thread(this);
        thread.start();
    }

    public void requestStop() {
	// implement here
        quantumExpired = true;
    }

    public abstract void main(); // this is the class your subclasses will implement

    public boolean isStopped() {
	// implement here
        return sem.availablePermits() == 0;
    }

    public boolean isDone() {
	// implement here
        return !thread.isAlive();
    }

    public void start() {
	// implement here
        sem.release();
    }

    public void stop() {
	// implement here
        sem.acquireUninterruptibly();
    }

    public void run() { // This is called by the Thread - NEVER CALL THIS!!!
	// implement here
        // wait until OS/kernel starts us
        sem.acquireUninterruptibly();
        main();
    }

    public void cooperate() {
	// implement here
        if (quantumExpired) {
            quantumExpired = false;
            OS.switchProcess();
        }
    }
}
