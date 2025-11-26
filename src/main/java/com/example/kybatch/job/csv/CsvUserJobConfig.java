package com.example.kybatch.job.csv;

import com.example.kybatch.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CsvUserJobConfig {

    private final CsvUserReader csvUserReader;
    private final CsvUserProcessor csvUserProcessor;
    private final CsvUserWriter csvUserWriter;

    @Bean
    public Job csvUserJob(JobRepository jobRepository, Step csvUserStep) {
        return new JobBuilder("csvUserJob", jobRepository)
                .start(csvUserStep)
                .build();
    }

    @Bean
    public Step csvUserStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("csvUserStep", jobRepository)
                .<User, User>chunk(10, txManager)
                .reader(csvUserReader.reader())
                .processor(csvUserProcessor)
                .writer(csvUserWriter)
                .build();
    }
}
