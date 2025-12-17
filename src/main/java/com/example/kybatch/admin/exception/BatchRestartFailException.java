package com.example.kybatch.admin.exception;

public class BatchRestartFailException extends RuntimeException {

    public BatchRestartFailException(Long jobExecutionId, String message) {
        super("배치 재실행 실패 (jobExecutionId=" + jobExecutionId + ") : " + message);
    }
}
