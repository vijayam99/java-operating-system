# Java Operating System

A Java operating system simulation I built for my Operating Systems class. The project brings together process scheduling, system calls, device management, interprocess communication, and virtual memory.

## Features

- **Process management:** Java threads and semaphores represent and coordinate user processes and the kernel. Each process has a process control block (PCB).
- **Priority scheduling:** Realtime, interactive, and background queues use probabilistic selection. A 250 ms timer requests a yield, and processes switch when they call `cooperate()`. Repeated timeouts can lower a process's priority.
- **System calls:** The `OS` class exposes process creation, sleep, exit, PID lookup, device operations, messaging, and memory operations.
- **Device management:** A virtual file system (VFS) routes requests to a seeded random device or a file device backed by `RandomAccessFile`.
- **Interprocess communication:** Processes send copied messages, look up processes by name, and block while waiting for a message.
- **Virtual memory:** Per-process page tables translate virtual addresses into physical addresses through a two-entry translation lookaside buffer (TLB). Memory uses 1,024-byte pages and 1 MiB of simulated physical memory.

## Project structure

All Java source files are in `src/`.

| Files | Role |
| --- | --- |
| `Main.java` | Entry point for the default memory demonstration |
| `Process.java`, `UserlandProcess.java`, `PCB.java` | Process lifecycle and per-process state |
| `OS.java`, `Kernel.java` | System call interface and kernel operations |
| `Scheduler.java`, `IdleProcess.java` | Scheduling, sleeping processes, and idle execution |
| `Device.java`, `VFS.java`, `RandomDevice.java`, `FakeFileSystem.java` | Device interface and implementations |
| `KernelMessage.java`, `Ping.java`, `Pong.java` | Message passing and demonstration processes |
| `Hardware.java` | Simulated physical memory and TLB translation |
| `PagingTest.java`, `MemoryProcessA.java`, `MemoryProcessB.java` | Memory demonstrations |
| `A2_Test.java` | Scheduling demonstration with different priorities |
| `DeviceTest.java` | Device reads, writes, seeking, and open limits |
| `HelloWorld.java`, `GoodbyeWorld.java` | Basic process demonstrations |

## Build and run

Use a **JDK**, which includes both `java` and `javac`. JDK 21 is a suitable choice for this project. No external libraries, Maven, or Gradle are needed.

From a terminal:

```sh
git clone https://github.com/vijayam99/java-operating-system.git
cd java-operating-system
mkdir out
javac -d out src/*.java
java -cp out Main
```

In IntelliJ IDEA, open the repository, configure a JDK, mark `src` as a Sources Root if needed, and run `Main.main()`.

### Default demonstration

`Main` starts `PagingTest`, `MemoryProcessA`, `MemoryProcessB`, and `IdleProcess`.

- `PagingTest` writes and reads the values 42 and 99 across two pages, frees the allocation, and deliberately reads freed memory to exercise the segmentation-fault path.
- `MemoryProcessA` and `MemoryProcessB` each allocate a page and read back their own values, 11 and 22.
- Output order depends on scheduling.
- The idle process runs indefinitely. Stop the program with **Ctrl+C** or your IDE's Stop button.

These are console demonstrations rather than an automated assertion-based test suite.

### Other demonstrations

To launch the existing scheduling demonstration:

```sh
java -cp out A2_Test
```

To try the device demonstration, replace the startup array in `Main` with:

```java
UserlandProcess[] init = new UserlandProcess[]{
        new DeviceTest(),
        new IdleProcess()
};
OS.Startup(init);
```

For message passing, use:

```java
UserlandProcess[] init = new UserlandProcess[]{
        new Ping(),
        new Pong(),
        new IdleProcess()
};
OS.Startup(init);
```

Recompile after changing `Main.java`. Run each demonstration in a fresh JVM. The device demonstration creates or updates `test.txt` in the working directory.

## About

Built by **Vijayam Gupta** as an Operating Systems course project. This project models OS concepts inside a Java application.
