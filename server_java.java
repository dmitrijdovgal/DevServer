// server_java.java — локальный HTTP-сервер (разработка) на Java

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DevServer {
    private static String root = ".";
    private static int port = 8080;

    public static void main(String[] args) throws IOException {
        // Парсинг аргументов
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("--port") && i+1 < args.length) port = Integer.parseInt(args[++i]);
            else if (args[i].equals("--root") && i+1 < args.length) root = args[++i];
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        // Статика
        server.createContext("/", new StaticHandler());
        // API
        server.createContext("/api/users", new ApiHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("🌐 DevServer запущен на http://localhost:" + port);
        System.out.println("Корневая папка: " + root);
        System.out.println("Нажмите Ctrl+C для остановки");
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            String filePath = Paths.get(root, path).toString();
            try {
                byte[] data = Files.readAllBytes(Paths.get(filePath));
                String mime = Files.probeContentType(Paths.get(filePath));
                if (mime == null) mime = "application/octet-stream";
                exchange.getResponseHeaders().set("Content-Type", mime);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, data.length);
                exchange.getResponseBody().write(data);
            } catch (NoSuchFileException e) {
                String resp = "404 Not Found";
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(404, resp.length());
                exchange.getResponseBody().write(resp.getBytes());
            } catch (Exception e) {
                String resp = "500 Internal Server Error";
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(500, resp.length());
                exchange.getResponseBody().write(resp.getBytes());
            } finally {
                exchange.getResponseBody().close();
                log(exchange, exchange.getResponseCode());
            }
        }
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String resp;
            int status;
            if (method.equals("GET")) {
                resp = "[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]";
                status = 200;
            } else if (method.equals("POST")) {
                resp = "{\"status\":\"created\"}";
                status = 201;
            } else if (method.equals("PUT")) {
                resp = "{\"status\":\"updated\"}";
                status = 200;
            } else if (method.equals("DELETE")) {
                resp = "";
                status = 204;
            } else {
                resp = "Method Not Allowed";
                status = 405;
            }
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.sendResponseHeaders(status, resp.length());
            exchange.getResponseBody().write(resp.getBytes());
            exchange.getResponseBody().close();
            log(exchange, status);
        }
    }

    private static void log(HttpExchange exchange, int status) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.printf("[%s] %s %s -> %d%n", timestamp, exchange.getRequestMethod(),
                          exchange.getRequestURI().getPath(), status);
    }
}
