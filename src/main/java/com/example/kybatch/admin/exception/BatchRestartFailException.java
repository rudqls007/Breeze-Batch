package com.example.kybatch.admin.exception;

import lombok.Getter;

@Getter
public class BatchRestartFailException extends RuntimeException {

    private final Long jobExecutionId;

    // 메시지 기반 (jobExecutionId 없음)
    public BatchRestartFailException(String message) {
        super(message);
        this.jobExecutionId = null;
    }

    public BatchRestartFailException(String message, Throwable cause) {
        super(message, cause);
        this.jobExecutionId = null;
    }

    // jobExecutionId + 메시지 기반
    public BatchRestartFailException(Long jobExecutionId, String message) {
        super(message);
        this.jobExecutionId = jobExecutionId;
    }

    public BatchRestartFailException(Long jobExecutionId, String message, Throwable cause) {
        super(message, cause);
        this.jobExecutionId = jobExecutionId;
    }
}
