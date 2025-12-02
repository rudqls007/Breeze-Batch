package com.example.kybatch.report;

import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.domain.stats.MonthlyStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class MonthlyReportJobTest {

    @Autowired
    JobLauncher launcher;

    @Autowired
    Job monthlyReportJob;

    @Autowired
    MonthlyStatusRepository repo;

    @Test
    void generateMonthlyReportCsv() throws Exception {

        /* GIVEN */
        repo.save(MonthlyStatus.builder()
                .userId(1L)
                .year(2025)
                .month(11)
                .loginCount(10L)
                .viewCount(30L)
                .orderCount(5L)
                .build());
        /* WHEN */
        JobParameters params = new JobParametersBuilder()
                .addLong("year", 2025L)
                .addLong("month", 11L)
                .toJobParameters();

        launcher.run(monthlyReportJob, params);

        /* THEN */
        File file = new File("output/monthly-report-2025-11.csv");

        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
}
