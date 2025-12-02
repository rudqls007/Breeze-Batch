package com.example.kybatch.job.report;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.dto.report.DailyReportDTO;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DailyReportJobConfig {

    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    /** Daily CSV Export Reader Bean */
    @Bean
    @StepScope
    public JpaPagingItemReader<DailyStatus> dailyReportReader(
            @Value("#{jobParameters['date']}") String date
    ) {

        JpaPagingItemReader<DailyStatus> reader = new JpaPagingItemReader<>();

        reader.setName("dailyReportReader");
        reader.setEntityManagerFactory(emf);
        reader.setQueryString("SELECT d FROM DailyStatus d WHERE d.date = :date");
        reader.setPageSize(100);

        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.parse(date));
        reader.setParameterValues(params);

        return reader;
    }

    /** Daily CSV Export Writer Bean */
    @Bean
    @StepScope
    public FlatFileItemWriter<DailyReportDTO> dailyReportWriter(
            @Value("#{jobParameters['date']}") String date
    ) {

        FlatFileItemWriter<DailyReportDTO> writer = new FlatFileItemWriter<>();

        writer.setName("dailyReportWriter");
        writer.setResource(new FileSystemResource("output/daily-report-" + date + ".csv"));

        BeanWrapperFieldExtractor<DailyReportDTO> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"userId", "date", "loginCount", "viewCount", "orderCount"});

        DelimitedLineAggregator<DailyReportDTO> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(extractor);

        writer.setLineAggregator(aggregator);
        writer.setHeaderCallback(w -> w.write("userId,date,loginCount,viewCount,orderCount"));

        return writer;
    }

    /** Step */
    @Bean
    public Step dailyReportStep(
            JpaPagingItemReader<DailyStatus> dailyReportReader,
            DailyReportProcessor processor,
            FlatFileItemWriter<DailyReportDTO> dailyReportWriter
    ) {
        return new StepBuilder("dailyReportStep", jobRepository)
                .<DailyStatus, DailyReportDTO>chunk(100, tm)
                .reader(dailyReportReader)
                .processor(processor)
                .writer(dailyReportWriter)
                .build();
    }

    /** Job */
    @Bean
    public Job dailyReportJob(Step dailyReportStep) {
        return new JobBuilder("dailyReportJob", jobRepository)
                .start(dailyReportStep)
                .build();
    }
}
