package com.example.kybatch.report;

import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.domain.stats.WeeklyStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class WeeklyFileReportJobTest {

    @Autowired
    JobLauncher launcher;

    @Autowired
    @Qualifier("wfrWeeklyFileReportJob")   // ← 추가!!!
    Job wfrWeeklyFileReportJob;

    @Autowired
    WeeklyStatusRepository weeklyRepo;

    @Test
    void generateWeeklyFileReport() throws Exception {

        // GIVEN
        weeklyRepo.save(new WeeklyStatus(null, 1L, 2025, 3, 10, 30, 5, null, null, null));
        weeklyRepo.save(new WeeklyStatus(null, 2L, 2025, 3, 3, 12, 1, null, null, null));

        JobParameters params = new JobParametersBuilder()
                .addString("outputPath", "C:/batch-output")
                .addLong("year", 2025L)
                .addLong("week", 3L)
                .toJobParameters();

        // WHEN
        launcher.run(wfrWeeklyFileReportJob, params);

        // THEN
        assertTrue(new File("C:/batch-output/weekly_file_report_2025_03.csv").exists());
    }
}
