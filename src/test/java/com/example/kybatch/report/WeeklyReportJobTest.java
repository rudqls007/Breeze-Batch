package com.example.kybatch.report;

import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.domain.stats.WeeklyStatusRepository;
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
class WeeklyReportJobTest {

    @Autowired
    JobLauncher launcher;

    @Autowired
    Job weeklyReportJob;

    @Autowired
    WeeklyStatusRepository repo;

    @Test
    void generateWeeklyReportCsv() throws Exception {

        // given
        repo.save(WeeklyStatus.builder()
                .userId(1L)
                .weekOfYear(48)
                .startDate(LocalDate.of(2025, 11, 24))
                .endDate(LocalDate.of(2025, 11, 30))
                .loginCount(3L)
                .viewCount(7L)
                .orderCount(1L)
                .build());

        // when
        JobParameters params = new JobParametersBuilder()
                .addLong("week", 48L)
                .toJobParameters();

        launcher.run(weeklyReportJob, params);

        // then
        File file = new File("output/weekly-report-48.csv");
        assertTrue(file.exists(), "CSV 파일이 생성되지 않았습니다.");

        assertTrue(file.length() > 0, "CSV 파일에 데이터가 기록되지 않았습니다.");
    }
}
