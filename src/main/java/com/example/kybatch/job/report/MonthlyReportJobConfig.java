package com.example.kybatch.job.report;

import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.dto.report.MonthlyReportDTO;
import com.example.kybatch.job.report.monthly.MonthlyReportProcessor;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class MonthlyReportJobConfig {

    // JPA EntityManager를 만들어주는 팩토리 (JPA 조회용 인프라)
    private final EntityManagerFactory emf;

    // Job/Step 실행 이력을 저장하는 저장소
    private final JobRepository jobRepository;

    // Chunk 트랜잭션을 관리하는 트랜잭션 매니저
    private final PlatformTransactionManager tm;

    /**
     * 📌 Reader
     * - MonthlyStatus 엔티티를 JPA 페이징 방식으로 읽어오는 ItemReader
     * - year, month JobParameter를 이용해서 해당 연/월 데이터만 조회
     * - @StepScope: Step 실행 시점에 Bean을 생성 (jobParameters 사용 가능)
     */
    @Bean
    @StepScope
    public JpaPagingItemReader<MonthlyStatus> monthlyReportReader(
            @Value("#{jobParameters['year']}") Integer year,
            @Value("#{jobParameters['month']}") Integer month
    ) {
        JpaPagingItemReader<MonthlyStatus> reader = new JpaPagingItemReader<>();

        // 리더 이름 (모니터링, 디버깅용)
        reader.setName("monthlyReportReader");

        // JPA EntityManagerFactory 설정
        reader.setEntityManagerFactory(emf);

        // 특정 연도/월에 해당하는 MonthlyStatus만 조회하는 JPQL
        reader.setQueryString(
                "SELECT m FROM MonthlyStatus m " +
                        "WHERE m.year = :year AND m.month = :month"
        );

        // 한 번에 가져올 페이지 사이즈 (Chunk size와 보통 맞추거나 비슷하게 설정)
        reader.setPageSize(100);

        // JPQL 파라미터 바인딩
        Map<String, Object> params = new HashMap<>();
        params.put("year", year);
        params.put("month", month);
        reader.setParameterValues(params);

        return reader;
    }

    /**
     * 📌 Writer
     * - Processor에서 만들어진 MonthlyReportDTO를 CSV 파일로 출력하는 ItemWriter
     * - 파일명에 year, month를 사용하여 월별 리포트 파일을 생성
     * - @StepScope: 실행 시점에 year/month JobParameter를 받아서 파일명 동적 생성
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<MonthlyReportDTO> monthlyReportWriter(
            @Value("#{jobParameters['year']}") Integer year,
            @Value("#{jobParameters['month']}") Integer month
    ) {
        FlatFileItemWriter<MonthlyReportDTO> writer = new FlatFileItemWriter<>();

        // Writer 이름 설정
        writer.setName("monthlyReportWriter");

        // 출력 파일 경로 및 이름 (예: output/monthly-report-2025-11.csv)
        writer.setResource(new FileSystemResource(
                "output/monthly-report-" + year + "-" + month + ".csv"
        ));

        // DTO의 필드 값을 추출해 줄 FieldExtractor
        BeanWrapperFieldExtractor<MonthlyReportDTO> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{
                "userId", "year", "month", "loginCount", "viewCount", "orderCount"
        });

        // 필드들을 구분자(,)로 이어붙여 한 줄의 CSV 라인으로 만들어주는 Aggregator
        DelimitedLineAggregator<MonthlyReportDTO> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");           // 콤마 구분자 설정
        aggregator.setFieldExtractor(extractor);

        // CSV 헤더 한 줄 작성
        writer.setHeaderCallback(w ->
                w.write("userId,year,month,loginCount,viewCount,orderCount")
        );

        // 각 DTO를 CSV 라인으로 변환하는 로직 설정
        writer.setLineAggregator(aggregator);

        return writer;
    }

    /**
     * 📌 Step
     * - Reader → Processor → Writer 를 하나의 Chunk 기반 Step으로 묶는 설정
     * - <MonthlyStatus, MonthlyReportDTO> :
     *   Reader는 MonthlyStatus를 읽고, Processor는 MonthlyReportDTO로 변환
     */
    @Bean
    public Step monthlyReportStep(
            JpaPagingItemReader<MonthlyStatus> monthlyReportReader,
            MonthlyReportProcessor processor,
            FlatFileItemWriter<MonthlyReportDTO> monthlyReportWriter
    ) {
        return new StepBuilder("monthlyReportStep", jobRepository)
                // Chunk 단위 처리 설정: 100건 단위로 트랜잭션 처리
                .<MonthlyStatus, MonthlyReportDTO>chunk(100, tm)
                .reader(monthlyReportReader)   // JPA Reader
                .processor(processor)          // 엔티티 → DTO 변환/가공 로직
                .writer(monthlyReportWriter)   // CSV Writer
                .build();
    }

    /**
     * 📌 Job
     * - 월간 리포트 생성 배치 Job
     * - monthlyReportStep을 시작 Step으로 두고, 단일 Step으로 구성
     */
    @Bean
    public Job monthlyReportJob(Step monthlyReportStep) {
        return new JobBuilder("monthlyReportJob", jobRepository)
                .start(monthlyReportStep)  // 첫 번째이자 유일한 Step
                .build();
    }
}
