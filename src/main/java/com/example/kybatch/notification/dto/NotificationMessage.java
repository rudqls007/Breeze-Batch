package com.example.kybatch.notification.dto;

import com.example.kybatch.batch.failure.BatchFailureType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationMessage {

    // 기존 필드
    private String jobName;
    private String parameters;
    private String errorMessage;
    private BatchFailureType failureType;
    private LocalDateTime occurredAt;

    // STEP 32 추가 필드
    private Long jobExecutionId;   // JobExecution 식별자
    private String stepName;       // 실패한 Step 이름
    private String actionGuide;    // 운영자 행동 가이드
}
