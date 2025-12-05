package com.example.kybatch.domain.batchlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.StepExecution;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

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

    // ExitStatus.getExitCode() → COMPLETED / FAILED / UNKNOWN 등
    private String status;

    private long readCount;

    private long writeCount;

    private long skipCount;

    // StepExecution ExitMessage
    @Column(length = 2000)
    private String exitMessage;

    // Exception 메시지
    @Column(length = 3000)
    private String errorMessage;

    // StackTrace는 TEXT 활용
    @Column(columnDefinition = "TEXT")
    private String errorStack;


    /** Step 시작 시점 초기화 */
    public BatchStepLog(StepExecution se) {
        this.jobName = se.getJobExecution().getJobInstance().getJobName();
        this.stepName = se.getStepName();
        this.startTime = se.getStartTime();
        this.status = se.getExitStatus().getExitCode(); // ExitStatus 기반
    }

    /** Step 종료 시점 업데이트 */
    public void updateAfter(StepExecution se) {

        // 중복 업데이트 방지 (필수는 아니지만 안정성 ↑)
        if (this.endTime == null) {
            this.endTime = se.getEndTime();
        }

        this.status = se.getExitStatus().getExitCode();  // COMPLETED/FAILED/UNKNOWN

        this.readCount = se.getReadCount();
        this.writeCount = se.getWriteCount();
        this.skipCount = se.getSkipCount();

        this.exitMessage = se.getExitStatus() != null
                ? se.getExitStatus().getExitDescription()
                : null;

        // 에러 메시지 처리
        if (!se.getFailureExceptions().isEmpty()) {
            Throwable ex = se.getFailureExceptions().get(0);

            this.errorMessage = ex.getMessage();

            // StackTrace 앞 10줄 저장
            this.errorStack = Arrays.stream(ex.getStackTrace())
                    .limit(10)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));
        }
    }
}
