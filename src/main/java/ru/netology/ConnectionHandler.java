package ru.netology;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConnectionHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private Request request;

    public static final String GET = "GET";
    public static final String POST = "POST";

    public ConnectionHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

    }

    @Override
    public void run() {
        final var allowedMethods = List.of(GET, POST);
        try (

                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            // лимит на request line + заголовки
            final var limit = 4096;
            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                socket.close();
            }

            // читаем request line
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                socket.close();
                return;
            }

            final var method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
                socket.close();
                return;
            }

            final var path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                socket.close();
                return;

            }

            final var version = requestLine[2];

            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                socket.close();
                return;
            }

            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

            // для GET тела нет
            String body = "";
            if (!method.equals(GET)) {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);
                    body = new String(bodyBytes);
                    request = new Request(method, path, version, headers, body);
                    System.out.println(request.requstToString());
                    System.out.println(request.getParams());
                    System.out.println(request.getQueryParam("login"));
                    System.out.println(request.getQueryParam("password") + "\n");
                }
            } else {
                request = new Request(method, path, version, headers, body);
                System.out.println(request.requstToString());
            }
            Handler handler = server.getHandler(request.getMethod(), request.getPath());
            if (handler == null) {
                methodNotImplemented(out);
                socket.close();
                return;
            }
            handler.handle(request, out);
            socket.close();
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((  //метод для записи одиночных байтов или массива байтов (отвечают за вывод данных).
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static void methodNotImplemented(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 501 Not Implemented\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}


