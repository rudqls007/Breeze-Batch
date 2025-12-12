package com.example.kybatch.domain.batchlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobExecution;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * 🔥 배치 Job 실행 로그를 남기는 엔티티
 *  - 매 Job 실행마다 1 row 생성
 *  - 성공 / 실패 여부와 에러 메시지, 스택 트레이스를 저장
 */
@Entity
@Table(name = "batch_job_log")
@Getter
@NoArgsConstructor
public class BatchJobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 실행된 Job 이름 */
    private String jobName;

    /** Job 시작/종료 시간 */
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** COMPLETED / FAILED / UNKNOWN */
    private String status;

    /** 최종 상태에 대한 간단 요약 (COMPLETED, JOB FAILED 등) */
/*    @Column(columnDefinition = "TEXT")
    private String exitMessage; */

    /** Job Parameter 전체 문자열 */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /** 한 줄 에러 메시지 */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /** 전체 스택 트레이스 */
    @Column(columnDefinition = "TEXT")
    private String errorStack;

    /**
     * Job 시작 시점에 기본 정보 기록
     */
    public BatchJobLog(JobExecution je) {
        this.jobName = je.getJobInstance().getJobName();
        this.startTime = je.getStartTime();
        this.parameters = je.getJobParameters() != null
                ? je.getJobParameters().toString()
                : null;
    }

    /**
     * Job 종료 후 상태 및 에러 정보 업데이트
     */
    public void updateAfter(JobExecution je) {

        // Spring Batch가 endTime을 늦게 설정하므로 직접 now() 사용
        this.endTime = LocalDateTime.now();
        this.status = je.getExitStatus().getExitCode();  // COMPLETED / FAILED

        // -------------------------------
        // 🔥 실패한 Job 처리 로직
        // -------------------------------
        if (je.getStatus().isUnsuccessful()) {

            // exitMessage는 간단한 요약만 저장

            // 실패 예외가 존재하는 경우
            if (!je.getAllFailureExceptions().isEmpty()) {
                Throwable ex = je.getAllFailureExceptions().get(0);
                this.errorMessage = ex.getMessage();   // 한 줄 메시지
                this.errorStack = getStackTrace(ex);   // 전체 스택
            }
            // 실패했는데 failureExceptions는 비어있는 경우 → exitDescription에서 처리
            else {
                String desc = je.getExitStatus().getExitDescription();
                this.errorMessage = firstLine(desc);
                this.errorStack = desc;
            }

        }
        // -------------------------------
        // 🔥 성공한 Job 처리 로직
        // -------------------------------
        else {
            this.errorMessage = null;
            this.errorStack = null;
        }
    }

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
