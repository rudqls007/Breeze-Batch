package com.example.kybatch.batch.exception;

import com.example.kybatch.batch.failure.BatchFailureType;

public class NonCriticalBatchException extends BatchException{

    public NonCriticalBatchException(String message){
        super(message, BatchFailureType.NON_CRITICAL);
    }
}
