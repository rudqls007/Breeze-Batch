package com.example.kybatch.config;

import com.example.kybatch.job.listener.StepExecutionLoggingListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@RequiredArgsConstructor
public class CustomStepBuilderFactory {

    private final JobRepository jobRepository;
    private final StepExecutionLoggingListener stepListener;

    public StepBuilder step(String name) {
        // StepBuilder 생성 시 자동으로 listener 추가
        return new StepBuilder(name, jobRepository)
                .listener(stepListener);
    }
}
