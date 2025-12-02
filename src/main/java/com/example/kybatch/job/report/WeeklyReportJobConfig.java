package com.example.kybatch.job.report;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.dto.report.DailyReportDTO;
import com.example.kybatch.dto.report.WeeklyReportDTO;
import com.example.kybatch.job.report.daily.DailyReportProcessor;
import com.example.kybatch.job.report.weekly.WeeklyReportProcessor;
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
public class WeeklyReportJobConfig {

    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    /** Reader */
    @Bean
    @StepScope
    public JpaPagingItemReader<WeeklyStatus> weeklyReportReader(
            @Value("#{jobParameters['week']}") Integer week
    ) {
        JpaPagingItemReader<WeeklyStatus> reader = new JpaPagingItemReader<>();

        reader.setName("weeklyReportReader");
        reader.setEntityManagerFactory(emf);
        reader.setQueryString("SELECT w FROM WeeklyStatus w WHERE w.weekOfYear = :week");
        reader.setPageSize(100);

        Map<String, Object> params = new HashMap<>();
        params.put("week", week);
        reader.setParameterValues(params);

        return reader;
    }

    /** Writer */
    @Bean
    @StepScope
    public FlatFileItemWriter<WeeklyReportDTO> weeklyReportWriter(
            @Value("#{jobParameters['week']}") Integer week
    ) {
        FlatFileItemWriter<WeeklyReportDTO> writer = new FlatFileItemWriter<>();

        writer.setName("weeklyReportWriter");
        writer.setResource(new FileSystemResource("output/weekly-report-" + week + ".csv"));

        BeanWrapperFieldExtractor<WeeklyReportDTO> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{
                "userId", "startDate", "endDate",
                "loginCount", "viewCount", "orderCount"
        });

        DelimitedLineAggregator<WeeklyReportDTO> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(extractor);

        writer.setLineAggregator(aggregator);
        writer.setHeaderCallback(w ->
                w.write("userId,startDate,endDate,loginCount,viewCount,orderCount")
        );

        return writer;
    }

    /** Step */
    @Bean
    public Step weeklyReportStep(
            JpaPagingItemReader<WeeklyStatus> weeklyReportReader,
            WeeklyReportProcessor processor,
            FlatFileItemWriter<WeeklyReportDTO> weeklyReportWriter
    ) {
        return new StepBuilder("weeklyReportStep", jobRepository)
                .<WeeklyStatus, WeeklyReportDTO>chunk(100, tm)
                .reader(weeklyReportReader)
                .processor(processor)
                .writer(weeklyReportWriter)
                .build();
    }

    /** Job */
    @Bean
    public Job weeklyReportJob(Step weeklyReportStep) {
        return new JobBuilder("weeklyReportJob", jobRepository)
                .start(weeklyReportStep)
                .build();
    }
}
