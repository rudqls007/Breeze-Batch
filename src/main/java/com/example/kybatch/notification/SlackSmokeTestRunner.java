package com.example.kybatch.notification;

import com.example.kybatch.batch.failure.BatchFailureType;
import com.example.kybatch.notification.channel.SlackNotificationChannel;
import com.example.kybatch.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Profile("slack-test")
@Component
@RequiredArgsConstructor
public class SlackSmokeTestRunner implements CommandLineRunner {

    private final SlackNotificationChannel slackNotificationChannel;

    @Override
    public void run(String... args) {
        slackNotificationChannel.send(
                NotificationMessage.builder()
                        .jobName("slack-smoke-test")
                        .parameters("N/A")
                        .errorMessage("Slack Webhook 연결 테스트")
                        .failureType(BatchFailureType.FATAL)
                        .occurredAt(LocalDateTime.now())
                        .build()
        );
    }
}
