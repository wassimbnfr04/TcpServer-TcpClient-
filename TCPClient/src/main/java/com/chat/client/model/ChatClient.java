package com.chat.client.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ChatClient {
    private final ChatClientListener listener;
    private final Object sendLock = new Object();
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread ioThread;

    public ChatClient(ChatClientListener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    public void connect(String host, int port, String username) {
        if (connected.get()) {
            return;
        }
        ioThread = new Thread(() -> runConnection(host, port, username), "client-io");
        ioThread.setDaemon(true);
        ioThread.start();
    }

    public void sendMessage(String message) {
        if (!connected.get()) {
            return;
        }
        sendRaw("MSG|" + message);
    }

    public void disconnect() {
        if (!connected.getAndSet(false)) {
            close();
            return;
        }
        sendRaw("BYE");
        close();
        listener.onDisconnected("Disconnected");
    }

    public boolean isConnected() {
        return connected.get();
    }

    private void runConnection(String host, int port, String username) {
        boolean readOnly = username == null || username.trim().isEmpty();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            sendRaw("JOIN|" + (username == null ? "" : username.trim()));
            connected.set(true);
            listener.onConnected(readOnly);

            String line;
            while ((line = reader.readLine()) != null) {
                listener.onMessage(line);
            }

            if (connected.getAndSet(false)) {
                listener.onDisconnected("Server closed the connection");
            }
        } catch (IOException ex) {
            connected.set(false);
            listener.onError("Connection error: " + ex.getMessage());
        } finally {
            close();
        }
    }

    private void sendRaw(String line) {
        synchronized (sendLock) {
            if (writer != null) {
                writer.println(line);
            }
        }
    }

    private void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
