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

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // ✅ 여기서도 DB 저장은 하지 않고, 그냥 시작 로그만
        log.info("[JOB-LOG] {} START (params={})",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("### [DEBUG] afterJob 실행됨 — 실제로?");

        // ✅ JobExecution 정보를 기반으로 엔티티를 만들고, 종료 정보까지 채워서 한 번에 저장
        BatchJobLog logEntity = new BatchJobLog(jobExecution);
        logEntity.updateAfter(jobExecution);
        jobLogRepository.save(logEntity);

        log.info("[JOB-LOG] {} END → status={}, exit={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getExitStatus().getExitCode(),
                jobExecution.getExitStatus().getExitDescription());
    }
}
