public class PagingTest extends UserlandProcess {
    @Override
    public void main() {
        System.out.println("=== Paging Test ===");

        int ptr = OS.AllocateMemory(2048); // 2 pages
        System.out.println("allocated at virtual address = " + ptr);

        if (ptr == -1) {
            System.out.println("allocation failed");
            OS.Exit();
            return;
        }

        Hardware.Write(ptr, (byte) 42);
        Hardware.Write(ptr + 1024, (byte) 99);

        byte a = Hardware.Read(ptr);
        byte b = Hardware.Read(ptr + 1024);

        System.out.println("read first page value = " + a);
        System.out.println("read second page value = " + b);

        boolean freed = OS.FreeMemory(ptr, 2048);
        System.out.println("freed = " + freed);

        // should seg fault
        System.out.println("about to read freed memory...");
        byte c = Hardware.Read(ptr);
        System.out.println("this should not print: " + c);

        OS.Exit();
    }
}