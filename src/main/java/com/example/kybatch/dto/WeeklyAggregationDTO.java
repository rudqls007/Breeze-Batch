package com.example.kybatch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyAggregationDTO {


    private Long userId;
    private long loginCount;
    private long viewCount;
    private long orderCount;
}
