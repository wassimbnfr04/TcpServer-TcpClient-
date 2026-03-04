package com.chat.server.model;

import java.util.List;

public interface ChatServerListener {
    void onLog(String message);

    void onUsersChanged(List<String> users);

    void onStatusChanged(boolean online, String detail);
}
