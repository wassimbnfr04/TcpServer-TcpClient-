package com.chat.server.model;

import com.chat.server.config.ServerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class ChatServer {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ServerConfig config;
    private final ChatServerListener listener;
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final AtomicInteger readOnlyCounter = new AtomicInteger(1);
    private final AtomicBoolean online = new AtomicBoolean(false);

    private volatile boolean running;
    private ServerSocket serverSocket;
    private Thread acceptThread;

    public ChatServer(ServerConfig config, ChatServerListener listener) {
        this.config = Objects.requireNonNull(config, "config");
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        acceptThread = new Thread(this::acceptLoop, "server-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public void stop() {
        running = false;
        closeServerSocket();
        for (ClientHandler client : clients) {
            client.close();
        }
        clientPool.shutdownNow();
        updateStatus(false, "Server stopped");
    }

    private void acceptLoop() {
        try (ServerSocket server = new ServerSocket()) {
            serverSocket = server;
            server.bind(new InetSocketAddress(config.getBindHost(), config.getPort()));
            log("Server Started on " + config.getBindHost() + ":" + config.getPort());
            updateStatus(true, "Online");

            while (running) {
                log("Waiting for Client...");
                Socket socket = server.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                clientPool.execute(handler);
            }
        } catch (IOException ex) {
            if (running) {
                log("Server Error: " + ex.getMessage());
                updateStatus(false, "Error");
            }
        } finally {
            updateStatus(false, "Offline");
        }
    }

    private void closeServerSocket() {
        if (serverSocket == null) {
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    private void log(String message) {
        listener.onLog(message);
    }

    private void updateStatus(boolean isOnline, String detail) {
        if (online.getAndSet(isOnline) != isOnline) {
            listener.onStatusChanged(isOnline, detail);
        }
    }

    private void notifyUsersChanged() {
        List<String> users = clients.stream()
                .map(ClientHandler::getUsername)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        listener.onUsersChanged(users);
    }

    private String formatMessage(String username, String message) {
        String time = LocalTime.now().format(TIME_FORMATTER);
        return "[" + time + "] " + username + ": " + message;
    }

    private void broadcast(String message, ClientHandler exclude) {
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.send(message);
            }
        }
    }

    private void broadcastSystem(String message, ClientHandler exclude) {
        broadcast("Server: " + message, exclude);
    }

    private final class ClientHandler implements Runnable {
        private final Socket socket;
        private final Object sendLock = new Object();
        private BufferedReader reader;
        private PrintWriter writer;
        private String username;
        private boolean readOnly;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Socket ignored = socket) {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                String joinLine = reader.readLine();
                if (joinLine == null) {
                    return;
                }

                String requestedName = extractJoinName(joinLine);
                if (requestedName == null || requestedName.trim().isEmpty()) {
                    readOnly = true;
                    username = "READ_ONLY-" + readOnlyCounter.getAndIncrement();
                } else {
                    username = requestedName.trim();
                }

                log("Welcome " + username);
                notifyUsersChanged();
                send("Server: Welcome " + username + (readOnly ? " (READ-ONLY MODE)" : ""));
                broadcastSystem(username + " joined the chat.", this);

                String line;
                while ((line = reader.readLine()) != null) {
                    String payload = extractPayload(line);
                    String message = payload == null ? "" : payload.trim();
                    if (message.isEmpty()) {
                        continue;
                    }

                    if (isDisconnectCommand(message)) {
                        break;
                    }

                    if (message.equalsIgnoreCase("allUsers")) {
                        sendActiveUsers();
                        continue;
                    }

                    if (readOnly) {
                        send("Server: READ-ONLY MODE - messages are disabled.");
                        continue;
                    }

                    String formatted = formatMessage(username, message);
                    broadcast(formatted, this);
                    log(formatted);
                }
            } catch (IOException ex) {
                if (username != null) {
                    log("Connection error (" + username + "): " + ex.getMessage());
                } else {
                    log("Connection error: " + ex.getMessage());
                }
            } finally {
                disconnect();
            }
        }

        private void disconnect() {
            clients.remove(this);
            notifyUsersChanged();
            if (username != null) {
                log(username + " disconnected");
                broadcastSystem(username + " left the chat.", this);
            }
            close();
        }

        private void sendActiveUsers() {
            String users = clients.stream()
                    .map(ClientHandler::getUsername)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            send("Active users: " + (users.isEmpty() ? "(none)" : users));
        }

        private void send(String message) {
            synchronized (sendLock) {
                if (writer != null) {
                    writer.println(message);
                }
            }
        }

        private void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        private String getUsername() {
            return username;
        }
    }

    private static boolean isDisconnectCommand(String message) {
        String lowered = message.trim().toLowerCase();
        return lowered.equals("bye") || lowered.equals("end");
    }

    private static String extractJoinName(String line) {
        if (line.startsWith("JOIN|")) {
            return line.substring(5);
        }
        return line;
    }

    private static String extractPayload(String line) {
        if (line.startsWith("MSG|")) {
            return line.substring(4);
        }
        if (line.startsWith("CMD|")) {
            return line.substring(4);
        }
        if (line.equalsIgnoreCase("BYE")) {
            return "bye";
        }
        return line;
    }
}
