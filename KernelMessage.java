public class KernelMessage {
    public int senderPid;
    public int targetPid;
    public int what;
    public byte[] data;

    public KernelMessage(int targetPid, int what, byte[] data) {
        this.senderPid = -1;
        this.targetPid = targetPid;
        this.what = what;
        this.data = (data == null) ? null : data.clone();
    }

    // copy constructor
    public KernelMessage(KernelMessage other) {
        this.senderPid = other.senderPid;
        this.targetPid = other.targetPid;
        this.what = other.what;
        this.data = (other.data == null) ? null : other.data.clone();
    }

    @Override
    public String toString() {
        return "from: " + senderPid + " to: " + targetPid + " what: " + what;
    }
}