package service;

import model.PrintRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;


import static org.junit.jupiter.api.Assertions.*;

class PrinterServiceTest {

    private PrinterService printerService;

    // A anotação @BeforeEach faz este método rodar ANTES de CADA teste.
    // Isso garante que cada teste comece com um objeto "limpo".
    @BeforeEach
    void setUp() {
        printerService = new PrinterService();
    }

    @Test
    @DisplayName("Deve gerar ZPL padrão contendo os comandos essenciais")
    void testGenerateStandardLabelZPL_ContainsEssentialCommands() {
        // Arrange (Preparação): Define os dados de entrada para o teste.
        String labelText = "PRODUTO-A";
        int quantity = 2; // 1 par de etiquetas

        // Act (Ação): Executa o método que queremos testar.
        String zplResult = printerService.generateStandardLabelZPL(labelText, quantity);

        // Assert (Verificação): Verifica se o resultado é o esperado.
        assertNotNull(zplResult, "O resultado do ZPL não deve ser nulo.");
        assertTrue(zplResult.contains("^XA"), "Deve conter o comando de início de etiqueta ^XA.");
        assertTrue(zplResult.contains("^FDPRODUTO-A^FS"), "Deve conter o texto da etiqueta com o comando ^FD.");
        assertTrue(zplResult.contains("^XZ"), "Deve conter o comando de fim de etiqueta ^XZ.");
    }

    @Test
    @DisplayName("Deve gerar ZPL de 62mm usando o comando de bloco de texto ^FB")
    void testGenerate62mmLabelZPL_UsesFieldBlockCommand() {
        // Arrange
        String labelText = "Etiqueta especial de 62mm com texto um pouco mais longo";
        int quantity = 2;

        // Act
        String zplResult = printerService.generate62mmLabelZPL(labelText, quantity);

        // Assert
        assertNotNull(zplResult);
        assertTrue(zplResult.contains("^FB"), "ZPL para 62mm deve usar o comando ^FB para formatação de bloco.");
    }

    @Test
    @DisplayName("Não deve lançar exceção ao receber texto nulo na função pública")
    void printLabels_shouldNotThrowException_whenTextIsNull() {
        // Este teste verifica a robustez do nosso método público.
        // Ele garante que, mesmo com uma entrada inválida, a aplicação não quebra.
        assertDoesNotThrow(() -> {
            // Tentamos chamar o método público com um dado inválido que já tratamos no código.
            printerService.printLabels(null, 1, PrintRequest.LabelType.STANDARD);
        }, "A função printLabels não deveria lançar uma exceção para texto nulo.");
    }
}