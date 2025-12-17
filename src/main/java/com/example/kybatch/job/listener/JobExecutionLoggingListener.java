package com.example.kybatch.job.listener;

import com.example.kybatch.domain.batchlog.BatchJobLog;
import com.example.kybatch.domain.batchlog.BatchJobLogRepository;
import com.example.kybatch.notification.BatchNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobExecutionLoggingListener implements JobExecutionListener {

    private final BatchJobLogRepository jobLogRepository;
    private final BatchNotificationService notificationService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // ✅ 여기서도 DB 저장은 하지 않고, 그냥 시작 로그만
        log.info("[JOB-LOG] {} START (params={})",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        // 실행 이력 저장
        BatchJobLog logEntity = new BatchJobLog(jobExecution);
        logEntity.updateAfter(jobExecution);
        jobLogRepository.save(logEntity);

        // ❗ 실패 시 메일 알림
        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            notificationService.sendFailureMail(
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getJobParameters().toString(),
                    jobExecution.getAllFailureExceptions().toString()
            );
        }

        log.info("[JOB-LOG] {} END → status={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus());
    }
}
