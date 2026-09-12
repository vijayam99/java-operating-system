public class Main {
    public static void main(String[] args) {
        // Initial user processes
        UserlandProcess[] init = new UserlandProcess[]{
                new PagingTest(),
                new MemoryProcessA(),
                new MemoryProcessB(),
                new IdleProcess()
        };

        // Start the operating system
        OS.Startup(init);
    }
}