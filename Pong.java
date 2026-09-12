public class Pong extends UserlandProcess {
    @Override
    public void main() {
        int pingPid = -1;

        // wait until Ping exists
        while (pingPid == -1) {
            pingPid = OS.GetPidByName("Ping");
            if (pingPid == -1) {
                cooperate();
            }
        }

        System.out.println("I am PONG, ping = " + pingPid);

        while (true) {
            KernelMessage km = OS.WaitForMessage();
            System.out.println("  PONG: " + km);

            OS.SendMessage(new KernelMessage(pingPid, km.what, null));
            cooperate();
        }
    }
}