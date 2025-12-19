package com.example.kybatch.notification.channel;

import com.example.kybatch.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailNotificationChannel implements NotificationChannel {

    /**
     * Spring Mail 전송 객체
     * - SMTP 설정은 application.yml에서 관리
     */
    private final JavaMailSender mailSender;

    /**
     * 배치 실패 알림 수신자
     * - 운영 메일링 리스트 또는 담당자 메일
     */
    @Value("${batch.notification.mail.to}")
    private String to;

    /**
     * STEP 32 기준 메일 알림 포맷
     *
     * 목적:
     * - 장애 이력 공식 기록
     * - Slack/Kakao를 놓쳤을 경우에도 추적 가능
     * - 상세 정보는 메일에 최대한 풀어서 제공
     */
    @Override
    public void send(NotificationMessage message) {

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(to);
        mail.setSubject("🚨 [Batch 실패] " + message.getJobName());

        mail.setText("""
                [Batch Failure Notification]

                Job Name      : %s
                Execution ID  : %s
                Failed Step   : %s
                Failure Type  : %s

                Error Message:
                %s

                Action Guide:
                %s

                ⏰ Occurred At   : %s
                """
                .formatted(
                        message.getJobName(),
                        value(message.getJobExecutionId()),
                        value(message.getStepName()),
                        message.getFailureType(),
                        message.getErrorMessage(),
                        value(message.getActionGuide()),
                        message.getOccurredAt()
                )
        );

        // 메일 발송 (실패 시 예외는 Dispatcher에서 처리)
        mailSender.send(mail);
    }

    /**
     * null-safe 출력 유틸
     */
    private String value(Object v) {
        return v == null ? "N/A" : v.toString();
    }
}
