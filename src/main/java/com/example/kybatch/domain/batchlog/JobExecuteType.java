package com.example.kybatch.domain.batchlog;

/**
 * Job 실행 유형
 *
 * AUTO           : 스케줄러 / 시스템 자동 실행
 * ADMIN_RESTART  : Admin API를 통한 수동 재실행
 */
public enum JobExecuteType {
    AUTO,
    ADMIN_RESTART
}