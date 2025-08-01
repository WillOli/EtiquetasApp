package espacovista.service.strategies;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.strategies.ILabelStrategy;
import service.strategies.SimpleLayoutStrategy;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleLayoutStrategyTest {

    @Test
    @DisplayName("Deve gerar ZPL para Etiqueta Simples Padrão (80x25mm) com comandos essenciais")
    void generateZpl_forSimpleLayout_shouldContainEssentialCommands() {
        // Arrange (Preparação)
        // Cria uma instância da estratégia específica que queremos testar.
        ILabelStrategy strategy = new SimpleLayoutStrategy("TESTE-ZPL", 1);

        // Act (Ação)
        // Executa o método da estratégia.
        String zplResult = strategy.generateZpl();

        // Assert (Verificação)
        // Verifica se o ZPL gerado contém o que esperamos.
        assertNotNull(zplResult, "O resultado do ZPL não deve ser nulo.");
        assertTrue(zplResult.contains("^XA"), "Deve conter o comando de início ^XA.");
        assertTrue(zplResult.contains("^XZ"), "Deve conter o comando de fim ^XZ.");
        assertTrue(zplResult.contains("^FDTESTE-ZPL^FS"), "Deve conter o texto da etiqueta.");
        assertTrue(zplResult.contains("^FB"), "Deve usar o comando de bloco de texto ^FB para centralizar.");
    }
}