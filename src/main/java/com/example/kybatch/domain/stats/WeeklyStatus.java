package com.example.kybatch.domain.stats;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "weekly_status")
@Getter
@Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class WeeklyStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    /* 2025 */
    @Column(name = "year_value")
    private int year;
    /* ISO 주차 번호 */
    private int weekOfYear;

    private long loginCount;
    private long viewCount;
    private long orderCount;

    private LocalDate startDate;
    private LocalDate endDate;
}
