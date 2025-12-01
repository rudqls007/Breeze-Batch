package com.example.kybatch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyAggregationDTO {

    private Long userId;
    private int year;
    private int month;

    private Long loginCount;
    private Long viewCount;
    private Long orderCount;

}
