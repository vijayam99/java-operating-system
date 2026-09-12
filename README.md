# Operating System Simulator | Java

An operating system simulator developed for my Operating Systems course at the University at Albany. It models how a kernel coordinates processes, handles system calls, manages devices, exchanges messages, and translates virtual memory addresses.

**Technologies:** Java, multithreading, semaphores, collections, and file I/O.

## Technical highlights

- Implemented three priority queues with probabilistic process selection, a 250 ms timer, and priority demotion after repeated timeouts.
- Built a system call interface connecting user processes to kernel services through semaphore-based coordination.
- Implemented message queues with copied payloads and blocking receive operations.
- Built a virtual file system that translates per-process device handles into shared device mappings.
- Implemented per-process page tables with 100 virtual pages, a two-entry TLB, and allocation and deallocation of 1,024-byte pages within 1 MiB of simulated physical memory.

## Features

- **Process management:** Java threads and semaphores represent and coordinate user processes and the kernel. Each process has a process control block (PCB).
- **Priority scheduling:** Realtime, interactive, and background queues use probabilistic selection. A 250 ms timer requests a yield, and processes switch when they call `cooperate()`. Repeated timeouts can lower a process's priority.
- **System calls:** The `OS` class exposes process creation, sleep, exit, PID lookup, device operations, messaging, and memory operations.
- **Device management:** A virtual file system (VFS) routes requests to a seeded random device or a file device backed by `RandomAccessFile`.
- **Interprocess communication:** Processes send copied messages, look up processes by name, and block while waiting for a message.
- **Virtual memory:** Per-process page tables translate virtual addresses into physical addresses through a two-entry translation lookaside buffer (TLB). Memory uses 1,024-byte pages and 1 MiB of simulated physical memory.

## Project structure

All Java source files are in the repository's root folder.

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
javac -d out *.java
java -cp out Main
```

In IntelliJ IDEA, open the repository, configure a JDK, mark the repository folder as a Sources Root if needed (and exclude `out`), and run `Main.main()`.

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

## Design and scope

The simulator runs within the JVM. Java threads represent processes, and semaphores coordinate execution between user processes and the kernel. The timer requests a yield; a process switches when it reaches `cooperate()`.

Memory allocation assigns physical pages immediately. The TLB caches translations and is cleared on context switches. File-device operations use the host file system through `RandomAccessFile`.

The included demonstrations exercise individual OS concepts through console output. They are not a comprehensive automated test suite or a performance benchmark.

## Concepts demonstrated

- Process control blocks, priority queues, and cooperative scheduling
- Thread synchronization and kernel/user-process coordination
- Device abstraction and handle translation
- Interprocess communication and blocking operations
- Virtual-to-physical address translation, page allocation, and TLB invalidation

## About

Built by **Vijayam Gupta** as an Operating Systems course project. This project models OS concepts inside a Java application.

