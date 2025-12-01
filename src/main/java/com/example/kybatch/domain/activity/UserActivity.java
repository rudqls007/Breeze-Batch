package com.example.kybatch.domain.activity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

@Entity
@Table(name = "user_activity")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class UserActivity {


    /*
    * Batch 통계 집계 속도를 위해 날짜 연산을 로그 저장 시점에 수행하는 전략
    * 로그가 대량일수록 Processor 계산 비용을 줄일 수 있음
    * */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* 어떤 사용자의 행동인지 체크 */
    private Long userId;

    /* 개별 행동 카운트 (raw data) */
    private int loginCount;
    private int viewCount;
    private int orderCount;

    /* 행동 발생 시각 (기간 필터링에 활용) */
    private LocalDateTime createdAt;

    /* 통계 그룹핑을 빠르게 하기 위한 필드 */
    private int weekOfYear;

    private Integer month;


    /*
     * 엔티티가 DB에 저장되거나 조회될 때
     * createdAt 기반으로 month/weekOfYear 자동 계산
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt != null) {
            this.month = this.createdAt.getMonthValue();
            this.weekOfYear = this.createdAt.get(WeekFields.ISO.weekOfWeekBasedYear());
        }
    }



}
