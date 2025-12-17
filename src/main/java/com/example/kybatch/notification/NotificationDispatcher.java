package com.example.kybatch.notification;

import com.example.kybatch.batch.failure.BatchFailureType;
import com.example.kybatch.notification.channel.KakaoNotificationChannel;
import com.example.kybatch.notification.channel.MailNotificationChannel;
import com.example.kybatch.notification.channel.SlackNotificationChannel;
import com.example.kybatch.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final MailNotificationChannel mail;
    private final SlackNotificationChannel slack;
    private final KakaoNotificationChannel kakao;

    public void dispatch(NotificationMessage message) {

        mail.send(message);

        if (message.getFailureType() == BatchFailureType.RETRYABLE) {
            slack.send(message);
        }

        if (message.getFailureType() == BatchFailureType.FATAL) {
            slack.send(message);
            kakao.send(message);
        }
    }
}
