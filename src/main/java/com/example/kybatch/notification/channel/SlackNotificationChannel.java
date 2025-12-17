package com.example.kybatch.notification.channel;

import com.example.kybatch.notification.dto.NotificationMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SlackNotificationChannel implements NotificationChannel {

    @Value("${batch.notification.slack.webhook-url}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void send(NotificationMessage message) {

        Map<String, String> payload = Map.of(
                "text",
                """
                🚨 Batch Failure
                Job: %s
                Type: %s
                Error: %s
                """
                        .formatted(
                                message.getJobName(),
                                message.getFailureType(),
                                message.getErrorMessage()
                        )
        );

        restTemplate.postForEntity(webhookUrl, payload, Void.class);
    }
}
