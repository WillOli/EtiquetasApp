package application;

import controller.PrintController;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPlugin;

public class Main {

    public static void main(String[] args) {
        int port = 8080;

        // 1. Cria e configura uma instância do Javalin.
        Javalin app = Javalin.create(config -> {
            // Sintaxe correta e definitiva para o CorsPlugin
            config.registerPlugin(new CorsPlugin(cors -> {
                cors.addRule(it -> {
                    it.anyHost(); // Permite acesso de qualquer origem
                });
            }));
        }).start(port);

        System.out.println("Servidor iniciado na porta " + port);
        System.out.println("Acesse http://localhost:" + port + " no seu navegador para testar.");

        // 2. Define as rotas (endpoints) da sua API.
        app.post("/print", PrintController::handlePrintRequest);
        app.get("/", ctx -> ctx.result("Servidor de impressão Espaço Vista está no ar!"));
    }
}