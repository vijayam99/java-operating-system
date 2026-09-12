import java.util.HashMap;
import java.util.Random;

public class Kernel extends Process {
    private final Scheduler scheduler;
    private final VFS vfs;

    // pid -> pcb
    private final HashMap<Integer, PCB> pidMap = new HashMap<>();

    // processes waiting for a message
    private final HashMap<Integer, PCB> waitingMap = new HashMap<>();

    // physical memory free list: false = free, true = in use
    private final boolean[] physicalPagesInUse = new boolean[Hardware.NUM_PHYSICAL_PAGES];

    private final Random rng = new Random();

    public Kernel(UserlandProcess[] startup) {
        // create PCBs for startup processes (give all INTERACTIVE for now)
        PCB[] pcbs = new PCB[startup.length];
        for (int i = 0; i < startup.length; i++) {
            pcbs[i] = new PCB(startup[i], OS.PriorityType.interactive);
            pidMap.put(pcbs[i].pid, pcbs[i]);
        }

        vfs = new VFS();
        scheduler = new Scheduler(pcbs, this);
    }

    // small accessor so OS can stop currently-running process
    public PCB getCurrentlyRunning() {
        return scheduler.currentlyRunning;
    }

    @Override
    public void main() {
        while (true) { // Warning on infinite loop is OK...
            switch (OS.currentCall) { // get a job from OS, do it
                case SwitchProcess -> SwitchProcess();
                case Exit -> Exit();
                case CreateProcess -> OS.retVal = CreateProcess(
                        (UserlandProcess) OS.parameters.get(0),
                        (OS.PriorityType) OS.parameters.get(1)
                );
                case Sleep -> Sleep((int) OS.parameters.get(0));
                case GetPID -> OS.retVal = GetPid();

                case Open -> OS.retVal = Open((String) OS.parameters.get(0));
                case Close -> Close((int) OS.parameters.get(0));
                case Read -> OS.retVal = Read((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                case Seek -> Seek((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                case Write -> OS.retVal = Write((int) OS.parameters.get(0), (byte[]) OS.parameters.get(1));

                case SendMessage -> SendMessage((KernelMessage) OS.parameters.get(0));
                case WaitForMessage -> OS.retVal = WaitForMessage();
                case GetPIDByName -> OS.retVal = GetPidByName((String) OS.parameters.get(0));

                case GetMapping -> GetMapping((int) OS.parameters.get(0));
                case AllocateMemory -> OS.retVal = AllocateMemory((int) OS.parameters.get(0));
                case FreeMemory -> OS.retVal = FreeMemory((int) OS.parameters.get(0), (int) OS.parameters.get(1));

                default -> {
                }
            }

            // start the chosen user process
            if (scheduler.currentlyRunning != null) {
                scheduler.currentlyRunning.start();
            }

            // kernel goes to sleep until OS wakes it again
            stop();
        }
    }

    private void SwitchProcess() {
        scheduler.SwitchProcess();
    }

    private void Exit() {
        if (scheduler.currentlyRunning != null) {
            FreeAllMemory(scheduler.currentlyRunning);
            pidMap.remove(scheduler.currentlyRunning.pid);
            waitingMap.remove(scheduler.currentlyRunning.pid);
        }
        scheduler.Exit();
    }

    private int CreateProcess(UserlandProcess up, OS.PriorityType priority) {
        PCB pcb = scheduler.CreateProcess(up, priority);
        pidMap.put(pcb.pid, pcb);
        return pcb.pid;
    }

    private void Sleep(int mills) {
        scheduler.Sleep(mills);
    }

    private int GetPid() {
        return scheduler.GetPid();
    }

    private int Open(String s) {
        PCB current = scheduler.currentlyRunning;
        if (current == null) {
            return -1;
        }

        // ask VFS to open the device
        int vfsId = vfs.Open(s);
        if (vfsId == -1) {
            return -1;
        }

        // store VFS id in this process's device table
        int localId = current.addDevice(vfsId);
        if (localId == -1) {
            // pcb device table is full, so close VFS entry
            vfs.Close(vfsId);
            return -1;
        }

        // return local device id to user process
        return localId;
    }

    private void Close(int id) {
        PCB current = scheduler.currentlyRunning;
        if (current == null) {
            return;
        }

        // translate local id -> VFS id
        int vfsId = current.getDevice(id);
        if (vfsId == -1) {
            return;
        }

        vfs.Close(vfsId);
        current.removeDevice(id);
    }

    private byte[] Read(int id, int size) {
        PCB current = scheduler.currentlyRunning;
        if (current == null) {
            return null;
        }

        // translate local id -> VFS id
        int vfsId = current.getDevice(id);
        if (vfsId == -1) {
            return null;
        }

        return vfs.Read(vfsId, size);
    }

    private void Seek(int id, int to) {
        PCB current = scheduler.currentlyRunning;
        if (current == null) {
            return;
        }

        // translate local id -> VFS id
        int vfsId = current.getDevice(id);
        if (vfsId == -1) {
            return;
        }

        vfs.Seek(vfsId, to);
    }

    private int Write(int id, byte[] data) {
        PCB current = scheduler.currentlyRunning;
        if (current == null) {
            return -1;
        }

        // translate local id -> VFS id
        int vfsId = current.getDevice(id);
        if (vfsId == -1) {
            return -1;
        }

        return vfs.Write(vfsId, data);
    }

    // close all devices for a process
    public void closeAllDevices(PCB pcb) {
        int[] devices = pcb.getDevices();

        for (int i = 0; i < devices.length; i++) {
            if (devices[i] != -1) {
                vfs.Close(devices[i]);
                devices[i] = -1;
            }
        }
    }

    private void SendMessage(KernelMessage km) {
        PCB sender = scheduler.currentlyRunning;
        if (sender == null || km == null) {
            return;
        }

        KernelMessage copy = new KernelMessage(km);
        copy.senderPid = sender.pid;

        PCB target = pidMap.get(copy.targetPid);
        if (target == null) {
            return;
        }

        target.getMessageQueue().add(copy);

        if (waitingMap.containsKey(target.pid)) {
            PCB waiting = waitingMap.remove(target.pid);
            scheduler.addToRunnableQueue(waiting);
        }
    }

    private KernelMessage WaitForMessage() {
        PCB current = scheduler.currentlyRunning;
        if (current == null) {
            return null;
        }

        if (!current.getMessageQueue().isEmpty()) {
            return current.getMessageQueue().removeFirst();
        }

        current.resetTimeoutStreak();
        current.clearTimeoutFlag();

        waitingMap.put(current.pid, current);
        scheduler.currentlyRunning = null;
        scheduler.SwitchProcess();
        return null;
    }

    private int GetPidByName(String name) {
        for (PCB pcb : pidMap.values()) {
            if (pcb.getName().equals(name)) {
                return pcb.pid;
            }
        }
        return -1;
    }

    private void GetMapping(int virtualPage) {
        PCB current = scheduler.currentlyRunning;
        if (current == null) {
            return;
        }

        int[] pageTable = current.getPageTable();

        if (virtualPage < 0 || virtualPage >= pageTable.length) {
            System.out.println("seg fault");
            FreeAllMemory(current);
            pidMap.remove(current.pid);
            waitingMap.remove(current.pid);
            scheduler.Exit();
            return;
        }

        int physicalPage = pageTable[virtualPage];

        if (physicalPage == -1) {
            System.out.println("seg fault");
            FreeAllMemory(current);
            pidMap.remove(current.pid);
            waitingMap.remove(current.pid);
            scheduler.Exit();
            return;
        }

        int slot = rng.nextInt(2);
        Hardware.setTLBEntry(slot, virtualPage, physicalPage);
    }

    private int AllocateMemory(int size) {
        PCB current = scheduler.currentlyRunning;
        if (current == null) {
            return -1;
        }

        if (size <= 0 || size % Hardware.PAGE_SIZE != 0) {
            return -1;
        }

        int pagesNeeded = size / Hardware.PAGE_SIZE;
        int[] pageTable = current.getPageTable();

        // find contiguous hole in virtual page space
        int startVirtualPage = -1;
        int run = 0;

        for (int i = 0; i < pageTable.length; i++) {
            if (pageTable[i] == -1) {
                if (run == 0) {
                    startVirtualPage = i;
                }
                run++;

                if (run == pagesNeeded) {
                    break;
                }
            } else {
                run = 0;
                startVirtualPage = -1;
            }
        }

        if (run < pagesNeeded || startVirtualPage == -1) {
            return -1;
        }

        // find free physical pages
        int[] chosenPhysicalPages = new int[pagesNeeded];
        int found = 0;

        for (int i = 0; i < physicalPagesInUse.length && found < pagesNeeded; i++) {
            if (!physicalPagesInUse[i]) {
                chosenPhysicalPages[found] = i;
                found++;
            }
        }

        if (found < pagesNeeded) {
            return -1;
        }

        // assign mappings
        for (int i = 0; i < pagesNeeded; i++) {
            int vp = startVirtualPage + i;
            int pp = chosenPhysicalPages[i];

            pageTable[vp] = pp;
            physicalPagesInUse[pp] = true;
        }

        return startVirtualPage * Hardware.PAGE_SIZE;
    }

    private boolean FreeMemory(int pointer, int size) {
        PCB current = scheduler.currentlyRunning;
        if (current == null) {
            return false;
        }

        if (pointer < 0 || size <= 0) {
            return false;
        }

        if (pointer % Hardware.PAGE_SIZE != 0 || size % Hardware.PAGE_SIZE != 0) {
            return false;
        }

        int startVirtualPage = pointer / Hardware.PAGE_SIZE;
        int pagesToFree = size / Hardware.PAGE_SIZE;
        int[] pageTable = current.getPageTable();

        if (startVirtualPage < 0 || startVirtualPage + pagesToFree > pageTable.length) {
            return false;
        }

        // validate that all requested pages exist
        for (int i = 0; i < pagesToFree; i++) {
            if (pageTable[startVirtualPage + i] == -1) {
                return false;
            }
        }

        // free them
        for (int i = 0; i < pagesToFree; i++) {
            int vp = startVirtualPage + i;
            int pp = pageTable[vp];

            physicalPagesInUse[pp] = false;
            pageTable[vp] = -1;
        }

        Hardware.clearTLB();
        return true;
    }

    private void FreeAllMemory(PCB currentlyRunning) {
        if (currentlyRunning == null) {
            return;
        }

        int[] pageTable = currentlyRunning.getPageTable();

        for (int i = 0; i < pageTable.length; i++) {
            int pp = pageTable[i];
            if (pp != -1) {
                physicalPagesInUse[pp] = false;
                pageTable[i] = -1;
            }
        }

        Hardware.clearTLB();
    }
}
