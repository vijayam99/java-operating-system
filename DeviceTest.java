public class DeviceTest extends UserlandProcess {
    @Override
    public void main() {

        System.out.println("=== Basic Device Test ===");

        // open random device with seed 100
        int r = OS.Open("random 100");
        System.out.println("random local id = " + r);

        // read 10 random bytes
        byte[] randomData = OS.Read(r, 10);
        if (randomData != null) {
            System.out.println("random bytes length = " + randomData.length);
        }

        OS.Close(r);

        // open fake file
        int f = OS.Open("file test.txt");
        System.out.println("file local id = " + f);

        // write to file
        int written = OS.Write(f, "Hello File".getBytes());
        System.out.println("bytes written = " + written);

        // go back to start of file
        OS.Seek(f, 0);

        // read from file
        byte[] fileData = OS.Read(f, 20);
        if (fileData != null) {
            System.out.println("file contents = " + new String(fileData));
        }

        OS.Close(f);


        // limit / stress test


        System.out.println("\n=== Device Limit Test ===");

        int[] ids = new int[12];

        // try opening more than PCB limit (10)
        for (int i = 0; i < ids.length; i++) {
            ids[i] = OS.Open("random");
            System.out.println("open " + i + " -> " + ids[i]);
        }

        // close valid ones
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] != -1) {
                OS.Close(ids[i]);
            }
        }

        // end this process
        OS.Exit();
    }
}