package com.example.kybatch.job.aggregation.daily;

import com.example.kybatch.domain.activity.UserActivity;
import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DailyAggregationJobTest {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job dailyAggregationJob;

    @Autowired
    UserActivityRepository activityRepository;

    @Autowired
    DailyStatusRepository dailyStatusRepository;

    LocalDate targetDate;

    @BeforeEach
    void setup() {
        dailyStatusRepository.deleteAll();
        activityRepository.deleteAll();

        targetDate = LocalDate.of(2025, 12, 3);

        // userId = 1 로그 두 개
        activityRepository.save(UserActivity.builder()
                .userId(1L)
                .loginCount(1)
                .viewCount(5)
                .orderCount(2)
                .createdAt(targetDate.atTime(10, 0))
                .build());

        activityRepository.save(UserActivity.builder()
                .userId(1L)
                .loginCount(2)
                .viewCount(4)
                .orderCount(1)
                .createdAt(targetDate.atTime(15, 0))
                .build());

        // userId = 2 로그 하나
        activityRepository.save(UserActivity.builder()
                .userId(2L)
                .loginCount(3)
                .viewCount(1)
                .orderCount(1)
                .createdAt(targetDate.atTime(11, 30))
                .build());
    }

    @Test
    void testDailyAggregationJob() throws Exception {

        // -------------------------------
        // 1. Job 파라미터 구성
        // -------------------------------
        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", targetDate.toString())
                .addLong("time", System.currentTimeMillis()) // 중복 실행 방지
                .toJobParameters();

        // -------------------------------
        // 2. Job 실행
        // -------------------------------
        JobExecution execution = jobLauncher.run(dailyAggregationJob, params);

        assertThat(execution.getExitStatus().getExitCode())
                .isEqualTo("COMPLETED");

        // -------------------------------
        // 3. 결과 검증
        // -------------------------------
        assertThat(dailyStatusRepository.count()).isEqualTo(2); // userId=1, userId=2

        DailyStatus user1 = dailyStatusRepository.findByUserIdAndDate(1L, targetDate).orElseThrow();
        DailyStatus user2 = dailyStatusRepository.findByUserIdAndDate(2L, targetDate).orElseThrow();

        // user 1 합산 값
        assertThat(user1.getLoginCount()).isEqualTo(3);
        assertThat(user1.getViewCount()).isEqualTo(9);
    }
}