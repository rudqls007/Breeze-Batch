package com.example.kybatch.domain.batchlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobExecution;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    // COMPLETED / FAILED / UNKNOWN
    private String status;

    @Column(columnDefinition = "TEXT")
    private String exitMessage;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String errorStack;

    /** Job 시작 시 호출 */
    public BatchJobLog(JobExecution je) {
        this.jobName = je.getJobInstance().getJobName();
        this.startTime = je.getStartTime();

        this.parameters = je.getJobParameters() != null
                ? je.getJobParameters().toString()
                : null;
    }

    /** Job 종료 후 업데이트 */
    public void updateAfter(JobExecution je) {
        this.endTime = je.getEndTime();
        this.status = je.getExitStatus().getExitCode();        // COMPLETED/FAILED/UNKNOWN
        this.exitMessage = je.getExitStatus().getExitDescription();

        if (!je.getFailureExceptions().isEmpty()) {
            Throwable ex = je.getFailureExceptions().get(0);
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
