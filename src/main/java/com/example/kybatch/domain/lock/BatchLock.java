package com.example.kybatch.domain.lock;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * BatchLock
 * ---------------------------------
 * 배치 잡 실행을 보호하기 위한 락 테이블의 도메인 엔티티.
 * 운영 배치 시스템에서 필수적으로 사용되는 보호 장치.
 *
 * - lockName : 락 고유 식별자 (PK)
 * - lockPolicy : 락 정책 (Exclusive, Timed, Reentrant 등)
 * - lockType : 적용 대상 배치 종류 (Monthly, Weekly 등)
 * - description : 운영자가 이해하기 위한 설명
 * - lockedAt : 락 획득 시간
 * - expiredAt : 락 만료 시간(선택)
 * - hostname : 락을 획득한 서버 정보
 * - dbSchema : 이 락이 속한 DB 스키마
 */
@Entity
@Table(name = "batch_lock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BatchLock {

    @Id
    @Column(name = "lock_name", length = 100)
    private String lockName;

    @Enumerated(EnumType.STRING)
    @Column(name = "lock_policy", length = 30, nullable = false)
    private BatchLockPolicy lockPolicy;

    @Column(name = "lock_type", length = 50)
    private String lockType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "hostname", length = 100)
    private String hostname;

    @Column(name = "db_schema", length = 50)
    private String dbSchema;
}
