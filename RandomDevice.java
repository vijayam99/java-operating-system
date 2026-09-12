import java.util.Random;

public class RandomDevice implements Device {

    // holds up to 10 random generators
    private final Random[] randoms = new Random[10];

    @Override
    public int Open(String s) {
        // find empty slot
        for (int i = 0; i < randoms.length; i++) {
            if (randoms[i] == null) {

                // if no seed provided, use default random
                if (s == null || s.trim().isEmpty()) {
                    randoms[i] = new Random();
                }
                // if seed provided, use it
                else {
                    int seed = Integer.parseInt(s.trim());
                    randoms[i] = new Random(seed);
                }

                return i; // return device id
            }
        }

        return -1; // no free slot
    }

    @Override
    public void Close(int id) {
        // remove generator from slot
        if (id >= 0 && id < randoms.length) {
            randoms[id] = null;
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        // invalid checks
        if (id < 0 || id >= randoms.length || randoms[id] == null || size < 0) {
            return null;
        }

        // generate random bytes
        byte[] data = new byte[size];
        randoms[id].nextBytes(data);
        return data;
    }

    @Override
    public void Seek(int id, int to) {
        // invalid checks
        if (id < 0 || id >= randoms.length || randoms[id] == null || to < 0) {
            return;
        }

        // consume bytes without returning
        byte[] temp = new byte[to];
        randoms[id].nextBytes(temp);
    }

    @Override
    public int Write(int id, byte[] data) {
        // random device does not support writing
        return 0;
    }
}