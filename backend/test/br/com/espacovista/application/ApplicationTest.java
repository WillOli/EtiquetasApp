package application;

import io.javalin.Javalin;
import org.junit.jupiter.api.*;
import service.PrinterService;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationTest {
    private Javalin app;
    private PrinterService printer;
    private final HttpClient client = HttpClient.newHttpClient();
    @BeforeEach void start() {
        printer = mock(PrinterService.class);
        app = Main.createApp(printer).start("127.0.0.1", 0);
    }
    @AfterEach void stop() { app.stop(); }
    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + app.port() + path));
    }
    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void servesFrontendFromClasspathAndUsesRelativeApi() throws Exception {
        assertEquals(200, send(request("/web/index.html")).statusCode());
        var script = send(request("/web/script/main.js"));
        assertEquals(200, script.statusCode()); assertFalse(script.body().contains("http://localhost:8081"));
        assertEquals(200, send(request("/web/css/style.css")).statusCode());
    }
    @Test void blocksOtherOriginsAndSimpleFormPosts() throws Exception {
        var cross = send(request("/print").header("Origin", "https://untrusted.example")
            .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{}")));
        assertEquals(403, cross.statusCode()); assertTrue(cross.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
        assertEquals(415, send(request("/print").header("Content-Type", "text/plain")
            .POST(HttpRequest.BodyPublishers.ofString("{}"))).statusCode());
        verifyNoInteractions(printer);
    }
    @Test void sameOriginGetsValidationAndOversizeBodyIsRejected() throws Exception {
        assertEquals(400, send(request("/print").header("Origin", "http://127.0.0.1:" + app.port())
            .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{}"))).statusCode());
        assertEquals(413, send(request("/print").header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("x".repeat(20_000)))).statusCode());
        verifyNoInteractions(printer);
    }
}
