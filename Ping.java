public class Ping extends UserlandProcess {
    @Override
    public void main() {
        int pongPid = -1;

        // wait until Pong exists
        while (pongPid == -1) {
            pongPid = OS.GetPidByName("Pong");
            if (pongPid == -1) {
                cooperate();
            }
        }

        System.out.println("I am PING, pong = " + pongPid);

        int what = 0;

        while (true) {
            OS.SendMessage(new KernelMessage(pongPid, what, null));

            KernelMessage km = OS.WaitForMessage();
            System.out.println("  PING: " + km);

            what++;
            cooperate();
        }
    }
}