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

    private final JavaMailSender mailSender;

    @Value("${batch.notification.mail.to}")
    private String to;

    @Override
    public void send(NotificationMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(to);
        mail.setSubject("[Batch 실패] " + message.getJobName());
        mail.setText("""
            Job: %s
            Type: %s
            Error: %s
            Time: %s
        """.formatted(
                message.getJobName(),
                message.getFailureType(),
                message.getErrorMessage(),
                message.getOccurredAt()
        ));

        mailSender.send(mail);
    }
}
