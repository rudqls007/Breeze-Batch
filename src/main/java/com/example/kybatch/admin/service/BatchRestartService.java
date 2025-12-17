package com.example.kybatch.admin.service;

import com.example.kybatch.admin.dto.BatchRestartRequest;
import com.example.kybatch.admin.exception.BatchRestartFailException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchRestartService {

    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    public void restart(BatchRestartRequest request) {

        JobExecution jobExecution =
                jobExplorer.getJobExecution(request.getJobExecutionId());

        if (jobExecution == null) {
            throw new BatchRestartFailException(
                    request.getJobExecutionId(),
                    "존재하지 않는 JobExecution 입니다."
            );
        }

        // 운영 가드: FAILED 상태만 재실행 가능
        if (jobExecution.getStatus() != BatchStatus.FAILED) {
            throw new BatchRestartFailException(
                    request.getJobExecutionId(),
                    "FAILED 상태의 배치만 재실행할 수 있습니다."
            );
        }

        try {
            jobOperator.restart(request.getJobExecutionId());
        } catch (Exception e) {
            throw new BatchRestartFailException(
                    request.getJobExecutionId(),
                    e.getMessage()
            );
        }
    }
}
