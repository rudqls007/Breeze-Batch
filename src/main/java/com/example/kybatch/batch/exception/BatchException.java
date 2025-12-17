package com.example.kybatch.batch.exception;

import com.example.kybatch.batch.failure.BatchFailureType;

public abstract class BatchException extends RuntimeException {

    private final BatchFailureType failureType;

    protected BatchException(String message, BatchFailureType failureType) {
        super(message);
        this.failureType = failureType;
    }

    public BatchFailureType getFailureType() {
        return failureType;
    }
}
