package com.example.kybatch.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchNotificationService {

    private final JavaMailSender mailSender;

    @Value("${batch.notification.mail.to}")
    private String mailTo;

    /**
     * 배치 실패 시 메일 알림 전송
     */
    public void sendFailureMail(
            String jobName,
            String parameters,
            String errorMessage
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(mailTo); // TODO: yml 분리
        message.setSubject("[🚨 Batch 실패] " + jobName);

        message.setText("""
                배치 작업이 실패했습니다.

                ▶ Job Name : %s
                ▶ Parameters : %s
                ▶ Error Message :
                %s

                ▶ 발생 시각 : %s
                """.formatted(
                jobName,
                parameters,
                errorMessage,
                LocalDateTime.now()
        ));

        mailSender.send(message);
        log.info("[MAIL] Batch failure mail sent. job={}", jobName);
    }
}
