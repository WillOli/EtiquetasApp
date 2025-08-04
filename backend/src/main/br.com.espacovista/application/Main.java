package application;

import config.AppConfig;
import controller.PrintController;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.PrinterService;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var printerService = new PrinterService();
        var printController = new PrintController(printerService);
        int port = AppConfig.getServerPort();

        Javalin app = Javalin.create(config -> {
            // --- ALTERAÇÃO DE SEGURANÇA NO CORS ---
            config.registerPlugin(new CorsPlugin(cors -> {
                // Em vez de permitir qualquer host (anyHost), restringimos a origens específicas.
                // Isso garante que apenas o seu frontend possa fazer requisições para esta API.
                cors.addRule(it -> {
                    // Adicione aqui o endereço onde seu frontend está rodando durante o desenvolvimento.
                    // Exemplos comuns: http://localhost:5500 (Live Server), http://localhost:3000 (React), http://127.0.0.1:5500
                    it.allowHost("http://localhost:5500");
                    it.allowHost("http://127.0.0.1:5500");


                    // Em produção, você adicionaria o domínio real do seu site.
                    // ex: it.allowHost("https://www.seusite.com.br");

                    // Permite que o frontend envie os cabeçalhos necessários (opcional, mas bom ter).
                    it.allowCredentials = true;
                });
            }));
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/web";
                staticFiles.directory = "/web";
                staticFiles.location = Location.CLASSPATH;
            });
        }).start(port);

        logger.info("Servidor iniciado na porta {}", port);
        logger.info("Acesse http://localhost:{}/web/index.html para usar a aplicação.", port);

        app.post("/print", printController::handlePrintRequest);
        app.post("/print-validade", printController::handleValidadePrintRequest);
        app.get("/", ctx -> ctx.result("Servidor de impressão Espaço Vista está no ar!"));
    }
}
