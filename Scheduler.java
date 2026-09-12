import java.time.Clock;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    // Ready queues by priority
    private final LinkedList<PCB> realtimeQ = new LinkedList<>();
    private final LinkedList<PCB> interactiveQ = new LinkedList<>();
    private final LinkedList<PCB> backgroundQ = new LinkedList<>();

    // Sleeping processes (not runnable until wake time)
    private final LinkedList<SleepEntry> sleeping = new LinkedList<>();

    private static class SleepEntry {
        PCB pcb;
        long wakeTime;

        SleepEntry(PCB pcb, long wakeTime) {
            this.pcb = pcb;
            this.wakeTime = wakeTime;
        }
    }

    private final Random rng = new Random();
    private final Clock clock = Clock.systemUTC();

    // Timer used to simulate hardware timer interrupt
    private final Timer timer = new Timer(true); // daemon timer

    // reference to kernel so scheduler can close devices
    private final Kernel kernel;

    // Currently running process
    public PCB currentlyRunning;

    public Scheduler(PCB[] startup, Kernel kernel) {
        this.kernel = kernel;

        for (PCB p : startup) {
            enqueueByPriority(p);
        }

        currentlyRunning = pickNextProcess();
        Hardware.clearTLB();

        // timer interrupt every 250ms
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (Scheduler.this) {
                    if (currentlyRunning != null && !currentlyRunning.isDone()) {
                        // Mark timeout for this quantum
                        currentlyRunning.markTimedOutThisQuantum();

                        // Count timeout streak RIGHT HERE (most reliable place)
                        currentlyRunning.incrementTimeoutStreak();

                        // Demote after 5 consecutive timeouts
                        if (currentlyRunning.getTimeoutStreak() >= 5) {
                            OS.PriorityType oldP = currentlyRunning.getPriority();

                            if (oldP == OS.PriorityType.realtime) {
                                currentlyRunning.setPriority(OS.PriorityType.interactive);
                            } else if (oldP == OS.PriorityType.interactive) {
                                currentlyRunning.setPriority(OS.PriorityType.background);
                            }

                            if (oldP != currentlyRunning.getPriority()) {
                                System.out.println("DEMOTE: " + currentlyRunning.getName()
                                        + " " + oldP + " -> " + currentlyRunning.getPriority());
                            }

                            currentlyRunning.resetTimeoutStreak();
                        }

                        // Force the running process to yield
                        currentlyRunning.requestStop();
                    }
                }
            }
        }, 250, 250);
    }

    public synchronized PCB CreateProcess(UserlandProcess up, OS.PriorityType p) {
        PCB pcb = new PCB(up, p);
        enqueueByPriority(pcb);
        return pcb;
    }

    public synchronized int GetPid() {
        if (currentlyRunning == null) return -1;
        return currentlyRunning.pid;
    }

    public synchronized void Exit() {
        // close all devices for exiting process
        if (currentlyRunning != null) {
            kernel.closeAllDevices(currentlyRunning);
        }

        currentlyRunning = null;
        wakeSleepingProcesses();
        currentlyRunning = pickNextProcess();
        Hardware.clearTLB();
    }

    public synchronized void Sleep(int milliseconds) {
        if (currentlyRunning == null) return;

        long wake = clock.millis() + milliseconds;

        // Sleeping should break timeout streak
        currentlyRunning.resetTimeoutStreak();
        currentlyRunning.clearTimeoutFlag();

        sleeping.add(new SleepEntry(currentlyRunning, wake));
        currentlyRunning = null;

        wakeSleepingProcesses();
        currentlyRunning = pickNextProcess();
        Hardware.clearTLB();
    }

    public synchronized void SwitchProcess() {
        // if process finished, close all devices and do not re-queue it
        if (currentlyRunning != null && currentlyRunning.isDone()) {
            kernel.closeAllDevices(currentlyRunning);
            currentlyRunning.clearTimeoutFlag();
            currentlyRunning.resetTimeoutStreak();
        }
        // Put old running process back in correct queue (if exists and not done)
        else if (currentlyRunning != null) {
            // If it did NOT timeout this quantum, it cooperated early -> reset streak
            if (!currentlyRunning.didTimeoutThisQuantum()) {
                currentlyRunning.resetTimeoutStreak();
            }

            // Clear flag for next quantum
            currentlyRunning.clearTimeoutFlag();

            // Re-queue it
            enqueueByPriority(currentlyRunning);
        }

        wakeSleepingProcesses();
        currentlyRunning = pickNextProcess();
        Hardware.clearTLB();
    }

    // put a process back into its runnable queue
    public synchronized void addToRunnableQueue(PCB pcb) {
        if (pcb != null) {
            enqueueByPriority(pcb);
        }
    }

    // ------- helper -------- //
    private void enqueueByPriority(PCB pcb) {
        switch (pcb.getPriority()) {
            case realtime -> realtimeQ.addLast(pcb);
            case interactive -> interactiveQ.addLast(pcb);
            case background -> backgroundQ.addLast(pcb);
        }
    }

    private PCB pickNextProcess() {
        boolean hasRT = !realtimeQ.isEmpty();
        boolean hasI  = !interactiveQ.isEmpty();
        boolean hasB  = !backgroundQ.isEmpty();

        if (!hasRT && !hasI && !hasB) return null;

        if (hasRT) {
            int r = rng.nextInt(10); // 0..9

            // 6/10 realtime
            if (r <= 5 && hasRT) return realtimeQ.removeFirst();

            // 3/10 interactive
            if (r <= 8 && hasI) return interactiveQ.removeFirst();

            // 1/10 background
            if (hasB) return backgroundQ.removeFirst();
            if (hasI) return interactiveQ.removeFirst();
            return realtimeQ.removeFirst();
        }

        if (hasI) {
            int r = rng.nextInt(4); // 0..3
            if (r == 0 && hasB) return backgroundQ.removeFirst();
            return interactiveQ.removeFirst();
        }

        return backgroundQ.removeFirst();
    }

    private void wakeSleepingProcesses() {
        long now = clock.millis();
        Iterator<SleepEntry> it = sleeping.iterator();

        while (it.hasNext()) {
            SleepEntry se = it.next();
            if (now >= se.wakeTime) {
                enqueueByPriority(se.pcb);
                it.remove();
            }
        }
    }
}