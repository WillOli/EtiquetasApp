package espacovista.controller;

import com.google.gson.Gson;
import controller.PrintController;
import io.javalin.Javalin;
import model.PrintRequest;
import model.ValidadePrintRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

@ExtendWith(MockitoExtension.class)
class PrintControllerIntegrationTest {

    private static Javalin app;
    private static HttpClient httpClient;
    private final Gson gson = new Gson();

    @Mock
    private PrinterService mockPrinterService;

    @BeforeEach
    void setUp() {
        PrintController printController = new PrintController(mockPrinterService);
        // Usar a porta 8081 para ser consistente com a aplicação real
        app = Javalin.create()
                .post("/print", printController::handlePrintRequest)
                .post("/print-validade", printController::handleValidadePrintRequest)
                .start(0);

        httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    @DisplayName("POST /print deve retornar 200 OK para um pedido de Etiqueta Simples válido")
    void postToPrint_withValidRequest_shouldReturn200() throws IOException, InterruptedException {
        // Arrange
        PrintRequest payload = new PrintRequest("TESTE-SIMPLES", 1, "STANDARD");
        String requestJson = gson.toJson(payload);
        int serverPort = app.port();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverPort + "/print"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        // Act
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(200, response.statusCode());
        assertEquals("Impressão enviada com sucesso!", response.body());

        // ===================================================================
        // =====                  MUDANÇA PRINCIPAL                    =====
        // ===================================================================
        // Usamos um ArgumentCaptor para "capturar" o objeto que foi passado para o mock.
        // Esta é a forma correta de verificar objetos em testes.
        ArgumentCaptor<PrintRequest> printRequestCaptor = ArgumentCaptor.forClass(PrintRequest.class);

        // Verificamos se o controller chamou o método 'printLabels' com QUALQUER objeto PrintRequest.
        verify(mockPrinterService).printLabels(printRequestCaptor.capture());

        // Agora, pegamos o objeto que foi capturado e verificamos seus valores internos.
        PrintRequest capturedRequest = printRequestCaptor.getValue();
        assertEquals("TESTE-SIMPLES", capturedRequest.getText());
        assertEquals(1, capturedRequest.getQuantity());
        assertEquals(PrintRequest.LabelType.STANDARD, capturedRequest.getLabelType());
    }

    @Test
    @DisplayName("POST /print-validade deve retornar 200 OK para um pedido de Etiqueta de Validade válido")
    void postToPrintValidade_withValidRequest_shouldReturn200() throws IOException, InterruptedException {
        // Arrange
        ValidadePrintRequest payload = new ValidadePrintRequest();
        // (Em um teste real, você setaria os valores aqui. Para este exemplo, está ok)
        String requestJson = gson.toJson(payload);
        int serverPort = app.port();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverPort + "/print-validade"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        // Act
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(200, response.statusCode());
        assertEquals("Etiqueta de validade enviada com sucesso!", response.body());

        // Verify
        ArgumentCaptor<ValidadePrintRequest> captor = ArgumentCaptor.forClass(ValidadePrintRequest.class);
        verify(mockPrinterService).printValidadeLabel(captor.capture());
    }
}