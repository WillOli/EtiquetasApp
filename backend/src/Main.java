import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        int port = 8080;
        System.out.println("Servidor iniciado na porta " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(10000); // TIMEOUT DE 10 SEGUNDOS
                    new Thread(() -> handleClient(clientSocket)).start();
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            System.out.println("[LOG] " + LocalDateTime.now() + " - Conexão recebida de " + socket.getInetAddress());

            String line;
            StringBuilder header = new StringBuilder();
            int contentLength = 0;
            String method = "GET";

            while (!(line = in.readLine()).isEmpty()) {
                header.append(line).append("\n");
                if (line.toUpperCase().startsWith("CONTENT-LENGTH:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
                if (line.startsWith("POST") || line.startsWith("OPTIONS")) {
                    method = line.split(" ")[0];
                }
            }

            if (method.equals("OPTIONS")) {
                String response = ""
                        + "HTTP/1.1 204 No Content\r\n"
                        + "Access-Control-Allow-Origin: *\r\n"
                        + "Access-Control-Allow-Methods: POST, OPTIONS\r\n"
                        + "Access-Control-Allow-Headers: Content-Type\r\n"
                        + "Access-Control-Max-Age: 86400\r\n"
                        + "\r\n";
                out.write(response);
                out.flush();
                socket.close();
                return;
            }

            char[] body = new char[contentLength];
            in.read(body);
            String requestBody = new String(body);

            System.out.println("[LOG] JSON recebido: " + requestBody);

            String responseMessage = "Etiqueta enviada para impressão.";

            try {
                JSONObject json = new JSONObject(requestBody);
                String text = json.getString("text");
                int quantity = json.getInt("quantity");

                System.out.println("[LOG] Impressão solicitada - Texto: \"" + text + "\", Quantidade: " + quantity);

                PrinterService printer = new PrinterService();
                printer.printLabels(text, quantity);

            } catch (Exception e) {
                System.err.println("[ERRO] Falha ao interpretar JSON ou imprimir: " + e.getMessage());
                e.printStackTrace();

                responseMessage = "Erro: JSON inválido ou falha na impressão."; // << NOVO
            }

            String response = ""
                    + "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "Access-Control-Allow-Origin: *\r\n"
                    + "\r\n"
                    + responseMessage;
            out.write(response);
            out.flush();

        } catch (IOException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}
