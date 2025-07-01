package controller;

import model.PrintRequest;
import service.PrinterService;
import view.HttpResponseView;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class PrintController {

    public void handleClient(Socket socket) throws IOException {
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
                HttpResponseView.sendOptionsResponse(out);
                return;
            }

            char[] body = new char[contentLength];
            in.read(body);
            String requestBody = new String(body);
            System.out.println("[LOG] JSON recebido: " + requestBody);

            String responseMessage;
            try {
                JSONObject json = new JSONObject(requestBody);
                // Adiciona a leitura do tipo de etiqueta, com "standard" como padrão
                String labelTypeString = json.optString("labelType", "standard");

                PrintRequest printRequest = new PrintRequest(json.getString("text"), json.getInt("quantity"), labelTypeString);
                PrinterService printerService = new PrinterService();
                // Passa o tipo de etiqueta para o serviço de impressão
                printerService.printLabels(printRequest.getText(), printRequest.getQuantity(), printRequest.getLabelType());
                responseMessage = "Etiqueta enviada para impressão.";
            } catch (Exception e) {
                System.err.println("[ERRO] Falha ao interpretar JSON ou imprimir: " + e.getMessage());
                e.printStackTrace();
                responseMessage = "Erro: JSON inválido ou falha na impressão.";
            }

            HttpResponseView.sendResponse(out, responseMessage);
        }
    }
}