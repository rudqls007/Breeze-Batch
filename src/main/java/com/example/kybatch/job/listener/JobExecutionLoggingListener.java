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

    private final ThreadLocal<BatchJobLog> currentLog = new ThreadLocal<>();

    @Override
    public void beforeJob(JobExecution jobExecution) {

        BatchJobLog logEntity = new BatchJobLog(jobExecution);

        jobLogRepository.save(logEntity);
        currentLog.set(logEntity);

        log.info("[JOB-LOG] {} START (params={})",
                logEntity.getJobName(),
                logEntity.getParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("### [DEBUG] afterJob 실행됨 — 실제로?");
        BatchJobLog logEntity = currentLog.get();

        if (logEntity != null) {
            logEntity.updateAfter(jobExecution);
            jobLogRepository.save(logEntity);
            currentLog.remove();
        }

        log.info("[JOB-LOG] {} END → status={}, exit={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getExitStatus().getExitCode(),
                jobExecution.getExitStatus().getExitDescription());
    }
}
