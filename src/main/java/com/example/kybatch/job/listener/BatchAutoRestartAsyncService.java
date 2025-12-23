package com.example.kybatch.job.listener;

import com.example.kybatch.admin.service.BatchRestartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchAutoRestartAsyncService {

    private final JobExplorer jobExplorer;                 // ✅ 기존에 이미 쓰던 의존성 재사용
    private final BatchRestartService batchRestartService;

    @Async("batchRestartExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void restartAfterCommit(Long originJobExecutionId) {
        try {
            log.warn("[STEP 34] (ASYNC/AFTER_COMMIT) Auto restart triggered. originJobExecutionId={}",
                    originJobExecutionId);

            // ✅ 상태 안정화 대기 (레이스 제거)
            waitUntilTerminal(originJobExecutionId); // 🔧 변경

            batchRestartService.restart(originJobExecutionId);

        } catch (Exception e) {
            log.error("[STEP 34] (ASYNC/AFTER_COMMIT) Auto restart FAILED. originJobExecutionId={}",
                    originJobExecutionId, e);
        }
    }

    /**
     * AFTER_COMMIT 이후에도 JobExecution 상태 반영이 늦는 케이스가 있어
     * FAILED/COMPLETED 같은 terminal 상태가 될 때까지 짧게 폴링한다.
     */
    private void waitUntilTerminal(Long originJobExecutionId) throws InterruptedException { // 🔧 변경
        long deadline = System.currentTimeMillis() + 5_000;

        while (System.currentTimeMillis() < deadline) {
            JobExecution execution = jobExplorer.getJobExecution(originJobExecutionId);
            if (execution != null) {
                BatchStatus status = execution.getStatus();
                if (status == BatchStatus.FAILED || status == BatchStatus.COMPLETED || status == BatchStatus.STOPPED) {
                    log.warn("[AUTO-RESTART] origin status stabilized. jobExecutionId={}, status={}",
                            originJobExecutionId, status);
                    return;
                }
            }
            Thread.sleep(100);
        }

        JobExecution last = jobExplorer.getJobExecution(originJobExecutionId);
        log.warn("[AUTO-RESTART] status not stabilized within 5s. jobExecutionId={}, currentStatus={}",
                originJobExecutionId,
                last == null ? "null" : last.getStatus());
    }
}
