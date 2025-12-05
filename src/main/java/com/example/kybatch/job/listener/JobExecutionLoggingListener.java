package com.example.kybatch.job.listener;

import com.example.kybatch.domain.batchlog.BatchJobLog;
import com.example.kybatch.domain.batchlog.BatchJobLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobExecutionLoggingListener implements JobExecutionListener {

    private final BatchJobLogRepository jobLogRepository;

    // 같은 JobExecution에 대해 before/after에서 같이 쓸 로그 엔티티 보관
    private final ThreadLocal<BatchJobLog> currentLog = new ThreadLocal<>();

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[JOB-LOG] {} START, params={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());

        BatchJobLog logEntity = new BatchJobLog(jobExecution);
        jobLogRepository.save(logEntity);
        currentLog.set(logEntity);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchJobLog logEntity = currentLog.get();
        if (logEntity != null) {
            logEntity.updateAfter(jobExecution);
            jobLogRepository.save(logEntity);
            currentLog.remove();
        }

        log.info("[JOB-LOG] {} END, status={}, exit={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                jobExecution.getExitStatus());
    }
}
