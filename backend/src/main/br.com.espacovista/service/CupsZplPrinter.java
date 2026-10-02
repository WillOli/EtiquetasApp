package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Envia comandos ZPL sem conversão pelos filtros do driver CUPS. */
final class CupsZplPrinter {
    private final String executable;
    private final long timeoutMillis;

    CupsZplPrinter() {
        this("/usr/bin/lp");
    }

    CupsZplPrinter(String executable) {
        this(executable, 30_000);
    }

    CupsZplPrinter(String executable, long timeoutMillis) {
        this.executable = executable;
        this.timeoutMillis = timeoutMillis;
    }

    String print(String printerName, String zpl) throws IOException, InterruptedException {
        Path document = Files.createTempFile("etiquetas-", ".zpl");
        Path output = null;
        Process process = null;
        try {
            output = Files.createTempFile("etiquetas-lp-", ".log");
            Files.writeString(document, zpl, StandardCharsets.UTF_8);
            process = new ProcessBuilder(executable, "-d", printerName, "-o", "raw",
                    "-t", "Espaço Vista - Etiquetas", document.toString())
                    .redirectErrorStream(true)
                    .redirectOutput(output.toFile())
                    .start();
            if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
                throw new IOException("Tempo limite ao enviar ZPL à fila CUPS.");
            }
            String result = Files.readString(output, StandardCharsets.UTF_8).trim();
            if (process.exitValue() != 0) {
                throw new IOException("CUPS recusou a impressão (código "
                        + process.exitValue() + "): " + result);
            }
            return result;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
            Files.deleteIfExists(document);
            if (output != null) Files.deleteIfExists(output);
        }
    }
}
