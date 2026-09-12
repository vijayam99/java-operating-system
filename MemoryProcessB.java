public class MemoryProcessB extends UserlandProcess {
    @Override
    public void main() {
        System.out.println("=== MemoryProcessB starting ===");

        int ptr = OS.AllocateMemory(1024); // 1 page
        System.out.println("B allocated at = " + ptr);

        if (ptr == -1) {
            System.out.println("B allocation failed");
            OS.Exit();
            return;
        }

        Hardware.Write(ptr, (byte) 22);
        byte value = Hardware.Read(ptr);

        System.out.println("B read value = " + value);

        // let the other process run too
        cooperate();
        try { Thread.sleep(100); } catch (Exception e) { }

        // read again later to prove still unchanged
        value = Hardware.Read(ptr);
        System.out.println("B read again = " + value);

        OS.Exit();
    }
}