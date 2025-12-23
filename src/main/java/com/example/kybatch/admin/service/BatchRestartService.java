package com.example.kybatch.admin.service;

import com.example.kybatch.admin.exception.BatchRestartFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchRestartService {

    private static final String ORIGIN_JOB_EXECUTION_ID_KEY = "ORIGIN_JOB_EXECUTION_ID"; // 🔧 변경
    private static final String EXECUTE_TYPE_KEY = "EXECUTE_TYPE"; // 🔧 변경
    private static final String EXECUTE_TYPE_AUTO_RESTART = "AUTO_RESTART"; // 🔧 변경

    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository; // 🔧 변경 (주입 추가)

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long restart(Long originJobExecutionId) {
        JobExecution origin = jobExplorer.getJobExecution(originJobExecutionId);

        if (origin == null) {
            throw new BatchRestartFailException(originJobExecutionId,
                    "배치 재실행 실패: 존재하지 않는 executionId=" + originJobExecutionId);
        }

        if (origin.getStatus() != BatchStatus.FAILED) {
            throw new BatchRestartFailException(originJobExecutionId,
                    "배치 재실행 실패: FAILED 상태만 재실행 가능. current=" + origin.getStatus());
        }

        try {
            log.info("[AUTO-RESTART] Attempting restart. originJobExecutionId={}", originJobExecutionId);

            Long newExecutionId = jobOperator.restart(originJobExecutionId);

            // ✅ 재실행 JobExecution(=newExecutionId)에 "이게 재실행이다" 메타데이터를 박아준다
            //    -> JobExecutionLoggingListener가 이 값을 읽어 log 테이블에 저장 가능
            JobExecution restarted = jobExplorer.getJobExecution(newExecutionId); // 🔧 변경
            if (restarted != null) {
                restarted.getExecutionContext().putLong(ORIGIN_JOB_EXECUTION_ID_KEY, originJobExecutionId); // 🔧 변경
                restarted.getExecutionContext().putString(EXECUTE_TYPE_KEY, EXECUTE_TYPE_AUTO_RESTART);     // 🔧 변경
                jobRepository.updateExecutionContext(restarted);                                            // 🔧 변경
            }

            log.info("[AUTO-RESTART] Restart requested OK. newJobExecutionId={}", newExecutionId);
            return newExecutionId;

        } catch (Exception e) {
            throw new BatchRestartFailException(originJobExecutionId,
                    "배치 재실행 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
