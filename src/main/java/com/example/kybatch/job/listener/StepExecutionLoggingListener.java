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

        BatchStepLog logEntity = new BatchStepLog(stepExecution);

        stepLogRepository.save(logEntity);

        // afterStep에서 찾을 수 있도록 ID 저장
        stepExecution.getExecutionContext().put("STEP_LOG_ID", logEntity.getId());

        log.info("[STEP-LOG] {}.{} START",
                logEntity.getJobName(),
                logEntity.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("### [DEBUG] afterJob 실행됨 — 실제로?");
        Long id = (Long) stepExecution.getExecutionContext().get("STEP_LOG_ID");

        if (id != null) {
            stepLogRepository.findById(id)
                    .ifPresent(entity -> {
                        entity.updateAfter(stepExecution);
                        stepLogRepository.save(entity);
                    });
        }

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
