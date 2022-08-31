package ru.netology;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final Map<String, Map<String, Handler>> handlersMap = new ConcurrentHashMap<>();
    final ExecutorService threadPool = Executors.newFixedThreadPool(64);

    public void start(int port) {

        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.submit(new ConnectionHandler(socket, this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        Map<String, Handler> pathMap = handlersMap.get(method);
        if (null == pathMap) {
            pathMap = new ConcurrentHashMap<>();
            pathMap.put(path, handler);
            handlersMap.put(method, pathMap);
            return;
        }
        pathMap.putIfAbsent(path, handler);
    }

    public Handler getHandler(String method, String path) {
        Map<String, Handler> pathMap = handlersMap.get(method);
        if (null == pathMap) return null;
        return pathMap.get(path);
    }

}
