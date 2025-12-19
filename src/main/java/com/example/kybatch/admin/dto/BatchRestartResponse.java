package com.example.kybatch.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BatchRestartResponse {

    /**
     * 재실행 요청한 기존 JobExecution ID
     */
    private Long originJobExecutionId;

    /**
     * 새로 생성된 JobExecution ID
     */
    private Long newJobExecutionId;

}
