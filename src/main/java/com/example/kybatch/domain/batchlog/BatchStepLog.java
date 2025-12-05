package com.example.kybatch.domain.batchlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.StepExecution;

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

    private String status;

    private long readCount;

    private long writeCount;

    private long skipCount;

    @Column(length = 2000)
    private String exitMessage;

    public BatchStepLog(StepExecution se) {
        this.jobName = se.getJobExecution().getJobInstance().getJobName();
        this.stepName = se.getStepName();
        this.startTime = se.getStartTime();
        this.status = se.getStatus() != null ? se.getStatus().toString() : null;
    }

    public void updateAfter(StepExecution se) {
        this.endTime = se.getEndTime();
        this.status = se.getStatus() != null ? se.getStatus().toString() : null;
        this.readCount = se.getReadCount();
        this.writeCount = se.getWriteCount();
        this.skipCount = se.getSkipCount();
        this.exitMessage = se.getExitStatus() != null ? se.getExitStatus().getExitDescription() : null;
    }
}
