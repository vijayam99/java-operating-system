public class MemoryProcessA extends UserlandProcess {
    @Override
    public void main() {
        System.out.println("=== MemoryProcessA starting ===");

        int ptr = OS.AllocateMemory(1024); // 1 page
        System.out.println("A allocated at = " + ptr);

        if (ptr == -1) {
            System.out.println("A allocation failed");
            OS.Exit();
            return;
        }

        Hardware.Write(ptr, (byte) 11);
        byte value = Hardware.Read(ptr);

        System.out.println("A read value = " + value);

        // let the other process run too
        cooperate();
        try { Thread.sleep(100); } catch (Exception e) { }

        // read again later to prove still unchanged
        value = Hardware.Read(ptr);
        System.out.println("A read again = " + value);

        OS.Exit();
    }
}