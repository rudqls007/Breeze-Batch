package com.example.kybatch.domain.stats;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DailyStatus
 * - 하루 단위 유저 활동 집계 결과
 * - 이후 Weekly / Monthly 통계의 원본 데이터
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDate date;

    private Long loginCount;
    private Long viewCount;
    private Long orderCount;

    @Column(name = "created_at", insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Builder
    public DailyStatus(Long userId,
                       LocalDate date,
                       Long loginCount,
                       Long viewCount,
                       Long orderCount) {
        this.userId = userId;
        this.date = date;
        this.loginCount = loginCount;
        this.viewCount = viewCount;
        this.orderCount = orderCount;
    }
}
