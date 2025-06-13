import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        int port = 8080;
        System.out.println("Servidor iniciado na porta " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
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
            String line;
            StringBuilder request = new StringBuilder();
            int contentLength = 0;

            // Lê o cabeçalho HTTP
            while (!(line = in.readLine()).isEmpty()) {
                request.append(line).append("\n");
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            // Lê o corpo da requisição
            char[] body = new char[contentLength];
            in.read(body);
            String requestBody = new String(body);

            System.out.println("\n--- Requisição recebida ---");
            System.out.println(request.toString());
            System.out.println("Corpo:");
            System.out.println(requestBody);

            // Processa JSON
            try {
                JSONObject json = new JSONObject(requestBody);
                String text = json.getString("text");
                int quantity = json.getInt("quantity");

                PrinterService printer = new PrinterService();
                printer.printLabels(text, quantity);

            } catch (Exception e) {
                System.err.println("Erro ao interpretar JSON ou imprimir: " + e.getMessage());
                e.printStackTrace();
            }

            // Envia resposta
            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nEtiqueta enviada para impressão.";
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
