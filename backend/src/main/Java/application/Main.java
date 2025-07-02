package application;

import config.AppConfig;
import controller.PrintController;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPlugin;

public class Main {

    public static void main(String[] args) {
        int port = AppConfig.getServerPort();

        Javalin app = Javalin.create(config -> {
            // --- SINTAXE CORRETA E UNIFICADA PARA JAVALIN v5 ---

            // 1. Configuração de CORS
            config.registerPlugin(new CorsPlugin(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            }));

            // 2. Configuração para servir arquivos estáticos
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/web";       // O caminho no navegador
                staticFiles.directory = "/web";        // A pasta dentro de 'resources'
                staticFiles.location = Location.CLASSPATH;
            });

        }).start(port);

        System.out.println("Servidor iniciado na porta " + port);
        System.out.println("Acesse http://localhost:" + port + "/web/index.html para usar a aplicação.");

        // Define as rotas da API
        app.post("/print", PrintController::handlePrintRequest);
        app.get("/", ctx -> ctx.result("Servidor de impressão Espaço Vista está no ar!"));
    }
}