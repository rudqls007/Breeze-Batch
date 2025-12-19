package com.example.kybatch.notification.listener;

import com.example.kybatch.batch.failure.BatchFailureType;
import com.example.kybatch.notification.NotificationDispatcher;
import com.example.kybatch.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BatchFailureNotificationListener implements JobExecutionListener {

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void afterJob(JobExecution jobExecution) {

        // 1) FAILED 아닌 경우는 알림 발송 대상 아님
        if (jobExecution.getStatus() != BatchStatus.FAILED) {
            return;
        }

        // 2) STEP 32 기준 NotificationMessage 생성
        NotificationMessage message = NotificationMessage.builder()
                .jobName(jobExecution.getJobInstance().getJobName())
                .jobExecutionId(jobExecution.getId())                 // 🔥 STEP 32
                .stepName(resolveFailedStep(jobExecution))            // 🔥 STEP 32
                .parameters(jobExecution.getJobParameters().toString())
                .errorMessage(resolveErrorMessage(jobExecution))
                .failureType(resolveFailureType(jobExecution))
                .actionGuide(resolveActionGuide())                    // 🔥 STEP 32
                .occurredAt(LocalDateTime.now())
                .build();

        // 3) 기존 Dispatcher로 위임 (Mail / Slack / Kakao)
        notificationDispatcher.dispatch(message);
    }

    /**
     * 실패한 Step 이름 추출
     * - 운영자가 "어디서 죽었는지" 바로 알기 위함
     */
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

    /**
     * ※ failureType은 참고 정보용
     *   STEP 31 정책상 전송 분기에는 사용하지 않음
     */
    private BatchFailureType resolveFailureType(JobExecution jobExecution) {
        if (jobExecution.getAllFailureExceptions().isEmpty()) {
            return BatchFailureType.FATAL;
        }

        Throwable cause = jobExecution.getAllFailureExceptions().get(0);
        String msg = (cause.getMessage() == null) ? "" : cause.getMessage().toLowerCase();

        if (msg.contains("timeout") || msg.contains("lock") || msg.contains("deadlock")) {
            return BatchFailureType.RETRYABLE;
        }

        if (msg.contains("parse") || msg.contains("validation") || msg.contains("constraint")) {
            return BatchFailureType.NON_CRITICAL;
        }

        return BatchFailureType.FATAL;
    }

    /**
     * STEP 32 핵심
     * - 알림을 본 운영자가 "다음 행동"을 바로 알 수 있게 함
     * - 자동 재실행은 STEP 33에서 처리
     */
    private String resolveActionGuide() {
        return """
               🔁 조치 가이드
               - 배치 재실행 가능 여부 확인
               - 동일 파라미터 재실행 권장
               - Admin API 또는 수동 실행
               """;
    }
}
