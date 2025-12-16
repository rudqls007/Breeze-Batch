package com.example.kybatch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 *  JPQL에서 GROUP BY 결과를 담아오는 DTO
 * */
@Getter
@AllArgsConstructor
public class DailyAggregationDTO {

    /** 유저 ID */
    private Long userId;

    /** 하루 동안 로그인 총합 */
    private Long loginCount;

    /** 하루 동안 조회 총합 */
    private Long viewCount;

    /** 하루 동안 주문 총합 */
    private Long orderCount;
}
