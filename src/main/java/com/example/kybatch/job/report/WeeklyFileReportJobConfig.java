package com.example.kybatch.job.report;

import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.domain.stats.WeeklyStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WeeklyFileReportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;
    private final WeeklyStatusRepository weeklyRepo;

    // -------------------- Job --------------------
    @Bean
    public Job wfrWeeklyFileReportJob(Step wfrWeeklyFileReportStep) {
        return new JobBuilder("wfrWeeklyFileReportJob", jobRepository)
                .start(wfrWeeklyFileReportStep)
                .build();
    }

    // -------------------- Step --------------------
    @Bean
    public Step wfrWeeklyFileReportStep(
            RepositoryItemReader<WeeklyStatus> wfrWeeklyFileReportReader,
            FlatFileItemWriter<WeeklyStatus> wfrWeeklyFileReportWriter
    ) {
        return new StepBuilder("wfrWeeklyFileReportStep", jobRepository)
                .<WeeklyStatus, WeeklyStatus>chunk(1000, tm)
                .reader(wfrWeeklyFileReportReader)
                .writer(wfrWeeklyFileReportWriter)
                .build();
    }

    // -------------------- Reader --------------------
    @Bean
    @StepScope
    public RepositoryItemReader<WeeklyStatus> wfrWeeklyFileReportReader(
            @Value("#{jobParameters['year']}") Long yearParam,
            @Value("#{jobParameters['week']}") Long weekParam
    ) {

        return new RepositoryItemReaderBuilder<WeeklyStatus>()
                .name("wfrWeeklyFileReportReader")
                .repository(weeklyRepo)
                .methodName("findByYearAndWeekOfYear")
                .arguments(yearParam.intValue(), weekParam.intValue())
                .pageSize(1000)
                .sorts(Map.of("userId", Sort.Direction.ASC))
                .build();
    }

    // -------------------- Writer --------------------
    @Bean
    @StepScope
    public FlatFileItemWriter<WeeklyStatus> wfrWeeklyFileReportWriter(
            @Value("#{jobParameters['outputPath']}") String outputPath,
            @Value("#{jobParameters['year']}") Long yearParam,
            @Value("#{jobParameters['week']}") Long weekParam
    ) {

        String fileName = String.format(
                "%s/weekly_file_report_%d_%02d.csv",
                outputPath,
                yearParam.intValue(),
                weekParam.intValue()
        );

        BeanWrapperFieldExtractor<WeeklyStatus> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{
                "userId",
                "year",
                "weekOfYear",
                "loginCount",
                "viewCount",
                "orderCount"
        });

        DelimitedLineAggregator<WeeklyStatus> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(extractor);

        return new FlatFileItemWriterBuilder<WeeklyStatus>()
                .name("wfrWeeklyFileReportWriter")
                .resource(new FileSystemResource(fileName))
                .lineAggregator(aggregator)
                .headerCallback(writer ->
                        writer.write("userId,year,weekOfYear,loginCount,viewCount,orderCount"))
                .build();
    }
}