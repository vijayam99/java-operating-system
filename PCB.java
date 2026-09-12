import java.util.LinkedList;

public class PCB { // Process Control Block
    private static int nextPid = 1;
    public final int pid;
    public final UserlandProcess ulp;
    private OS.PriorityType priority;

    // counts how many timeouts in a row
    private int timeoutStreak = 0;

    // set true when the timer fires for this process
    private boolean timedOutThisQuantum = false;

    // stores this process's open devices
    // local device id -> VFS id
    private final int[] devices = new int[10];

    // stores waiting messages for this process
    private final LinkedList<KernelMessage> messageQueue = new LinkedList<>();

    // page table: virtual page -> physical page
    private final int[] pageTable = new int[100];

    PCB(UserlandProcess up, OS.PriorityType priority) {
        this.ulp = up;
        this.priority = priority;

        this.pid = nextPid;
        nextPid++;

        // initialize all device slots to empty
        for (int i = 0; i < devices.length; i++) {
            devices[i] = -1;
        }

        // initialize page table to "no mapping"
        for (int i = 0; i < pageTable.length; i++) {
            pageTable[i] = -1;
        }
    }

    public String getName() {
        return ulp.getClass().getSimpleName();
    }

    OS.PriorityType getPriority() {
        return priority;
    }

    public void requestStop() {
        ulp.requestStop();
    }

    // Stop the process and wait until it blocks
    public void stop() { // calls userlandprocess’ stop. Loops until ulp.isStopped() is true.
        ulp.stop();
        while (!ulp.isStopped()) {
            try { Thread.sleep(10); } catch (Exception e) { }
        }
    }

    // Check if the process has finished
    public boolean isDone() { // calls userlandprocess’ isDone()
        return ulp.isDone();
    }

    void start() { // calls userlandprocess’ start()
        ulp.start();
    }

    public void setPriority(OS.PriorityType newPriority) {
        priority = newPriority;
    }

    // called by Scheduler timer when the process times out
    public void markTimedOutThisQuantum() {
        timedOutThisQuantum = true;
    }

    public boolean didTimeoutThisQuantum() {
        return timedOutThisQuantum;
    }

    public void clearTimeoutFlag() {
        timedOutThisQuantum = false;
    }

    public int getTimeoutStreak() {
        return timeoutStreak;
    }

    public void incrementTimeoutStreak() {
        timeoutStreak++;
    }

    public void resetTimeoutStreak() {
        timeoutStreak = 0;
    }

    // add a VFS id into first empty slot
    // returns local device id, or -1 if full
    public int addDevice(int vfsId) {
        for (int i = 0; i < devices.length; i++) {
            if (devices[i] == -1) {
                devices[i] = vfsId;
                return i;
            }
        }
        return -1;
    }

    // get the VFS id stored at local device id
    public int getDevice(int localId) {
        if (localId < 0 || localId >= devices.length) {
            return -1;
        }
        return devices[localId];
    }

    // remove a device from this process table
    public void removeDevice(int localId) {
        if (localId >= 0 && localId < devices.length) {
            devices[localId] = -1;
        }
    }

    // returns all device slots
    public int[] getDevices() {
        return devices;
    }

    // returns this process message queue
    public LinkedList<KernelMessage> getMessageQueue() {
        return messageQueue;
    }

    // returns this process page table
    public int[] getPageTable() {
        return pageTable;
    }
}