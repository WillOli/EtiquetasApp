import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PrinterServiceTest {

    private final PrinterService printer = new PrinterService();

    @Test
    void testPrintLabels_Valid() {
        assertDoesNotThrow(() -> printer.printLabels("Etiqueta OK", 5));
    }

    @Test
    void testPrintLabels_EmptyText() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> printer.printLabels("", 5));
        assertEquals("Texto de etiqueta não pode ser vazio", ex.getMessage());
    }

    @Test
    void testPrintLabels_InvalidQuantity() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> printer.printLabels("Texto", 0));
        assertEquals("Quantidade deve estar entre 1 e 100", ex.getMessage());
    }
}
