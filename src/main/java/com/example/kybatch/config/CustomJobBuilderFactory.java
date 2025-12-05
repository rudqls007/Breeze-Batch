package com.example.kybatch.config;

import com.example.kybatch.job.listener.JobExecutionLoggingListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomJobBuilderFactory {

    private final JobRepository jobRepository;
    private final JobExecutionLoggingListener jobListener;

    public JobBuilder job(String name) {
        return new JobBuilder(name, jobRepository)
                .listener(jobListener);
    }
}
