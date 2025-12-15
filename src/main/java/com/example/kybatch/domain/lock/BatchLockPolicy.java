package com.example.kybatch.domain.lock;

public enum BatchLockPolicy {
    EXCLUSIVE,      // (기본) 한 번에 한 Job만 실행 가능
    REENTRANT,      // 동일 서버면 재진입 허용
    TIMED,          // expiredAt 기반 자동 만료
    FAIL_FAST,      // 락 실패 즉시 종료
    WAIT_RETRY,     // 재시도 정책
    DISTRIBUTED     // 분산 환경에서 Redis/ZK 기반 락
}
