package controller;

import com.google.gson.Gson;
import io.javalin.Javalin;
import model.PrintRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.PrinterService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Habilita a integração do Mockito com o JUnit 5
class PrintControllerIntegrationTest {

    private static Javalin app;
    private static HttpClient httpClient;
    private final Gson gson = new Gson();

    @Mock
    private PrinterService mockPrinterService; // Cria um "mock" (versão falsa) do nosso serviço

    // @BeforeEach roda antes de cada teste. Aqui vamos iniciar um servidor novo para cada teste.
    @BeforeEach
    void setUp() {
        // Cria uma instância do controller INJETANDO o serviço FALSO (mock)
        PrintController printController = new PrintController(mockPrinterService);

        // Iniciamos um servidor real, mas que usa o nosso controller com o mock
        app = Javalin.create()
                .post("/print", printController::handlePrintRequest)
                .start(0); // Iniciar numa porta aleatória livre para evitar conflitos

        httpClient = HttpClient.newHttpClient();
    }

    // @AfterEach roda depois de cada teste para limpar o ambiente.
    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    @DisplayName("POST /print deve retornar 200 OK e mensagem de sucesso para um pedido válido")
    void postToPrint_withValidRequest_shouldReturn200() throws IOException, InterruptedException {
        // Arrange (Preparação)
        PrintRequest payload = new PrintRequest("TESTE-INTEGRACAO", 5, "STANDARD");
        String requestJson = gson.toJson(payload);
        int serverPort = app.port(); // Pegamos a porta aleatória em que o servidor iniciou

        // Criamos uma requisição HTTP real para o nosso servidor de teste
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverPort + "/print"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        // Act (Ação)
        // Enviamos a requisição e recebemos a resposta
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert (Verificação da Resposta)
        // Verificamos se a resposta HTTP do servidor está correta
        assertEquals(200, response.statusCode());
        assertEquals("Impressão enviada com sucesso!", response.body());

        // Verify (Verificação da Interação)
        // Verificamos se o nosso controller realmente chamou o método 'printLabels'
        // do nosso serviço FALSO, com os dados que esperamos.
        verify(mockPrinterService)
                .printLabels("TESTE-INTEGRACAO", 5, PrintRequest.LabelType.STANDARD);
    }
}