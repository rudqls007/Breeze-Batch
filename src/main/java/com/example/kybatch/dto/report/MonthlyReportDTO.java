package com.example.kybatch.dto.report;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class MonthlyReportDTO {

    private Long userId;
    private Integer yearValue;
    private Integer monthValue;
    private Long loginCount;
    private Long viewCount;
    private Long orderCount;
}
