package com.example.kybatch.api.batchlog.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class JobLogResponseDTO {

    private Long id;
    private String jobName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String exitMessage;
    private String parameters;
}
