package com.example.kybatch.domain.stats;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;


/**
 * DailyStats
 * - 하루 단위로 사용자 활동을 집계한 테이블
 * - 이후 Weekly, Monthly 통계의 원본 데이터로 사용
 */
@Entity
@Getter
@Setter
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

    @Builder
    public void DailyStats(Long userId, LocalDate date, Long loginCount, Long viewCount, Long orderCount) {
        this.userId = userId;
        this.date = date;
        this.loginCount = loginCount;
        this.viewCount = viewCount;
        this.orderCount = orderCount;
    }


}
