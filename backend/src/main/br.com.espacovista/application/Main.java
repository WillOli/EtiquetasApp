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
        int port = AppConfig.getServerPort();
        var printerService = new PrinterService();
        var printController = new PrintController(printerService);

        Javalin app = Javalin.create(config -> {
            // --- CONFIGURAÇÃO DE CORS SEGURA PARA PRODUÇÃO ---
            config.registerPlugin(new CorsPlugin(cors -> {
                cors.addRule(it -> {
                    // Adicione aqui o(s) endereço(s) EXATO(s) de onde o seu frontend será acessado.
                    // Durante o desenvolvimento, pode ser o endereço do Live Server da sua IDE.
                    it.allowHost("http://localhost:5500");
                    it.allowHost("http://127.0.0.1:5500");

                    // QUANDO FOR PARA PRODUÇÃO, adicione o domínio real do seu site.
                    // Exemplo: it.allowHost("https://www.seusite.com.br");

                    // Se você não tiver certeza da porta, pode remover uma das linhas acima ou
                    // adicionar outras conforme necessário.
                });
            }));

            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/web";
                staticFiles.directory = "/web";
                staticFiles.location = Location.CLASSPATH;
            });
        }).start(port);

        logger.info("Servidor (versão de produção) iniciado na porta {}", port);
        logger.info("Acesse http://localhost:{}/web/index.html para usar a aplicação.", port);

        app.post("/print", printController::handlePrintRequest);
        app.post("/print-validade", printController::handleValidadePrintRequest);
        app.get("/", ctx -> ctx.result("Servidor de impressão Espaço Vista está no ar!"));
    }
}
