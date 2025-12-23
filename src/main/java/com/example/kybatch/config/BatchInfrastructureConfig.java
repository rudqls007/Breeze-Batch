package com.example.kybatch.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchInfrastructureConfig {

    @Bean
    public JobOperator jobOperator(
            JobExplorer jobExplorer,
            JobLauncher jobLauncher,
            JobRepository jobRepository,
            JobRegistry jobRegistry
    ) {
        SimpleJobOperator op = new SimpleJobOperator();
        op.setJobExplorer(jobExplorer);
        op.setJobLauncher(jobLauncher);
        op.setJobRepository(jobRepository);
        op.setJobRegistry(jobRegistry);
        return op;
    }
}
