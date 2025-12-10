package com.example.kybatch.api.batchlog.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StepLogResponseDTO {

    private Long id;
    private String jobName;
    private String stepName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private long readCount;
    private long writeCount;
    private long skipCount;
    private String exitMessage;

}
