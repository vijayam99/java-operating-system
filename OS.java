import java.util.ArrayList;
import java.util.List;
public class OS {
    private static Kernel ki; // The one and only one instance of the kernel.
    // Shared syscall data area
    public static List<Object> parameters = new ArrayList<>();
    public static Object retVal;

    public enum CallType {
        SwitchProcess, SendMessage,
        Open, Close, Read, Seek, Write, GetMapping,
        CreateProcess, Sleep, GetPID,
        AllocateMemory, FreeMemory, GetPIDByName,
        WaitForMessage, Exit
    }
    public static CallType currentCall;
    private static void startTheKernel() {
        // Wake the kernel
        ki.start();
        // Stop whoever is currently running (blocks the caller thread)
        PCB running = ki.getCurrentlyRunning();
        if (running != null) {
            running.stop();
        }
    }
    public static void switchProcess() {
        parameters.clear();
        retVal = null;              // keep state clean
        currentCall = CallType.SwitchProcess;
        startTheKernel();
    }
    public static void Startup(UserlandProcess[] init) {
        // Create the one-and-only kernel instance
        ki = new Kernel(init);

        parameters.clear();
        retVal = null;
        currentCall = CallType.SwitchProcess;
        // Kick kernel so it selects and starts first process
        ki.start();
        // Startup "weirdness": wait until first process is selected
        while (ki.getCurrentlyRunning() == null) {
            try { Thread.sleep(10); } catch (Exception e) { }
        }
    }
    public enum PriorityType { realtime, interactive, background }
    public static int CreateProcess(UserlandProcess up, PriorityType priority) {
        parameters.clear();
        retVal = null;
        parameters.add(up);
        parameters.add(priority);
        currentCall = CallType.CreateProcess;
        startTheKernel();
        return (int) retVal;
    }
    public static int GetPID() {
        parameters.clear();
        retVal = null;
        currentCall = CallType.GetPID;
        startTheKernel();
        return (int) retVal;
    }
    public static void Exit() {
        parameters.clear();
        retVal = null;
        currentCall = CallType.Exit;
        startTheKernel();
    }
    public static void Sleep(int mills) {
        parameters.clear();
        retVal = null;
        parameters.add(mills);
        currentCall = CallType.Sleep;
        startTheKernel();
    }
    // Devices

    public static int Open(String s) {
        parameters.clear();
        retVal = null;
        parameters.add(s);
        currentCall = CallType.Open;
        startTheKernel();
        return (int) retVal;
    }

    public static void Close(int id) {
        parameters.clear();
        retVal = null;
        parameters.add(id);
        currentCall = CallType.Close;
        startTheKernel();
    }

    public static byte[] Read(int id, int size) {
        parameters.clear();
        retVal = null;
        parameters.add(id);
        parameters.add(size);
        currentCall = CallType.Read;
        startTheKernel();
        return (byte[]) retVal;
    }

    public static void Seek(int id, int to) {
        parameters.clear();
        retVal = null;
        parameters.add(id);
        parameters.add(to);
        currentCall = CallType.Seek;
        startTheKernel();
    }

    public static int Write(int id, byte[] data) {
        parameters.clear();
        retVal = null;
        parameters.add(id);
        parameters.add(data);
        currentCall = CallType.Write;
        startTheKernel();
        return (int) retVal;
    }

    // Messages
    public static void SendMessage(KernelMessage km) {
        parameters.clear();
        retVal = null;
        parameters.add(km);
        currentCall = CallType.SendMessage;
        startTheKernel();
    }

    public static KernelMessage WaitForMessage() {
        while (true) {
            parameters.clear();
            retVal = null;
            currentCall = CallType.WaitForMessage;
            startTheKernel();

            if (retVal != null) {
                return (KernelMessage) retVal;
            }
        }
    }

    public static int GetPidByName(String name) {
        parameters.clear();
        retVal = null;
        parameters.add(name);
        currentCall = CallType.GetPIDByName;
        startTheKernel();
        return (int) retVal;
    }

    // Memory
    public static void GetMapping(int virtualPage) {
        parameters.clear();
        retVal = null;
        parameters.add(virtualPage);
        currentCall = CallType.GetMapping;
        startTheKernel();
    }

    public static int AllocateMemory(int size) {
        parameters.clear();
        retVal = null;
        parameters.add(size);
        currentCall = CallType.AllocateMemory;
        startTheKernel();
        return (int) retVal;
    }

    public static boolean FreeMemory(int pointer, int size) {
        parameters.clear();
        retVal = null;
        parameters.add(pointer);
        parameters.add(size);
        currentCall = CallType.FreeMemory;
        startTheKernel();
        return (boolean) retVal;
    }
}