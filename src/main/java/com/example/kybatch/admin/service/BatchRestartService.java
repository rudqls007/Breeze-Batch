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
    // private final BatchJobLogRepository batchJobLogRepository; (STEP 34)

    /**
     * 배치 재실행 처리
     *
     * @return 새로 생성된 JobExecution ID
     */
    public Long restart(BatchRestartRequest request) {

        // 1️⃣ JobExecution 존재 여부 확인
        JobExecution jobExecution =
                jobExplorer.getJobExecution(request.getJobExecutionId());

        if (jobExecution == null) {
            throw new BatchRestartFailException(
                    request.getJobExecutionId(),
                    "존재하지 않는 JobExecution 입니다."
            );
        }

        // 2️⃣ 운영 가드 로직
        // force=false 인 경우 FAILED 상태만 재실행 허용
        if (!request.isForce()
                && jobExecution.getStatus() != BatchStatus.FAILED) {

            throw new BatchRestartFailException(
                    request.getJobExecutionId(),
                    "FAILED 상태의 배치만 재실행할 수 있습니다."
            );
        }

        try {
            // 3️⃣ Spring Batch 공식 재실행 API
            // → 기존 ExecutionContext 기반으로 새 JobExecution 생성
            Long newExecutionId =
                    jobOperator.restart(request.getJobExecutionId());

            // 4️⃣ (STEP 34) 재실행 이력 저장 예정
            /*
            batchJobLogRepository.save(
                    BatchJobLog.restart(
                            request.getJobExecutionId(),
                            newExecutionId,
                            request.getReason()
                    )
            );
            */

            return newExecutionId;

        } catch (Exception e) {
            // 5️⃣ 재실행 실패 시 운영 친화적인 예외 변환
            throw new BatchRestartFailException(
                    request.getJobExecutionId(),
                    "배치 재실행 중 오류 발생: " + e.getMessage()
            );
        }
    }
}
