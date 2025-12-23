package com.example.kybatch.notification.listener;

import com.example.kybatch.batch.failure.BatchFailureType;
import com.example.kybatch.notification.NotificationDispatcher;
import com.example.kybatch.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchFailureNotificationListener implements JobExecutionListener {

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void afterJob(JobExecution jobExecution) {

        // 1️⃣ FAILED 아니면 아무 것도 안 함
        if (jobExecution.getStatus() != BatchStatus.FAILED) {
            return;
        }

        log.error("[STEP 34] Job FAILED. jobExecutionId={}", jobExecution.getId());

        // 2️⃣ 실패 알림 전송
        NotificationMessage message = NotificationMessage.builder()
                .jobName(jobExecution.getJobInstance().getJobName())
                .jobExecutionId(jobExecution.getId())
                .stepName(resolveFailedStep(jobExecution))
                .parameters(jobExecution.getJobParameters().toString())
                .errorMessage(resolveErrorMessage(jobExecution))
                .failureType(resolveFailureType(jobExecution))
                .actionGuide(resolveActionGuide())
                .occurredAt(LocalDateTime.now())
                .build();

        notificationDispatcher.dispatch(message);
    }

    private String resolveFailedStep(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .filter(step -> step.getStatus() == BatchStatus.FAILED)
                .map(StepExecution::getStepName)
                .findFirst()
                .orElse("UNKNOWN_STEP");
    }

    private String resolveErrorMessage(JobExecution jobExecution) {
        return jobExecution.getAllFailureExceptions().isEmpty()
                ? "Unknown batch failure"
                : jobExecution.getAllFailureExceptions().get(0).getMessage();
    }

    private BatchFailureType resolveFailureType(JobExecution jobExecution) {
        if (jobExecution.getAllFailureExceptions().isEmpty()) {
            return BatchFailureType.FATAL;
        }

        String msg = String.valueOf(
                jobExecution.getAllFailureExceptions().get(0).getMessage()
        ).toLowerCase();

        if (msg.contains("timeout") || msg.contains("lock")) {
            return BatchFailureType.RETRYABLE;
        }

        if (msg.contains("validation")) {
            return BatchFailureType.NON_CRITICAL;
        }

        return BatchFailureType.FATAL;
    }

    private String resolveActionGuide() {
        return """
               🔁 조치 가이드
               - 실패 원인 점검
               - 자동 재실행 정책 확인
               - 필요 시 Admin API 수동 재실행
               """;
    }
}
