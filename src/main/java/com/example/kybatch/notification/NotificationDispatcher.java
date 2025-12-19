package com.example.kybatch.notification;

import com.example.kybatch.notification.channel.KakaoNotificationChannel;
import com.example.kybatch.notification.channel.MailNotificationChannel;
import com.example.kybatch.notification.channel.SlackNotificationChannel;
import com.example.kybatch.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final MailNotificationChannel mail;
    private final SlackNotificationChannel slack;
    private final KakaoNotificationChannel kakao;

    /**
     * STEP 31 정책:
     * - 배치가 FAILED 되면
     * - 실패 종류와 무관하게
     * - Mail / Slack / Kakao 전부 알림
     *
     * 목적:
     * - 최대한 빠르게 실패 인지
     * - 모바일 기반 실시간 확인
     */
    public void dispatch(NotificationMessage message) {

        try {
            mail.send(message);
        } catch (Exception e) {
            log.error("Mail notification failed", e);
        }

        try {
            slack.send(message);
        } catch (Exception e) {
            log.error("Slack notification failed", e);
        }

        try {
            kakao.send(message);
        } catch (Exception e) {
            log.error("Kakao notification failed", e);
        }
    }
}
