package service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.util.concurrent.*;
import java.util.*;
import static java.nio.file.StandardOpenOption.*;
import static org.junit.jupiter.api.Assertions.*;

class SequenceSafetyTest {
    @TempDir Path directory;
    @Test void missingFileNeverRestartsAtOne() throws Exception {
        Path file = directory.resolve("sequence.txt");
        assertThrows(SequenceException.class, () -> SequenceManager.getNextSequenceAndIncrement(file, 1));
        SequenceManager.initialize(file, 100);
        assertEquals(100, SequenceManager.getNextSequenceAndIncrement(file, 2));
        Files.delete(file);
        assertThrows(SequenceException.class, () -> SequenceManager.getNextSequenceAndIncrement(file, 1));
        assertFalse(Files.exists(file));
    }
    @Test void initializeNeverOverwritesAndBackupIsConsistent() throws Exception {
        Path file = directory.resolve("sequence.txt"), backup = directory.resolve("backup.txt");
        SequenceManager.initialize(file, 50);
        assertThrows(SequenceException.class, () -> SequenceManager.initialize(file, 1));
        SequenceManager.getNextSequenceAndIncrement(file, 3);
        SequenceManager.backup(file, backup);
        assertEquals("53", Files.readString(backup));
        assertThrows(SequenceException.class, () -> SequenceManager.backup(file, backup));
        assertEquals("53", Files.readString(file));
    }
    @Test void lockedFileAndOverflowNeverChangeStoredValue() throws Exception {
        Path file = directory.resolve("sequence.txt"); SequenceManager.initialize(file, Long.MAX_VALUE);
        assertThrows(SequenceException.class, () -> SequenceManager.getNextSequenceAndIncrement(file, 1));
        assertEquals(Long.toString(Long.MAX_VALUE), Files.readString(file));
        try (var channel = FileChannel.open(directory.resolve("sequence.txt.lock"), CREATE, WRITE); var lock = channel.lock()) {
            assertThrows(SequenceException.class, () -> SequenceManager.getNextSequenceAndIncrement(file, 1));
        }
    }
    @Test void concurrentReservationsAreUnique() throws Exception {
        Path file = directory.resolve("sequence.txt"); SequenceManager.initialize(file, 100);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Long>> calls = new ArrayList<>();
            for (int i = 0; i < 20; i++) calls.add(() -> SequenceManager.getNextSequenceAndIncrement(file, 3));
            Set<Long> starts = new HashSet<>();
            for (Future<Long> result : pool.invokeAll(calls)) starts.add(result.get());
            assertEquals(20, starts.size()); assertEquals("160", Files.readString(file));
        } finally { pool.shutdownNow(); }
    }
    @Test void separateJvmContinuesSequenceAfterRestart() throws Exception {
        Path file = directory.resolve("sequence.txt"); SequenceManager.initialize(file, 100);
        for (int i = 0; i < 2; i++) {
            Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), SequenceProcess.class.getName(), file.toString())
                .redirectErrorStream(true).redirectOutput(directory.resolve("child-" + i + ".log").toFile()).start();
            try { assertTrue(process.waitFor(20, TimeUnit.SECONDS)); assertEquals(0, process.exitValue()); }
            finally { process.destroyForcibly(); }
        }
        assertEquals("106", Files.readString(file));
    }
    public static class SequenceProcess {
        public static void main(String[] args) { SequenceManager.getNextSequenceAndIncrement(Path.of(args[0]), 3); }
    }
}
