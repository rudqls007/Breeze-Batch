package com.example.kybatch.batch.exception;

import com.example.kybatch.batch.failure.BatchFailureType;

public class RetryableBatchException extends BatchException{

    public RetryableBatchException(String message) {
        super(message, BatchFailureType.RETRYABLE);
    }
}
