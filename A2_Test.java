public class A2_Test {

    static class RT_Hog extends UserlandProcess {
        @Override
        public void main() {
            while (true) {
                // Burn CPU longer than quantum (250ms)
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 400) {
                    // busy work
                }
                System.out.println("RT_Hog running (timeout expected)");

                cooperate();
            }
        }
    }

    // Realtime that sleeps often -> should NOT demote much
    static class RT_Sleeper extends UserlandProcess {
        @Override
        public void main() {
            while (true) {
                System.out.println("RT_Sleeper running -> Sleep(500)");
                OS.Sleep(500);
                cooperate();
            }
        }
    }

    // Background process to show it still runs sometimes
    static class BG_Talker extends UserlandProcess {
        @Override
        public void main() {
            while (true) {
                System.out.println("BG_Talker running");
                cooperate();
                try {
                    Thread.sleep(20);
                } catch (Exception e) {
                }
            }
        }
    }

    // Process that exits after a few prints
    static class ExitSoon extends UserlandProcess {
        @Override
        public void main() {
            for (int i = 0; i < 10; i++) {
                System.out.println("ExitSoon running: " + i);
                cooperate();
            }
            System.out.println("ExitSoon calling OS.Exit()");
            OS.Exit();
        }
    }

    public static void main(String[] args) {
        // Start OS with just Idle so something always runs
        OS.Startup(new UserlandProcess[]{new IdleProcess()});

        // Now create processes with explicit priorities
        OS.CreateProcess(new RT_Hog(), OS.PriorityType.realtime);
        OS.CreateProcess(new RT_Sleeper(), OS.PriorityType.realtime);
        OS.CreateProcess(new BG_Talker(), OS.PriorityType.background);
        OS.CreateProcess(new ExitSoon(), OS.PriorityType.interactive);
    }
}