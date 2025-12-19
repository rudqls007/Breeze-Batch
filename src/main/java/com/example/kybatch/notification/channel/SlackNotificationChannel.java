package com.example.kybatch.notification.channel;

import com.example.kybatch.notification.dto.NotificationMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SlackNotificationChannel implements NotificationChannel {

    /**
     * Slack Incoming Webhook URL
     * - Slack App에서 발급
     * - 배치 실패 시 메시지를 전송할 채널과 연결됨
     */
    @Value("${batch.notification.slack.webhook-url}")
    private String webhookUrl;

    /**
     * 단순 Webhook 호출용 RestTemplate
     * - Slack은 인증 없이 Webhook URL로 POST
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * STEP 32 기준 Slack 알림 포맷
     *
     * 목적:
     * - 배치 실패를 모바일/실시간으로 즉시 인지
     * - "어떤 배치가 / 어디서 / 왜 실패했는지"를 한 눈에 전달
     * - 로그를 보지 않고도 1차 판단 가능하도록 구성
     */
    @Override
    public void send(NotificationMessage message) {

        String text = """
                🚨 [Batch 실패]

                • Job: %s
                • ExecutionId: %s
                • Failed Step: %s
                • Type: %s

                ❗ Error
                %s

                %s

                ⏰ Occurred At: %s
                """
                .formatted(
                        message.getJobName(),
                        value(message.getJobExecutionId()),
                        value(message.getStepName()),
                        message.getFailureType(),
                        message.getErrorMessage(),
                        value(message.getActionGuide()),
                        message.getOccurredAt()
                );

        Map<String, String> payload = Map.of("text", text);

        // Slack Webhook 호출 (실패 시 예외는 Dispatcher에서 잡힘)
        restTemplate.postForEntity(webhookUrl, payload, Void.class);
    }

    /**
     * null-safe 출력 유틸
     * - Slack 메시지에서 null 그대로 노출되는 것을 방지
     */
    private String value(Object v) {
        return v == null ? "N/A" : v.toString();
    }
}
