package com.example.kybatch.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BatchRestartRequest {

    /**
     * 재실행 대상 JobExecution ID
     */
    private Long jobExecutionId;

    /**
     * 운영자 재실행 사유
     * - 장애 조치
     * - 데이터 보정
     * - 임시 대응 등
     */
    private String reason;

    /**
     * 강제 재실행 여부
     * - false: FAILED 상태만 허용
     * - true : STOPPED / UNKNOWN 확장 대비
     */
    private boolean force;
}
