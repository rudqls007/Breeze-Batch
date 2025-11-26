package com.example.kybatch.job.report;

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

/**
 * STEP 5 - DB(User) → CSV 리포트 Job
 * --------------------------------------------------
 * - Reader : UserReportReader (JPA)
 * - Writer : UserReportWriter (CSV 생성)
 * - Processor 없음 (가공 없이 원본 출력)
 */
@Configuration
@RequiredArgsConstructor
public class UserReportJobConfig {

    private final UserReportReader reader;
    private final UserReportWriter writer;

    @Bean
    public Job userReportJob(JobRepository jobRepository, Step userReportStep) {
        return new JobBuilder("userReportJob", jobRepository)
                .start(userReportStep)
                .build();
    }

    @Bean
    public Step userReportStep(JobRepository jobRepository,
                               PlatformTransactionManager txManager) {
        return new StepBuilder("userReportStep", jobRepository)
                /* Processor가 없는 구조기 떄문에 입 * 출력 타입이 같음.
                *  한 번에 10개씩 처리 -> 청크 작업 한 번이 하나의 트랜잭션으로 이루어짐.*/
                .<User, User>chunk(10, txManager)
                .reader(reader.reader())
                .writer(writer.writer())
                .build();
    }
}
