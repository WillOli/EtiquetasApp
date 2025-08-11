package br.com.espacovista.controller;

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
        // --- CORREÇÃO APLICADA AQUI ---
        // Cria o objeto usando o construtor padrão e os setters.
        PrintRequest payload = new PrintRequest();
        payload.setText("TESTE-SIMPLES");
        payload.setQuantity(1);
        payload.setLabelType("STANDARD");

        String requestJson = gson.toJson(payload);
        int serverPort = app.port();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverPort + "/print"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        ArgumentCaptor<PrintRequest> captor = ArgumentCaptor.forClass(PrintRequest.class);
        verify(mockPrinterService).printLabels(captor.capture());
        assertEquals("TESTE-SIMPLES", captor.getValue().getText());
    }

    @Test
    @DisplayName("POST /print-validade deve retornar 200 OK e chamar o serviço com os dados corretos")
    void postToPrintValidade_withValidRequest_shouldReturn200() throws IOException, InterruptedException {
        // Arrange
        ValidadePrintRequest payload = new ValidadePrintRequest();
        payload.setProductName("Bolo de Teste");
        payload.setMfgDate("2025-08-04");
        payload.setValidityDays(5);
        payload.setQuantity(2);
        payload.setLabelType(PrintRequest.LabelType.SIXTY_TWO_MM);

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

        // Valida os dados do objeto que foi passado para o serviço.
        ValidadePrintRequest capturedRequest = captor.getValue();
        assertEquals("Bolo de Teste", capturedRequest.getProductName());
        assertEquals(5, capturedRequest.getValidityDays());
        assertEquals(PrintRequest.LabelType.SIXTY_TWO_MM, capturedRequest.getLabelType());
    }
}
