package com.example.kybatch.domain.batchlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobExecution;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_job_log")
@Getter
@NoArgsConstructor
public class BatchJobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    @Column(length = 2000)
    private String exitMessage;

    @Column(length = 1000)
    private String parameters;

    public BatchJobLog(JobExecution je) {
        this.jobName = je.getJobInstance().getJobName();
        this.startTime = je.getStartTime();
        this.status = je.getStatus() != null ? je.getStatus().toString() : null;
        this.exitMessage = je.getExitStatus() != null ? je.getExitStatus().getExitDescription() : null;
        this.parameters = je.getJobParameters() != null ? je.getJobParameters().toString() : null;
    }

    public void updateAfter(JobExecution je) {
        this.endTime = je.getEndTime();
        this.status = je.getStatus() != null ? je.getStatus().toString() : null;
        this.exitMessage = je.getExitStatus() != null ? je.getExitStatus().getExitDescription() : null;
    }
}
