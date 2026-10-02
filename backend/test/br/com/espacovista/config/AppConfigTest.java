package config;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {
    @Test void systemPropertiesOverrideBundledConfiguration() {
        String old = System.getProperty("etiquetas.server.port");
        try {
            System.setProperty("etiquetas.server.port", "9099"); assertEquals(9099, AppConfig.getServerPort());
            for (String invalid : new String[]{"abc", "0", "65536", "-1"}) {
                System.setProperty("etiquetas.server.port", invalid);
                assertThrows(IllegalArgumentException.class, AppConfig::getServerPort);
            }
        } finally { if (old == null) System.clearProperty("etiquetas.server.port"); else System.setProperty("etiquetas.server.port", old); }
    }
    @Test void sequencePathIsExplicitAndAbsolute() {
        String old = System.getProperty("etiquetas.sequence.file");
        try {
            System.setProperty("etiquetas.sequence.file", "test-data/sequence.txt");
            assertEquals(Path.of("test-data/sequence.txt").toAbsolutePath(), AppConfig.getSequenceFilePath());
        } finally { if (old == null) System.clearProperty("etiquetas.sequence.file"); else System.setProperty("etiquetas.sequence.file", old); }
    }
}
