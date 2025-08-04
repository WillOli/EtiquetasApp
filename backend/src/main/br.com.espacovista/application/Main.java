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
            config.registerPlugin(new CorsPlugin(cors -> {
                cors.addRule(it -> it.anyHost());
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
