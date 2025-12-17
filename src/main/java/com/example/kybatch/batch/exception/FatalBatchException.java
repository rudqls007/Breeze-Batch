package com.example.kybatch.batch.exception;

import com.example.kybatch.batch.failure.BatchFailureType;

public class FatalBatchException extends BatchException {

    public FatalBatchException(String message){
        super(message, BatchFailureType.FATAL);
    }
}
