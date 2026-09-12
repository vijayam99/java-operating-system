public class Hardware {
    public static final int PAGE_SIZE = 1024;
    public static final int NUM_PHYSICAL_PAGES = 1024;
    public static final int MEMORY_SIZE = PAGE_SIZE * NUM_PHYSICAL_PAGES; // 1MB

    // simulated physical memory
    private static final byte[] memory = new byte[MEMORY_SIZE];

    // tlb[row][0] = virtual page, tlb[row][1] = physical page
    private static final int[][] tlb = new int[2][2];

    static {
        clearTLB();
    }

    public static void clearTLB() {
        for (int i = 0; i < tlb.length; i++) {
            tlb[i][0] = -1;
            tlb[i][1] = -1;
        }
    }

    public static void setTLBEntry(int slot, int virtualPage, int physicalPage) {
        if (slot < 0 || slot >= tlb.length) {
            return;
        }
        tlb[slot][0] = virtualPage;
        tlb[slot][1] = physicalPage;
    }

    private static int findPhysicalPage(int virtualPage) {
        for (int i = 0; i < tlb.length; i++) {
            if (tlb[i][0] == virtualPage) {
                return tlb[i][1];
            }
        }
        return -1;
    }

    public static byte Read(int address) {
        if (address < 0) {
            throw new RuntimeException("Invalid address: " + address);
        }

        int virtualPage = address / PAGE_SIZE;
        int offset = address % PAGE_SIZE;

        int physicalPage = findPhysicalPage(virtualPage);

        if (physicalPage == -1) {
            OS.GetMapping(virtualPage);
            physicalPage = findPhysicalPage(virtualPage);

            if (physicalPage == -1) {
                throw new RuntimeException("Segmentation fault on Read at address " + address);
            }
        }

        int physicalAddress = physicalPage * PAGE_SIZE + offset;
        return memory[physicalAddress];
    }

    public static void Write(int address, byte value) {
        if (address < 0) {
            throw new RuntimeException("Invalid address: " + address);
        }

        int virtualPage = address / PAGE_SIZE;
        int offset = address % PAGE_SIZE;

        int physicalPage = findPhysicalPage(virtualPage);

        if (physicalPage == -1) {
            OS.GetMapping(virtualPage);
            physicalPage = findPhysicalPage(virtualPage);

            if (physicalPage == -1) {
                throw new RuntimeException("Segmentation fault on Write at address " + address);
            }
        }

        int physicalAddress = physicalPage * PAGE_SIZE + offset;
        memory[physicalAddress] = value;
    }
}