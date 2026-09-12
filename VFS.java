import java.util.HashMap;
import java.util.Map;

public class VFS implements Device {

    // maps VFS id -> real device + real device id
    private static class VFSMapping {
        Device device;
        int deviceId;

        VFSMapping(Device device, int deviceId) {
            this.device = device;
            this.deviceId = deviceId;
        }
    }

    // table of open VFS entries
    private final VFSMapping[] mappings = new VFSMapping[20];

    // maps device name -> device object
    private final Map<String, Device> deviceMap = new HashMap<>();

    public VFS() {
        // register available device types
        deviceMap.put("random", new RandomDevice());
        deviceMap.put("file", new FakeFileSystem());
    }

    @Override
    public int Open(String s) {
        // invalid input
        if (s == null || s.trim().isEmpty()) {
            return -1;
        }

        String input = s.trim();
        String[] parts = input.split(" ", 2);

        // first word decides device type
        String deviceName = parts[0].trim().toLowerCase();

        // rest is argument (seed or filename)
        String arg = "";
        if (parts.length > 1) {
            arg = parts[1].trim();
        }

        // get device from map
        Device chosenDevice = deviceMap.get(deviceName);
        if (chosenDevice == null) {
            return -1; // unknown device
        }

        // open real device
        int realId = chosenDevice.Open(arg);
        if (realId == -1) {
            return -1;
        }

        // store mapping
        for (int i = 0; i < mappings.length; i++) {
            if (mappings[i] == null) {
                mappings[i] = new VFSMapping(chosenDevice, realId);
                return i; // return VFS id
            }
        }

        // no VFS slot, close device
        chosenDevice.Close(realId);
        return -1;
    }

    @Override
    public void Close(int id) {
        // invalid checks
        if (id < 0 || id >= mappings.length || mappings[id] == null) {
            return;
        }

        // close real device
        mappings[id].device.Close(mappings[id].deviceId);
        mappings[id] = null;
    }

    @Override
    public byte[] Read(int id, int size) {
        // invalid checks
        if (id < 0 || id >= mappings.length || mappings[id] == null) {
            return null;
        }

        return mappings[id].device.Read(mappings[id].deviceId, size);
    }

    @Override
    public void Seek(int id, int to) {
        // invalid checks
        if (id < 0 || id >= mappings.length || mappings[id] == null) {
            return;
        }

        mappings[id].device.Seek(mappings[id].deviceId, to);
    }

    @Override
    public int Write(int id, byte[] data) {
        // invalid checks
        if (id < 0 || id >= mappings.length || mappings[id] == null) {
            return -1;
        }

        return mappings[id].device.Write(mappings[id].deviceId, data);
    }
}