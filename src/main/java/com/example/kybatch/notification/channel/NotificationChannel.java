package com.example.kybatch.notification.channel;

import com.example.kybatch.notification.dto.NotificationMessage;

public interface NotificationChannel {

    void send(NotificationMessage message);
}
