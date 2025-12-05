package com.example.kybatch.job.listener;

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

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("[STEP-LOG] {}.{} START",
                stepExecution.getJobExecution().getJobInstance().getJobName(),
                stepExecution.getStepName());

        BatchStepLog entity = new BatchStepLog(stepExecution);
        stepLogRepository.save(entity);

        // 나중에 afterStep에서 다시 꺼내 쓰기 위해 ID 저장
        stepExecution.getExecutionContext().put("stepLogId", entity.getId());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Long id = (Long) stepExecution.getExecutionContext().get("stepLogId");
        if (id != null) {
            stepLogRepository.findById(id).ifPresent(entity -> {
                entity.updateAfter(stepExecution);
                stepLogRepository.save(entity);
            });
        }

        log.info("[STEP-LOG] {}.{} END, status={}, read={}, write={}, skip={}",
                stepExecution.getJobExecution().getJobInstance().getJobName(),
                stepExecution.getStepName(),
                stepExecution.getStatus(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());

        return stepExecution.getExitStatus();
    }
}
