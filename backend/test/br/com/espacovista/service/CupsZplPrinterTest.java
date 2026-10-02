package service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class CupsZplPrinterTest {
    @TempDir Path directory;

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void preservesZplBytesAndUsesRawWithoutShellExpansion() throws Exception {
        Path capture = directory.resolve("capture");
        Path script = directory.resolve("lp");
        Files.writeString(script, "#!/bin/sh\n" +
                "printf '%s\\n' \"$@\" > '" + capture + ".args'\n" +
                "cat \"$7\" > '" + capture + ".zpl'\n" +
                "printf 'job-123\\n'\n");
        assertTrue(script.toFile().setExecutable(true));
        String zpl = "^XA^CI28^FDProdução café^FS^PQ1^XZ\n";
        String printer = "Zebra $(touch unwanted)";
        assertEquals("job-123", new CupsZplPrinter(script.toString()).print(printer, zpl));
        var arguments = Files.readAllLines(Path.of(capture + ".args"));
        assertEquals(printer, arguments.get(1));
        assertEquals("-o", arguments.get(2));
        assertEquals("raw", arguments.get(3));
        assertArrayEquals(zpl.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                Files.readAllBytes(Path.of(capture + ".zpl")));
        assertFalse(Files.exists(Path.of(arguments.get(6))), "Documento temporário deve ser removido");
    }

    @Test
    void usesInternalQueueNameInsteadOfMacDisplayName() {
        javax.print.PrintService printer = org.mockito.Mockito.mock(javax.print.PrintService.class);
        org.mockito.Mockito.when(printer.getName()).thenReturn("Zebra Technologies ZD230 ZPL");
        org.mockito.Mockito.when(printer.getAttribute(javax.print.attribute.standard.PrinterName.class))
                .thenReturn(new javax.print.attribute.standard.PrinterName("Zebra_Technologies_ZD230_ZPL", null));
        assertEquals("Zebra_Technologies_ZD230_ZPL", PrinterService.cupsQueueName(printer));
    }

    @Test
    void rejectsMissingQueueName() {
        javax.print.PrintService printer = org.mockito.Mockito.mock(javax.print.PrintService.class);
        assertThrows(PrinterService.PrinterServiceException.class,
                () -> PrinterService.cupsQueueName(printer));
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void reportsCupsRejectionInsteadOfSuccess() throws Exception {
        Path script = directory.resolve("lp-failure");
        Files.writeString(script, "#!/bin/sh\nprintf 'unknown printer\\n' >&2\nexit 1\n");
        assertTrue(script.toFile().setExecutable(true));
        IOException error = assertThrows(IOException.class,
                () -> new CupsZplPrinter(script.toString()).print("missing", "^XA^XZ"));
        assertTrue(error.getMessage().contains("unknown printer"));
    }
    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void timesOutWithoutReportingSuccess() throws Exception {
        Path script = directory.resolve("lp-timeout");
        Files.writeString(script, "#!/bin/sh\nexec sleep 10\n");
        assertTrue(script.toFile().setExecutable(true));
        IOException error = assertThrows(IOException.class,
            () -> new CupsZplPrinter(script.toString(), 100).print("Zebra", "^XA^XZ"));
        assertTrue(error.getMessage().contains("Tempo limite"));
    }
}
