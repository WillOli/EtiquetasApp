package application;

import config.AppConfig;
import controller.PrintController;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPlugin;
import service.PrinterService;

public class Main {

    public static void main(String[] args) {
        // 1. Criar as instâncias
        var printerService = new PrinterService();
        var printController = new PrintController(printerService);
        int port = AppConfig.getServerPort();

        // 2. Iniciar o servidor Javalin com a configuração para v6
        Javalin app = Javalin.create(config -> {
            // Sintaxe de CORS para Javalin v5/v6
            config.registerPlugin(new CorsPlugin(cors -> {
                cors.addRule(it -> it.anyHost());
            }));

            // Sintaxe de Arquivos Estáticos para Javalin v5/v6
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/web";
                staticFiles.directory = "/web";
                staticFiles.location = Location.CLASSPATH;
            });

        }).start(port);

        System.out.println("Servidor iniciado na porta " + port);
        System.out.println("Acesse http://localhost:" + port + "/web/index.html para usar a aplicação.");

        // 3. Registrar as rotas
        app.post("/print", printController::handlePrintRequest);
        app.get("/", ctx -> ctx.result("Servidor de impressão Espaço Vista está no ar!"));
    }
}