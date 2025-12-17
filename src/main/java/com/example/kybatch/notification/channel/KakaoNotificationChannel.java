package com.example.kybatch.notification.channel;

import com.example.kybatch.notification.dto.NotificationMessage;
import org.springframework.stereotype.Component;

@Component
public class KakaoNotificationChannel implements NotificationChannel {

    @Override
    public void send(NotificationMessage message) {
        // STEP 28에서는 구조만 정의
        // - 알림톡 API
        // - 사내 메시지 게이트웨이
    }
}
