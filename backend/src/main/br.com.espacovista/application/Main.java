package application;

import config.AppConfig;
import controller.PrintController;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.PrinterService;
import service.SequenceManager;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.PrinterName;
import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;

public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "--list-printers":
                    for (var printer : PrintServiceLookup.lookupPrintServices(null, null)) {
                        PrinterName internal = printer.getAttribute(PrinterName.class);
                        System.out.println(printer.getName() + (internal == null ? "" : " | " + internal.getValue()));
                    }
                    return;
                case "--initialize-sequence":
                    if (args.length != 2) throw new IllegalArgumentException("Informe o próximo registro conferido.");
                    SequenceManager.initialize(AppConfig.getSequenceFilePath(), Long.parseLong(args[1]));
                    logger.info("Sequência inicializada em {}.", AppConfig.getSequenceFilePath());
                    return;
                case "--backup-sequence":
                    if (args.length != 2) throw new IllegalArgumentException("Informe o caminho do backup novo.");
                    SequenceManager.backup(AppConfig.getSequenceFilePath(), Path.of(args[1]));
                    logger.info("Backup da sequência concluído.");
                    return;
                default: throw new IllegalArgumentException("Opção desconhecida.");
            }
        }
        Javalin app = createApp(new PrinterService()).start("127.0.0.1", AppConfig.getServerPort());
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        logger.info("Abra http://localhost:{}/web/index.html", app.port());
    }

    public static Javalin createApp(PrinterService printer) {
        PrintController controller = new PrintController(printer);
        return Javalin.create(config -> {
            config.http.maxRequestSize = 16_384L;
            config.staticFiles.add(files -> {
                files.hostedPath = "/web";
                files.directory = AppConfig.isDevelopment() ? AppConfig.getWebDirectory() : "/web";
                files.location = AppConfig.isDevelopment() ? Location.EXTERNAL : Location.CLASSPATH;
            });
        }).before(ctx -> {
            if (!ctx.method().name().equals("POST")) return;
            String origin = ctx.header("Origin");
            if (origin != null && !sameOrigin(origin, ctx.header("Host")))
                throw new io.javalin.http.ForbiddenResponse("Origem não permitida.");
            String contentType = ctx.contentType();
            if (contentType == null || !contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT).equals("application/json"))
                throw new io.javalin.http.UnsupportedMediaTypeResponse("Use application/json.");
        }).post("/print", controller::handlePrintRequest)
          .post("/print-validade", controller::handleValidadePrintRequest)
          .post("/print-consumo-imediato", controller::handleImmediateConsumptionRequest)
          .post("/print-producao", controller::handleProductionRequest)
          .get("/", ctx -> ctx.redirect("/web/index.html"));
    }

    private static boolean sameOrigin(String origin, String host) {
        try {
            URI uri = URI.create(origin);
            return "http".equals(uri.getScheme()) && uri.getRawUserInfo() == null
                    && ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost()))
                    && uri.getRawAuthority().equals(host);
        } catch (IllegalArgumentException e) { return false; }
    }
}
