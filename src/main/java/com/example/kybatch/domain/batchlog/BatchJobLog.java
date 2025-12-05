package com.example.kybatch.domain.batchlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobExecution;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

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

    // ExitStatus 기반 → COMPLETED / FAILED / UNKNOWN
    private String status;

    @Column(length = 2000)
    private String exitMessage;

    @Column(length = 1000)
    private String parameters;

    @Column(length = 3000)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String errorStack;


    /** Job 시작 시 기록 */
    public BatchJobLog(JobExecution je) {
        this.jobName = je.getJobInstance().getJobName();
        this.startTime = je.getStartTime();

        // BatchStatus 대신 ExitStatus 기반으로 저장
        this.status = je.getExitStatus().getExitCode();

        this.exitMessage = je.getExitStatus() != null
                ? je.getExitStatus().getExitDescription()
                : null;

        this.parameters = je.getJobParameters() != null
                ? je.getJobParameters().toString()
                : null;
    }

    /** Job 종료 시 업데이트 */
    public void updateAfter(JobExecution je) {

        // 중복 호출 방지
        if (this.endTime == null) {
            this.endTime = je.getEndTime();
        }

        this.status = je.getExitStatus().getExitCode();

        this.exitMessage = je.getExitStatus() != null
                ? je.getExitStatus().getExitDescription()
                : null;

        // 실제 오류가 있을 때의 메시지 / 스택 저장
        if (!je.getFailureExceptions().isEmpty()) {
            Throwable ex = je.getFailureExceptions().get(0);

            this.errorMessage = ex.getMessage();

            this.errorStack = Arrays.stream(ex.getStackTrace())
                    .limit(10)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));
        }
    }
}
