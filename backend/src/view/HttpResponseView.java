package view;

import java.io.BufferedWriter;
import java.io.IOException;

public class HttpResponseView {

    public static void sendOptionsResponse(BufferedWriter out) throws IOException {
        String response = ""
                + "HTTP/1.1 204 No Content\r\n"
                + "Access-Control-Allow-Origin: *\r\n"
                + "Access-Control-Allow-Methods: POST, OPTIONS\r\n"
                + "Access-Control-Allow-Headers: Content-Type\r\n"
                + "Access-Control-Max-Age: 86400\r\n"
                + "\r\n";
        out.write(response);
        out.flush();
    }

    public static void sendResponse(BufferedWriter out, String message) throws IOException {
        String response = ""
                + "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/plain\r\n"
                + "Access-Control-Allow-Origin: *\r\n"
                + "\r\n"
                + message;
        out.write(response);
        out.flush();
    }
}