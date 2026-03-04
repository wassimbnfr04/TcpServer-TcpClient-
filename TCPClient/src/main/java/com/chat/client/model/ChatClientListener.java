package com.chat.client.model;

public interface ChatClientListener {
    void onConnected(boolean readOnly);

    void onMessage(String message);

    void onDisconnected(String reason);

    void onError(String message);
}
