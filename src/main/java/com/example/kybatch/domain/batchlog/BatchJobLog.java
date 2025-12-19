package com.example.kybatch.domain.batchlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobExecution;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * 🔥 배치 Job 실행 로그 엔티티 (STEP 34 확장)
 *
 * - 매 JobExecution 마다 1 row 생성
 * - 자동 실행 / Admin 수동 재실행 구분 가능
 * - 실패 시 에러 메시지 + 스택 트레이스 저장
 */
@Entity
@Table(name = "batch_job_log")
@Getter
@NoArgsConstructor
public class BatchJobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Spring Batch JobExecution ID */
    private Long jobExecutionId;

    /** Admin 재실행인 경우 기준이 된 이전 JobExecution ID */
    private Long originJobExecutionId;

    /** 실행 유형 (AUTO / ADMIN_RESTART) */
    @Enumerated(EnumType.STRING)
    private JobExecuteType executeType;

    /** Admin 재실행 사유 */
    @Column(columnDefinition = "TEXT")
    private String restartReason;

    /** 실행된 Job 이름 */
    private String jobName;

    /** Job 시작/종료 시간 */
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** COMPLETED / FAILED / UNKNOWN */
    private String status;

    /** Job Parameter 전체 문자열 */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /** 한 줄 에러 메시지 */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /** 전체 스택 트레이스 */
    @Column(columnDefinition = "TEXT")
    private String errorStack;

    // =====================================================
    // 생성 로직
    // =====================================================

    /**
     * 🔹 자동 실행 Job 로그 생성 (기존 로직 유지)
     */
    public BatchJobLog(JobExecution je) {
        this.jobExecutionId = je.getId();
        this.jobName = je.getJobInstance().getJobName();
        this.startTime = je.getStartTime();
        this.parameters = je.getJobParameters() != null
                ? je.getJobParameters().toString()
                : null;
        this.executeType = JobExecuteType.AUTO;
    }

    /**
     * 🔹 Admin 수동 재실행 Job 로그 생성 (STEP 34)
     */
    public static BatchJobLog adminRestart(
            JobExecution newExecution,
            Long originJobExecutionId,
            String reason
    ) {
        BatchJobLog log = new BatchJobLog();
        log.jobExecutionId = newExecution.getId();
        log.originJobExecutionId = originJobExecutionId;
        log.jobName = newExecution.getJobInstance().getJobName();
        log.startTime = newExecution.getStartTime();
        log.parameters = newExecution.getJobParameters() != null
                ? newExecution.getJobParameters().toString()
                : null;
        log.executeType = JobExecuteType.ADMIN_RESTART;
        log.restartReason = reason;
        return log;
    }

    // =====================================================
    // 실행 종료 후 상태 업데이트
    // =====================================================

    /**
     * Job 종료 후 상태 및 에러 정보 업데이트
     */
    public void updateAfter(JobExecution je) {

        // Spring Batch endTime 지연 이슈로 now() 사용
        this.endTime = LocalDateTime.now();
        this.status = je.getExitStatus().getExitCode();

        // -------------------------------
        // 🔥 실패한 Job 처리
        // -------------------------------
        if (je.getStatus().isUnsuccessful()) {

            if (!je.getAllFailureExceptions().isEmpty()) {
                Throwable ex = je.getAllFailureExceptions().get(0);
                this.errorMessage = ex.getMessage();
                this.errorStack = getStackTrace(ex);
            } else {
                String desc = je.getExitStatus().getExitDescription();
                this.errorMessage = firstLine(desc);
                this.errorStack = desc;
            }

        }
        // -------------------------------
        // 🔥 성공한 Job 처리
        // -------------------------------
        else {
            this.errorMessage = null;
            this.errorStack = null;
        }
    }

    // =====================================================
    // 유틸 메서드
    // =====================================================

    /** 문자열의 첫 줄 반환 */
    private String firstLine(String text) {
        if (text == null) return null;
        return text.split("\n")[0];
    }

    /** Exception을 문자열 스택으로 변환 */
    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
