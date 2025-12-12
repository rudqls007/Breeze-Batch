package com.example.kybatch.api.batchlog.service;

import com.example.kybatch.api.batchlog.dto.JobLogResponseDTO;
import com.example.kybatch.api.batchlog.dto.StepLogResponseDTO;
import com.example.kybatch.domain.batchlog.BatchJobLogRepository;
import com.example.kybatch.domain.batchlog.BatchStepLog;
import com.example.kybatch.domain.batchlog.BatchStepLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchLogQueryService {

    private final BatchJobLogRepository jobRepo;
    private final BatchStepLogRepository stepRepo;

    public List<JobLogResponseDTO> getRecentJobLogs(String jobName) {

        return jobRepo.findRecentLogs(jobName).stream()
                .map(j -> JobLogResponseDTO.builder()
                        .id(j.getId())
                        .jobName(j.getJobName())
                        .startTime(j.getStartTime())
                        .endTime(j.getEndTime())
                        .status(j.getStatus())
                        .exitMessage(j.getExitMessage())
                        .parameters(j.getParameters())
                        .build())
                .toList();
    }

    public List<StepLogResponseDTO> getRecentStepLogs(String stepName) {

        return stepRepo.findRecentLogs(stepName).stream()
                .map(s -> StepLogResponseDTO.builder()
                        .id(s.getId())
                        .jobName(s.getJobName())
                        .stepName(s.getStepName())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .status(s.getStatus())
                        .readCount(s.getReadCount())
                        .writeCount(s.getWriteCount())
                        .skipCount(s.getSkipCount())
                        .exitMessage(s.getExitMessage())
                        .build())
                .toList();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveStep(BatchStepLog log){
        stepRepo.save(log);
    }
}
