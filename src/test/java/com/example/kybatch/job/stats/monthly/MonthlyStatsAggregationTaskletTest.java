package com.example.kybatch.job.stats.monthly;

import com.example.kybatch.domain.stats.DailyStatusRepository;
import com.example.kybatch.domain.stats.MonthlyStatusRepository;
import com.example.kybatch.lock.BatchLockService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.assertj.core.api.Assertions.*;

@SpringBatchTest
@SpringBootTest
class MonthlyStatsAggregationTaskletTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DailyStatusRepository dailyStatusRepository;

    @Autowired
    private MonthlyStatusRepository monthlyStatusRepository;

    @Autowired
    private BatchLockService lockService;

    @Autowired
    private Job monthlyStatsAggregationJob;

    @BeforeEach
    void setup() {
        dailyStatusRepository.deleteAll();
        monthlyStatusRepository.deleteAll();
        // 락 테이블도 정리
        lockService.releaseLock("MONTHLY_STATS");
    }

    // -------------------------------------------------------------
    // 1) STEP 24-1 기능 테스트: 날짜 검증 예외 테스트
    // -------------------------------------------------------------
    @Test
    void testMonthlyAggregation_invalidStartDate_shouldThrowException() throws Exception {

        // startDate가 1일이 아님 → 예외 발생해야 함
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2025-07-02") // 잘못된 날짜
                .addString("endDate", "2025-08-01")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(monthlyStatsAggregationJob);

        assertThatThrownBy(() -> jobLauncherTestUtils.launchJob(params))
                .isInstanceOf(Exception.class);
    }

    @Test
    void testMonthlyAggregation_invalidEndDate_shouldThrowException() throws Exception {

        // endDate가 startDate + 1 month가 아님
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2025-07-01")
                .addString("endDate", "2025-07-30") // 잘못된 날짜
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(monthlyStatsAggregationJob);

        assertThatThrownBy(() -> jobLauncherTestUtils.launchJob(params))
                .isInstanceOf(Exception.class);
    }

    // -------------------------------------------------------------
    // 2) STEP 24-1 기능 테스트: Lock 기능 테스트
    // -------------------------------------------------------------
    @Test
    void testMonthlyAggregation_lockShouldPreventDuplicateExecution() throws Exception {

        // 먼저 Lock을 인위적으로 잡는다
        boolean acquired = lockService.acquireLock("MONTHLY_STATS");
        assertThat(acquired).isTrue();

        // 이제 본 배치는 Lock 때문에 SKIP 되어야 함
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2025-07-01")
                .addString("endDate", "2025-08-01")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(monthlyStatsAggregationJob);

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        // SKIP되면 COMPLETED이지만 MonthlyStatus에는 아무 데이터도 없어야 한다.
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        assertThat(monthlyStatusRepository.findAll()).isEmpty();

        // 마지막으로 락 해제해서 클린업
        lockService.releaseLock("MONTHLY_STATS");
    }

}
