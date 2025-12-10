package com.example.kybatch.api.batchlog;

import com.example.kybatch.api.batchlog.dto.JobLogResponseDTO;
import com.example.kybatch.api.batchlog.dto.StepLogResponseDTO;
import com.example.kybatch.api.batchlog.service.BatchLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch-logs")
public class BatchLogController {

    private final BatchLogQueryService batchLogQueryService;

    @GetMapping("/jobs")
    public List<JobLogResponseDTO> getJobLogs(@RequestParam String jobName) {
        return batchLogQueryService.getRecentJobLogs(jobName);
    }

    @GetMapping("/steps")
    public List<StepLogResponseDTO> getStepLogs(@RequestParam String stepName) {
        return batchLogQueryService.getRecentStepLogs(stepName);
    }
}
