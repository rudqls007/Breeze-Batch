package com.example.kybatch.domain.batchlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.StepExecution;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_step_log")
@Getter
@NoArgsConstructor
public class BatchStepLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;
    private String stepName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // COMPLETED / FAILED / UNKNOWN
    private String status;

    private long readCount;
    private long writeCount;
    private long skipCount;

    @Column(columnDefinition = "TEXT")
    private String exitMessage;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String errorStack;

    /** Step 시작 시 초기화 */
    public BatchStepLog(StepExecution se) {
        this.jobName = se.getJobExecution().getJobInstance().getJobName();
        this.stepName = se.getStepName();
        this.startTime = se.getStartTime();
        // ❗ status는 처음에 넣지 않음 → Dirty Checking 방해됨
    }

    /** Step 종료 후 업데이트 */
    public void updateAfter(StepExecution se) {
        this.endTime = se.getEndTime();
        this.status = se.getExitStatus().getExitCode();   // COMPLETED/FAILED/UNKNOWN

        this.readCount = se.getReadCount();
        this.writeCount = se.getWriteCount();
        this.skipCount = se.getSkipCount();

        this.exitMessage = se.getExitStatus() != null
                ? se.getExitStatus().getExitDescription()
                : null;

        if (!se.getFailureExceptions().isEmpty()) {
            Throwable ex = se.getFailureExceptions().get(0);
            this.errorMessage = ex.getMessage();
            this.errorStack = getStackTrace(ex);
        }
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
