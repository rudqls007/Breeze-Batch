package com.example.kybatch.batch.failure;

/**
 * 배치 실패 유형 정의
 * - 실패 발생 시 대응 전략 판단 기준
 */
public enum BatchFailureType {

    /** 재시도 가능한 일시적 실패 */
    RETRYABLE,

    /** Job은 성공, 일부 데이터만 실패 (Skip / Recover 대상) */
    NON_CRITICAL,

    /** 즉시 중단이 필요한 치명적 실패 */
    FATAL
}
