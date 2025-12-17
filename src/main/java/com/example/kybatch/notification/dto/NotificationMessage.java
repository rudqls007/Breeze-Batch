package com.example.kybatch.notification.dto;

import com.example.kybatch.batch.failure.BatchFailureType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationMessage {

    private String jobName;
    private String parameters;
    private String errorMessage;
    private BatchFailureType failureType;
    private LocalDateTime occurredAt;
}