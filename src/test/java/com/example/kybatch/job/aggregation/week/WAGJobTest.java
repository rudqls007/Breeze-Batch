package com.example.kybatch.job.aggregation.week;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.domain.stats.WeeklyStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class WAGJobTest {

    @Autowired
    JobLauncher launcher;

    @Autowired
    Job weeklyAggregationJob;

    @Autowired
    DailyStatusRepository dailyRepo;

    @Autowired
    WeeklyStatusRepository weeklyRepo;

    @BeforeEach
    void clean() {
        weeklyRepo.deleteAll();
        dailyRepo.deleteAll();
    }

    @Test
    void weeklyAggregation_success() throws Exception {

        int year = 2025;
        int week = 3;

        // ISO Week 2025-03 → "2025-01-13 ~ 2025-01-19"
        LocalDate monday = LocalDate.of(2025, 1, 13);

        // user 1 → 3건 생성
        dailyRepo.save(DailyStatus.builder()
                .userId(1L)
                .date(monday)
                .loginCount(1L).viewCount(10L).orderCount(1L)
                .build());

        dailyRepo.save(DailyStatus.builder()
                .userId(1L)
                .date(monday.plusDays(1))
                .loginCount(2L).viewCount(20L).orderCount(2L)
                .build());

        dailyRepo.save(DailyStatus.builder()
                .userId(1L)
                .date(monday.plusDays(2))
                .loginCount(3L).viewCount(30L).orderCount(3L)
                .build());

        // user 2 → 1건
        dailyRepo.save(DailyStatus.builder()
                .userId(2L)
                .date(monday.plusDays(3))
                .loginCount(5L).viewCount(50L).orderCount(5L)
                .build());

        // Job 파라미터 설정
        JobParameters params = new JobParametersBuilder()
                .addLong("year", (long) year)
                .addLong("week", (long) week)
                .toJobParameters();

        // WHEN
        JobExecution execution = launcher.run(weeklyAggregationJob, params);

        // THEN
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        List<WeeklyStatus> results = weeklyRepo.findAll();
        assertThat(results).hasSize(2);   // user1, user2

        WeeklyStatus u1 = results.stream()
                .filter(w -> w.getUserId().equals(1L))
                .findFirst()
                .orElseThrow();

        WeeklyStatus u2 = results.stream()
                .filter(w -> w.getUserId().equals(2L))
                .findFirst()
                .orElseThrow();

        // user1 합계 검증
        assertThat(u1.getLoginCount()).isEqualTo(1L + 2L + 3L);
        assertThat(u1.getViewCount()).isEqualTo(10L + 20L + 30L);
        assertThat(u1.getOrderCount()).isEqualTo(1L + 2L + 3L);
        assertThat(u1.getYear()).isEqualTo(year);
        assertThat(u1.getWeekOfYear()).isEqualTo(week);

        // user2 검증
        assertThat(u2.getLoginCount()).isEqualTo(5L);
        assertThat(u2.getViewCount()).isEqualTo(50L);
        assertThat(u2.getOrderCount()).isEqualTo(5L);
        assertThat(u2.getYear()).isEqualTo(year);
        assertThat(u2.getWeekOfYear()).isEqualTo(week);
    }
}