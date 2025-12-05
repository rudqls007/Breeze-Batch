package com.example.kybatch.domain.stats;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(name = "year_value")
    private int year;

    @Column(name = "month_value")
    private int month;

    private Long loginCount;
    private Long viewCount;
    private Long orderCount;

    @Column(name = "created_at", insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;


    @Builder
    public MonthlyStatus(Long userId, int year, int month, Long loginCount, Long viewCount, Long orderCount) {
        this.userId = userId;
        this.year = year;
        this.month = month;
        this.loginCount = loginCount;
        this.viewCount = viewCount;
        this.orderCount = orderCount;
    }
}
