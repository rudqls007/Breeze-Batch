package com.example.kybatch.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BatchRestartRequest {

    /**
     * 실패한 JobExecution ID
     */
    private Long jobExecutionId;

    /**
     * 재실행 사유 (운영 기록용)
     */
    private String reason;

    /**
     * 강제 재실행 여부
     * (추후 가드 로직 확장 대비)
     */
    private boolean force;
}