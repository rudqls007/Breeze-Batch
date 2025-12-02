package com.example.kybatch.report;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DailyReportJobTest {

    @Autowired private JobLauncher launcher;
    @Autowired private Job dailyReportJob;
    @Autowired private DailyStatusRepository repo;

    @Test
    void generateDailyReportCsv() throws Exception {

        // given
        repo.save(DailyStatus.builder()
                .userId(1L)
                .date(LocalDate.of(2025, 11, 26))
                .loginCount(3L)
                .viewCount(5L)
                .orderCount(2L)
                .build());

        // when
        JobParameters params = new JobParametersBuilder()
                .addString("date", "2025-11-26")
                .toJobParameters();

        launcher.run(dailyReportJob, params);

        // then
        File file = new File("output/daily-report-2025-11-26.csv");
        assertTrue(file.exists());
    }
}
