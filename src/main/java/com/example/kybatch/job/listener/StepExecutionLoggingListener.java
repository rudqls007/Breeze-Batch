package com.example.kybatch.job.listener;

import com.example.kybatch.api.batchlog.service.BatchLogQueryService;
import com.example.kybatch.domain.batchlog.BatchStepLog;
import com.example.kybatch.domain.batchlog.BatchStepLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StepExecutionLoggingListener implements StepExecutionListener {

    private final BatchStepLogRepository stepLogRepository;
    private final BatchLogQueryService logQueryService;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // ✅ 이제는 DB에 아무것도 저장하지 말고, 그냥 로그만 남기자
        log.info("[STEP-LOG] {}.{} START",
                stepExecution.getJobExecution().getJobInstance().getJobName(),
                stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // ✅ 여기서 한 번에 엔티티 생성 + 완료 정보까지 채워서 저장
        BatchStepLog logEntity = new BatchStepLog(stepExecution);
        logEntity.updateAfter(stepExecution);

        logQueryService.saveStep(logEntity);

        log.info("[STEP-LOG] {}.{} END → status={}, read={}, write={}, skip={}",
                stepExecution.getJobExecution().getJobInstance().getJobName(),
                stepExecution.getStepName(),
                stepExecution.getExitStatus().getExitCode(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());

        return stepExecution.getExitStatus();
    }
}
