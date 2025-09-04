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
        // Reintroduzimos o AppConfig para ler a porta do arquivo de propriedades.
        // Isso mantém o código flexível.
        int port = AppConfig.getServerPort();

        // Inicializa os componentes principais.
        var printerService = new PrinterService();
        var printController = new PrintController(printerService);

        Javalin app = Javalin.create(config -> {
            // Mantemos a configuração de CORS mais simples (anyHost) por enquanto,
            // pois sabemos que ela funciona e não causa problemas durante o desenvolvimento.
            config.registerPlugin(new CorsPlugin(cors -> {
                cors.addRule(it -> it.anyHost());
            }));

            // A configuração dos arquivos estáticos, que está correta, é mantida.
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/web";
                staticFiles.directory = "/web";
                staticFiles.location = Location.CLASSPATH;
            });
        }).start(port);

        logger.info("Servidor iniciado com sucesso na porta {}", port);
        logger.info("Acesse http://localhost:{}/web/index.html para usar a aplicação.", port);

        // As rotas da API continuam as mesmas.
        app.post("/print", printController::handlePrintRequest);
        app.post("/print-validade", printController::handleValidadePrintRequest);
        app.post("/print-consumo-imediato", printController::handleImmediateConsumptionRequest);
        app.get("/", ctx -> ctx.result("Servidor de impressão Espaço Vista está no ar!"));
    }
}
