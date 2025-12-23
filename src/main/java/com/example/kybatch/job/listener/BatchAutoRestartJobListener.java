package com.example.kybatch.job.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchAutoRestartJobListener implements JobExecutionListener {

    private static final String AUTO_RESTART_ONCE_FLAG = "AUTO_RESTART_ONCE_DONE";

    private final ApplicationEventPublisher eventPublisher;
    private final JobRepository jobRepository;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() != BatchStatus.FAILED) {
            return;
        }

        Long originJobExecutionId = jobExecution.getId();

        // ✅ 무한 루프 방지 (ExecutionContext에는 getBoolean 없음)
        Boolean alreadyDone = (Boolean) jobExecution
                .getExecutionContext()
                .get(AUTO_RESTART_ONCE_FLAG);

        if (Boolean.TRUE.equals(alreadyDone)) {
            log.warn(
                    "[AUTO-RESTART] skip: already restarted once. jobExecutionId={}",
                    originJobExecutionId
            );
            return;
        }

        // ✅ 1회 재시작 플래그 저장
        jobExecution.getExecutionContext().put(AUTO_RESTART_ONCE_FLAG, true);
        jobRepository.updateExecutionContext(jobExecution);

        log.warn(
                "[STEP 34] Job FAILED → auto restart requested (AFTER_COMMIT event). jobExecutionId={}",
                originJobExecutionId
        );

        // ✅ AFTER_COMMIT에서 처리될 이벤트만 발행 (restart 직접 호출 ❌)
        eventPublisher.publishEvent(originJobExecutionId);
    }
}
