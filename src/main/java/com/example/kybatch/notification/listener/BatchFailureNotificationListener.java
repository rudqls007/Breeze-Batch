package com.example.kybatch.notification.listener;

import com.example.kybatch.batch.failure.BatchFailureType;
import com.example.kybatch.notification.NotificationDispatcher;
import com.example.kybatch.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
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

        // 2) 너희가 이미 만들어 둔 NotificationMessage 포맷 그대로 생성
        NotificationMessage message = NotificationMessage.builder()
                .jobName(jobExecution.getJobInstance().getJobName())
                .parameters(jobExecution.getJobParameters().toString())
                .errorMessage(resolveErrorMessage(jobExecution))
                .failureType(resolveFailureType(jobExecution))
                .occurredAt(LocalDateTime.now())
                .build();

        // 3) 기존 Dispatcher로 위임 (Slack/Mail/Kakao는 dispatcher 내부에서 분기)
        notificationDispatcher.dispatch(message);
    }

    private String resolveErrorMessage(JobExecution jobExecution) {
        return jobExecution.getAllFailureExceptions().isEmpty()
                ? "Unknown batch failure"
                : jobExecution.getAllFailureExceptions().get(0).getMessage();
    }

    /**
     * ※ 여기 로직은 “최소 구현” 버전
     *   일단은 원인 메시지/예외로 failureType을 대략 분류하고,
     *   다음 STEP에서 정교화하면 된다.
     */
    private BatchFailureType resolveFailureType(JobExecution jobExecution) {
        if (jobExecution.getAllFailureExceptions().isEmpty()) {
            return BatchFailureType.FATAL;
        }

        Throwable cause = jobExecution.getAllFailureExceptions().get(0);

        String msg = (cause.getMessage() == null) ? "" : cause.getMessage().toLowerCase();

        // 잠금/타임아웃/네트워크성 등 “일시적” 성격은 보통 재시도 대상
        if (msg.contains("timeout") || msg.contains("lock") || msg.contains("deadlock")) {
            return BatchFailureType.RETRYABLE;
        }

        // 데이터 일부 오류/파싱/검증 등은 NON_CRITICAL로 시작 (운영 정책에 따라 조정)
        if (msg.contains("parse") || msg.contains("validation") || msg.contains("constraint")) {
            return BatchFailureType.NON_CRITICAL;
        }

        return BatchFailureType.FATAL;
    }
}
