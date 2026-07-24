package com.example.kybatch.controller.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/batch/admin")
@RequiredArgsConstructor
public class BatchAdminController {

    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;

    /**
     * Batch 메타 정보 조회
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("jobNames", jobExplorer.getJobNames());
        result.put("registeredJobCount", jobExplorer.getJobNames().size());

        return ResponseEntity.ok(result);
    }

    /**
     * Job 실행 가능 여부 확인
     */
    @GetMapping("/exists/{jobName}")
    public ResponseEntity<Boolean> existsJob(
            @PathVariable String jobName
    ) {

        boolean exists =
                jobExplorer.getJobNames()
                        .stream()
                        .anyMatch(name -> name.equals(jobName));

        return ResponseEntity.ok(exists);
    }

}
