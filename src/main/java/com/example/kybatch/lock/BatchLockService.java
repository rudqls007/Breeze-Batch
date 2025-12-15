package com.example.kybatch.lock;

import com.example.kybatch.domain.lock.BatchLock;
import com.example.kybatch.domain.lock.BatchLockPolicy;
import com.example.kybatch.domain.lock.BatchLockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchLockService {

    private final BatchLockRepository repository;

    /**
     * =========================================================================
     *  PUBLIC API — 배치에서 사용하는 공식 함수
     * =========================================================================
     */
    public boolean acquireLock(String lockName,
                               BatchLockPolicy policy,
                               String lockType,
                               String description) {

        Optional<BatchLock> current = repository.findById(lockName);

        // 기존 락이 이미 존재 → 정책에 따라 처리
        if (current.isPresent()) {
            return handleExistingLock(current.get(), policy);
        }

        // 새 잠금 생성
        BatchLock newLock = BatchLock.builder()
                .lockName(lockName)
                .lockPolicy(policy)
                .lockType(lockType)
                .description(description)
                .lockedAt(LocalDateTime.now())
                .expiredAt(nextExpireTime(policy))
                .hostname(getHostname())
                .dbSchema("public")
                .build();

        repository.save(newLock);
        log.info("[BatchLock] 락 획득 성공 - name={}, policy={}, type={}", lockName, policy, lockType);
        return true;
    }


    /**
     * =========================================================================
     *  PUBLIC API — 락 해제
     * =========================================================================
     */
    public void releaseLock(String lockName) {
        try {
            repository.deleteById(lockName);
            log.info("[BatchLock] 락 해제 완료 - {}", lockName);

        } catch (Exception e) {
            log.error("[BatchLock] 락 해제 실패 - {}", lockName, e);
        }
    }



    /**
     * =========================================================================
     *  내부 정책 처리
     * =========================================================================
     */

    private boolean handleExistingLock(BatchLock existing, BatchLockPolicy policy) {

        switch (policy) {

            case EXCLUSIVE:
                log.warn("[BatchLock] EXCLUSIVE 정책: 이미 락이 존재하여 실행 불가");
                return false;

            case REENTRANT:
                if (isSameHost(existing)) {
                    log.info("[BatchLock] REENTRANT 정책: 동일 서버 → 재진입 허용");
                    return true;
                }
                log.warn("[BatchLock] REENTRANT 정책: 다른 서버가 락을 잡음 → 실행 불가");
                return false;

            case TIMED:
                if (isExpired(existing)) {
                    log.info("[BatchLock] TIMED 정책: 기존 락 만료됨 → 새로운 락 부여");
                    repository.delete(existing);
                    return true;
                }
                log.warn("[BatchLock] TIMED 정책: 락이 아직 유효함 → 실행 불가");
                return false;

            case FAIL_FAST:
                log.warn("[BatchLock] FAIL_FAST 정책: 락 존재 → 즉시 실패");
                return false;

            case WAIT_RETRY:
                return retryAcquire(existing.getLockName(), 5, 500);

            case DISTRIBUTED:
                // Redis/ZooKeeper 등과 연동할 때 확장됨
                log.warn("[BatchLock] DISTRIBUTED 정책 미구현 → 기본 FAIL");
                return false;

            default:
                return false;
        }
    }


    /**
     * =========================================================================
     *  부가 메서드
     * =========================================================================
     */

    private boolean retryAcquire(String lockName, int attempts, long sleepMillis) {
        for (int i = 1; i <= attempts; i++) {
            sleep(sleepMillis);

            if (!repository.existsById(lockName)) {
                log.info("[BatchLock] WAIT_RETRY 정책: {}번째 시도 → 성공", i);
                return true;
            }
            log.info("[BatchLock] WAIT_RETRY 정책: {}번째 시도 실패", i);
        }
        return false;
    }


    private boolean isSameHost(BatchLock lock) {
        return lock.getHostname() != null &&
                lock.getHostname().equals(getHostname());
    }

    private boolean isExpired(BatchLock lock) {
        return lock.getExpiredAt() != null &&
                lock.getExpiredAt().isBefore(LocalDateTime.now());
    }

    private LocalDateTime nextExpireTime(BatchLockPolicy policy) {
        if (policy == BatchLockPolicy.TIMED) {
            return LocalDateTime.now().plusMinutes(10); // 기본 TTL 10분
        }
        return null;
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }
}
