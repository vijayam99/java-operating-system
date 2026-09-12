import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device {

    // holds up to 10 open files
    private final RandomAccessFile[] files = new RandomAccessFile[10];

    @Override
    public int Open(String s) {
        // filename must be provided
        if (s == null || s.trim().isEmpty()) {
            throw new RuntimeException("Filename cannot be empty");
        }

        // find empty slot
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                try {
                    // open file in read/write mode
                    files[i] = new RandomAccessFile(s.trim(), "rw");
                    return i;
                } catch (IOException e) {
                    return -1;
                }
            }
        }

        return -1; // no free slot
    }

    @Override
    public void Close(int id) {
        // invalid checks
        if (id < 0 || id >= files.length || files[id] == null) {
            return;
        }

        // close file safely
        try {
            files[id].close();
        } catch (IOException e) {
            // ignore close error
        }

        files[id] = null;
    }

    @Override
    public byte[] Read(int id, int size) {
        // invalid checks
        if (id < 0 || id >= files.length || files[id] == null || size < 0) {
            return null;
        }

        try {
            byte[] buffer = new byte[size];
            int bytesRead = files[id].read(buffer);

            // end of file
            if (bytesRead == -1) {
                return new byte[0];
            }

            // full read
            if (bytesRead == size) {
                return buffer;
            }

            // partial read
            byte[] smaller = new byte[bytesRead];
            System.arraycopy(buffer, 0, smaller, 0, bytesRead);
            return smaller;

        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void Seek(int id, int to) {
        // invalid checks
        if (id < 0 || id >= files.length || files[id] == null || to < 0) {
            return;
        }

        // move file pointer
        try {
            files[id].seek(to);
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        // invalid checks
        if (id < 0 || id >= files.length || files[id] == null || data == null) {
            return -1;
        }

        try {
            files[id].write(data);
            return data.length;
        } catch (IOException e) {
            return -1;
        }
    }
}