package com.example.kybatch.job.report.parameter;

import com.example.kybatch.domain.user.User;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.configuration.annotation.StepScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * STEP 6 - 날짜 기반 JobParameter 적용 예제
 * ----------------------------------------
 * - JobParameter 'reportDate'를 받아서 검증하고
 * - 해당 날짜를 파일명에 반영한 CSV 리포트 생성
 *
 * 실행 예시:
 *   --spring.batch.job.name=userDailyReportJob reportDate=2025-11-26
 *
 * 주요 포인트:
 * - @StepScope + @Value("#{jobParameters['reportDate']}")
 * - 문자열 날짜를 LocalDate로 변환 + 검증
 * - JobParameter에 따라 동적으로 파일명 구성
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserDailyReportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    /**
     * [Job] userDailyReportJob
     * - UserDailyReportStep 1개만 가진 단일 Job
     * - reportDate 파라미터 기반으로 동작
     */
    @Bean
    public Job userDailyReportJob(Step userDailyReportStep) {
        return new JobBuilder("userDailyReportJob", jobRepository)
                .start(userDailyReportStep)
                .build();
    }

    /**
     * [Step] userDailyReportStep
     * - JPA로 User 엔티티를 페이징 조회
     * - CSV Writer로 파일 출력
     */
    @Bean
    public Step userDailyReportStep(JpaPagingItemReader<User> userDailyReportReader,
                                    FlatFileItemWriter<User> userDailyReportWriter) {

        return new StepBuilder("userDailyReportStep", jobRepository)
                .<User, User>chunk(50, transactionManager) // 한 번에 50건씩 처리
                .reader(userDailyReportReader)
                .writer(userDailyReportWriter)
                .build();
    }

    /**
     * [Reader] JPA 기반 User 조회
     * - JPQL: select u from User u order by u.id
     * - reportDate 자체는 조회 조건으로 쓰지 않고,
     *   "이 날짜 기준 리포트를 뽑는다"라는 의미로만 사용 (STEP 6에서는)
     */
    @Bean
    @StepScope
    public JpaPagingItemReader<User> userDailyReportReader(
            @Value("#{jobParameters['reportDate']}") String reportDate) {

        validateReportDate(reportDate);

        log.info("[userDailyReportReader] reportDate = {}", reportDate);

        JpaPagingItemReader<User> reader = new JpaPagingItemReader<>();
        reader.setName("userDailyReportReader");
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setPageSize(50);
        reader.setQueryString("select u from User u order by u.id");

        return reader;
    }

    /**
     * [Writer] CSV 파일 출력
     * - 파일명에 reportDate를 반영
     *   예) output/user-report-2025-11-26.csv
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<User> userDailyReportWriter(
            @Value("#{jobParameters['reportDate']}") String reportDate) {

        LocalDate localDate = LocalDate.parse(reportDate);

        // 날짜 기반 디렉토리 생성
        String baseDir = String.format(
                "reports/user-daily/%d/%02d/%02d/",
                localDate.getYear(),
                localDate.getMonthValue(),
                localDate.getDayOfMonth()
        );

        // 파일명 구성
        String fileName = String.format("user-report-%s.csv", reportDate);

        Path outputPath = Paths.get(baseDir + fileName);

        // 디렉토리 자동 생성
        try {
            Files.createDirectories(outputPath.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create report directory", e);
        }

        return new FlatFileItemWriterBuilder<User>()
                .name("userDailyReportWriter")
                .resource(new FileSystemResource(outputPath))
                .delimited()
                .delimiter(",")
                .names("id", "name", "email", "status")
                .headerCallback(w -> w.write("id,name,email,status"))
                .build();
    }

    /**
     * reportDate 파라미터 검증 유틸
     * - null / 빈값이면 예외
     * - yyyy-MM-dd 형식 아니면 예외
     */
    private LocalDate validateReportDate(String reportDate) {
        if (reportDate == null || reportDate.isBlank()) {
            throw new IllegalArgumentException(
                    "JobParameter 'reportDate' is required. (예: reportDate=2025-11-26)");
        }

        try {
            return LocalDate.parse(reportDate); // 기본 형식: yyyy-MM-dd
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "JobParameter 'reportDate' 형식이 잘못되었습니다. 형식: yyyy-MM-dd (예: 2025-11-26)",
                    e
            );
        }
    }
}
