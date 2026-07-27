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
        // Leitura dinâmica da porta definida no arquivo de propriedades
        int port = AppConfig.getServerPort();

        // Inicializa os componentes principais do sistema
        var printerService = new PrinterService();
        var printController = new PrintController(printerService);

        Javalin app = Javalin.create(config -> {
            // Configuração de CORS liberada para evitar bloqueios no navegador durante o desenvolvimento
            config.registerPlugin(new CorsPlugin(cors -> {
                cors.addRule(it -> it.anyHost());
            }));

            // ✅ ALTERAÇÃO PRINCIPAL: Mudamos para Location.EXTERNAL lendo direto da pasta do projeto.
            // Isso resolve o erro de "directory does not exist" e permite atualizar o HTML/JS sem reiniciar o Java!
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/web";
                staticFiles.directory = "src/main/resources/web";
                staticFiles.location = Location.EXTERNAL;
            });
        }).start(port);

        logger.info("Servidor iniciado com sucesso na porta {}", port);
        logger.info("Acesse http://localhost:{}/web/index.html para usar a aplicação.", port);

        // Mapeamento das rotas da nossa API de impressão
        app.post("/print", printController::handlePrintRequest);
        app.post("/print-validade", printController::handleValidadePrintRequest);
        app.post("/print-consumo-imediato", printController::handleImmediateConsumptionRequest);
        app.get("/", ctx -> ctx.result("Servidor de impressão Espaço Vista está no ar!"));
    }
}
