package br.com.espacovista.controller;

import com.google.gson.JsonObject;
import controller.PrintController;
import io.javalin.Javalin;
import model.*;
import org.junit.jupiter.api.*;
import service.PrinterService;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PrintValidationIntegrationTest {
    private static Javalin app;
    private static PrinterService printer;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String[] ROUTES = {
        "/print", "/print-validade", "/print-consumo-imediato", "/print-producao"
    };

    @BeforeAll static void start() {
        printer = mock(PrinterService.class);
        PrintController controller = new PrintController(printer);
        app = Javalin.create()
            .post(ROUTES[0], controller::handlePrintRequest)
            .post(ROUTES[1], controller::handleValidadePrintRequest)
            .post(ROUTES[2], controller::handleImmediateConsumptionRequest)
            .post(ROUTES[3], controller::handleProductionRequest).start(0);
    }

    @AfterAll static void stop() { if (app != null) app.stop(); }

    private JsonObject valid() {
        JsonObject json = new JsonObject();
        json.addProperty("text", "  Pão de açúcar  ");
        json.addProperty("productName", "  Pão de açúcar  ");
        json.addProperty("setor", "  Cozinha  ");
        for (String key : List.of("dataFabricacao", "dataAbertura", "dataValidade", "validade", "dataPreparacao")) {
            json.addProperty(key, "2026-10-02");
        }
        json.addProperty("horarioPreparo", "08:30");
        json.addProperty("horarioDescarte", "12:30");
        json.addProperty("quantity", 1);
        json.addProperty("labelType", "SIXTY_TWO_MM");
        return json;
    }

    private HttpResponse<String> post(String route, String body) throws Exception {
        return CLIENT.send(HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + route))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private void rejected(String route, String body) throws Exception {
        reset(printer);
        HttpResponse<String> response = post(route, body);
        assertEquals(400, response.statusCode(), route + " " + body + " -> " + response.body());
        assertFalse(response.body().isBlank());
        verifyNoInteractions(printer);
    }

    @TestFactory Stream<DynamicTest> commonInvalidRequests() {
        List<DynamicTest> tests = new ArrayList<>();
        for (String route : ROUTES) {
            for (String body : List.of("", "null", "{}", "[]", "{invalid")) {
                tests.add(DynamicTest.dynamicTest(route + " body " + body, () -> rejected(route, body)));
            }
            for (int quantity : new int[]{-1, 0, 51, Integer.MAX_VALUE}) {
                tests.add(DynamicTest.dynamicTest(route + " quantity " + quantity, () -> {
                    JsonObject json = valid(); json.addProperty("quantity", quantity); rejected(route, json.toString());
                }));
            }
            for (String type : Arrays.asList(null, "", "INVALID")) {
                tests.add(DynamicTest.dynamicTest(route + " labelType " + type, () -> {
                    JsonObject json = valid(); json.addProperty("labelType", type); rejected(route, json.toString());
                }));
            }
            for (String field : List.of("quantity", "labelType", route.equals("/print") ? "text" : "productName")) {
                tests.add(DynamicTest.dynamicTest(route + " missing " + field, () -> {
                    JsonObject json = valid(); json.remove(field); rejected(route, json.toString());
                }));
            }
            tests.add(DynamicTest.dynamicTest(route + " blank product", () -> {
                JsonObject json = valid(); json.addProperty(route.equals("/print") ? "text" : "productName", " \t ");
                rejected(route, json.toString());
            }));
        }
        return tests.stream();
    }

    @TestFactory Stream<DynamicTest> invalidDatesAndTimes() {
        Map<String, List<String>> fields = Map.of(
            ROUTES[0], List.of("dataFabricacao", "dataValidade"),
            ROUTES[1], List.of("dataFabricacao", "dataAbertura", "dataValidade"),
            ROUTES[2], List.of("dataFabricacao", "validade"),
            ROUTES[3], List.of("dataPreparacao", "dataValidade", "horarioPreparo", "horarioDescarte"));
        List<DynamicTest> tests = new ArrayList<>();
        fields.forEach((route, names) -> names.forEach(field -> {
            List<String> values = field.startsWith("horario") ? Arrays.asList(null, "", "24:00", "10:99")
                : Arrays.asList(null, "", "2026-02-30", "31/04/2026", "02/10/26");
            for (String value : values) tests.add(DynamicTest.dynamicTest(route + " " + field + "=" + value, () -> {
                JsonObject json = valid(); json.addProperty(field, value); rejected(route, json.toString());
            }));
        }));
        return tests.stream();
    }

    @Test void chronologyAndProductionLayout() throws Exception {
        for (String route : ROUTES) {
            JsonObject json = valid();
            json.addProperty(route.equals(ROUTES[2]) ? "validade" : "dataValidade", "2026-10-01");
            rejected(route, json.toString());
        }
        JsonObject json = valid(); json.addProperty("dataAbertura", "2026-10-01");
        rejected(ROUTES[1], json.toString());
        json = valid(); json.addProperty("labelType", "STANDARD"); rejected(ROUTES[3], json.toString());
        json = valid(); json.addProperty("setor", " "); rejected(ROUTES[0], json.toString());
    }

    @Test void acceptsBoundariesAndTrimsText() throws Exception {
        for (String route : ROUTES) for (int quantity : new int[]{1, 50}) {
            reset(printer);
            JsonObject json = valid(); json.addProperty("quantity", quantity);
            assertEquals(200, post(route, json.toString()).statusCode());
            switch (route) {
                case "/print": verify(printer).printLabels(argThat(r -> r.getText().equals("Pão de açúcar") && r.getSetor().equals("Cozinha"))); break;
                case "/print-validade": verify(printer).printValidadeLabel(argThat(r -> r.getProductName().equals("Pão de açúcar"))); break;
                case "/print-consumo-imediato": verify(printer).printImmediateConsumptionLabel(argThat(r -> r.getProductName().equals("Pão de açúcar"))); break;
                default: verify(printer).printProductionLabel(argThat(r -> r.getProductName().equals("Pão de açúcar")));
            }
            verifyNoMoreInteractions(printer);
        }
    }

    @Test void acceptsBrazilianDatesAndLeapDay() throws Exception {
        for (String route : ROUTES) {
            reset(printer); JsonObject json = valid();
            for (String field : List.of("dataFabricacao", "dataAbertura", "dataValidade", "validade", "dataPreparacao"))
                json.addProperty(field, " 29/02/2024 ");
            assertEquals(200, post(route, json.toString()).statusCode());
        }
    }

    @Test void internalErrorsDoNotLeakDetails() throws Exception {
        for (String route : ROUTES) {
            reset(printer);
            RuntimeException failure = new RuntimeException("PRIVATE-STACKTRACE-PATH");
            doThrow(failure).when(printer).printLabels(any());
            doThrow(failure).when(printer).printValidadeLabel(any());
            doThrow(failure).when(printer).printImmediateConsumptionLabel(any());
            doThrow(failure).when(printer).printProductionLabel(any());
            HttpResponse<String> response = post(route, valid().toString());
            assertEquals(500, response.statusCode());
            assertFalse(response.body().contains("PRIVATE"));
        }
    }
    @Test void printerAndSequenceFailuresAreActionableWithoutTechnicalDetails() throws Exception {
        reset(printer);
        doThrow(new PrinterService.PrinterServiceException("A impressora configurada não foi encontrada.", new RuntimeException("PRIVATE")))
            .when(printer).printProductionLabel(any());
        HttpResponse<String> unavailable = post("/print-producao", valid().toString());
        assertEquals(503, unavailable.statusCode());
        assertTrue(unavailable.body().contains("não foi encontrada")); assertFalse(unavailable.body().contains("PRIVATE"));
        reset(printer);
        doThrow(new service.SequenceException("PRIVATE path" )).when(printer).printLabels(any());
        HttpResponse<String> sequence = post("/print", valid().toString());
        assertEquals(503, sequence.statusCode()); assertFalse(sequence.body().contains("PRIVATE"));
    }

    @Test void allEndpointsRejectOversizeNamesAndFractionalQuantities() throws Exception {
        for (String route : ROUTES) {
            JsonObject json = valid(); json.addProperty(route.equals("/print") ? "text" : "productName", "a".repeat(81));
            rejected(route, json.toString());
            json = valid(); json.addProperty("quantity", 1.5); rejected(route, json.toString());
        }
    }
}
