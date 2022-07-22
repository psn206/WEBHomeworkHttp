package ru.netology;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    final ExecutorService threadPool = Executors.newFixedThreadPool(64);

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.submit( new ServerRunnable(socket,validPaths));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
