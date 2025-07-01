package application;

import controller.PrintController;

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
                    clientSocket.setSoTimeout(10000); // TIMEOUT DE 10 SEGUNDOS
                    new Thread(() -> {
                        try {
                            PrintController controller = new PrintController();
                            controller.handleClient(clientSocket);
                        } catch (IOException e) {
                            System.err.println("Erro no cliente: " + e.getMessage());
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                                System.err.println("Erro ao fechar conexão: " + e.getMessage());
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }
}