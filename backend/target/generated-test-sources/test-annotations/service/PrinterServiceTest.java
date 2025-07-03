package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PrinterServiceTest {

    private PrinterService printerService;

    // A anotação @BeforeEach faz este método rodar ANTES de CADA teste.
    // Isso garante que cada teste comece com um objeto "limp".
    @BeforeEach
    void setUp() {
        printerService = new PrinterService();
    }


    @Test
    void testGenerateStandardLabelZPL_ContainsEssentialCommands() {
        // Arrange (Preparação): Define os dados de entrada para o teste.
        String labelText = "PRODUTO-A";
        int quantity = 2; // 1 par de etiquetas

        // Act (Ação): Executa o método que queremos testar.
        String zplResult = printerService.generate62mmLabelZPL(labelText, quantity);

        // Assert (Verificação): Verifica se o resultado é o esperado.
        assertNotNull(zplResult)

    }
}
