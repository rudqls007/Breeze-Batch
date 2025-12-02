package com.example.kybatch.dto.report;

import lombok.*;

import java.time.LocalDate;

/**
 *  Daily Report 용 DTO
 *  - CSV Export에 필요한 필드만 추출
 * */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReportDTO {

    private Long userId;
    private LocalDate date;
    private Long loginCount;
    private Long viewCount;
    private Long orderCount;
}
