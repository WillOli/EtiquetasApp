package service;

import config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;

/** Reserva persistente. Arquivo ausente exige inicialização administrativa explícita. */
public final class SequenceManager {
    private static final Logger logger = LoggerFactory.getLogger(SequenceManager.class);
    private SequenceManager() {}

    public static long getNextSequenceAndIncrement(int quantity) {
        return getNextSequenceAndIncrement(AppConfig.getSequenceFilePath(), quantity);
    }

    public static synchronized long getNextSequenceAndIncrement(Path file, int quantity) {
        if (quantity < 1) throw new SequenceException("A quantidade de registros deve ser positiva.");
        return locked(file, path -> {
            long current = read(path);
            long next = Math.addExact(current, quantity);
            replace(path, next);
            logger.info("Registros reservados: {} a {}. Próximo: {}.", current, next - 1, next);
            return current;
        });
    }

    /** Somente em primeira instalação ou recuperação conferida. Nunca sobrescreve arquivo existente. */
    public static synchronized void initialize(Path file, long next) {
        if (next < 1) throw new SequenceException("O próximo registro deve ser positivo.");
        locked(file, path -> {
            try (FileChannel channel = FileChannel.open(path, CREATE_NEW, WRITE)) {
                write(channel, next);
            }
            return next;
        });
    }

    /** Backup explícito sob o mesmo lock das reservas. Destino existente não é sobrescrito. */
    public static synchronized void backup(Path file, Path destination) {
        locked(file, path -> {
            long next = read(path);
            try (FileChannel channel = FileChannel.open(destination, CREATE_NEW, WRITE)) {
                write(channel, next);
            }
            return next;
        });
    }

    private interface Operation { long run(Path path) throws IOException; }
    private static long locked(Path file, Operation operation) {
        if (file == null) throw new SequenceException("Caminho da sequência não configurado.");
        Path path = file.toAbsolutePath().normalize();
        try {
            Files.createDirectories(path.getParent());
            // Canonicaliza o diretório para que caminhos equivalentes usem o mesmo lock.
            path = path.getParent().toRealPath().resolve(path.getFileName());
            if (Files.isSymbolicLink(path)) throw new IOException("A sequência não pode ser um link simbólico.");
            Path lockPath = path.resolveSibling(path.getFileName() + ".lock");
            try (FileChannel channel = FileChannel.open(lockPath, CREATE, WRITE);
                 FileLock lock = channel.tryLock()) {
                if (lock == null) throw new IOException("Sequência em uso por outro processo.");
                return operation.run(path);
            }
        } catch (IOException | ArithmeticException | NumberFormatException | OverlappingFileLockException e) {
            logger.error("Falha na sequência {}.", path, e);
            throw new SequenceException("Não foi possível reservar a sequência. Confira o arquivo e o procedimento de recuperação; não reinicie a contagem.", e);
        }
    }

    private static long read(Path file) throws IOException {
        if (!Files.isRegularFile(file) || !Files.isReadable(file) || !Files.isWritable(file))
            throw new IOException("Arquivo de sequência ausente ou inacessível.");
        long next = Long.parseLong(Files.readString(file, StandardCharsets.UTF_8).trim());
        if (next < 1) throw new IOException("Sequência inválida.");
        return next;
    }

    private static void write(FileChannel channel, long next) throws IOException {
        ByteBuffer bytes = StandardCharsets.UTF_8.encode(Long.toString(next));
        while (bytes.hasRemaining()) channel.write(bytes);
        channel.force(true);
    }

    private static void replace(Path file, long next) throws IOException {
        Path temporary = Files.createTempFile(file.getParent(), "sequence-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary, WRITE, TRUNCATE_EXISTING)) { write(channel, next); }
            // Sem fallback não atômico: se o volume não suportar, bloqueia a impressão.
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
    }
}
