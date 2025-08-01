package application;

import config.AppConfig;
import controller.PrintController;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPlugin;
import service.PrinterService;

public class Main {
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

        System.out.println("Servidor iniciado na porta " + port);
        System.out.println("Acesse http://localhost:" + port + "/web/index.html para usar a aplicação.");

        // ALTERAÇÃO: Adicionada a nova rota
        app.post("/print", printController::handlePrintRequest);
        app.post("/print-validade", printController::handleValidadePrintRequest);
        app.get("/", ctx -> ctx.result("Servidor de impressão Espaço Vista está no ar!"));
    }
}