package com.example.kybatch.dto.report;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyReportDTO {

    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long loginCount;
    private Long viewCount;
    private Long orderCount;
}
