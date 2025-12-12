package com.example.kybatch.job.stats.monthly;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.domain.stats.MonthlyStatusRepository;

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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MonthlyStatsAggregationJob 통합 테스트
 * ---------------------------------------
 * 1. DailyStatus 더미 데이터 DB insert
 * 2. startDate(2025-07-01) / endDate(2025-08-01) JobParameter 전달
 * 3. MonthlyStatsAggregationBatch 실행
 * 4. MonthlyStatus DB 결과값 검증
 * 5. deleteByYearAndMonth 동작 검증 (이전 통계 삭제 후 재삽입)
 */
@SpringBatchTest
@SpringBootTest
class MonthlyStatsAggregationJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DailyStatusRepository dailyStatusRepository;

    @Autowired
    private MonthlyStatusRepository monthlyStatusRepository;

    @Autowired
    private Job monthlyStatsAggregationJob;

    @BeforeEach
    void setup() {
        dailyStatusRepository.deleteAll();
        monthlyStatusRepository.deleteAll();
    }

    @Test
    void testMonthlyAggregationJob() throws Exception {

        // ---------------------------------------------------------
        // 1) 테스트 데이터 준비
        // ---------------------------------------------------------
        LocalDate startOfMonth = LocalDate.of(2025, 7, 1);
        LocalDate startOfNextMonth = LocalDate.of(2025, 8, 1);

        // userId = 1
        dailyStatusRepository.save(new DailyStatus(1L, startOfMonth.plusDays(0), 3L, 1L, 2L));
        dailyStatusRepository.save(new DailyStatus(1L, startOfMonth.plusDays(5), 2L, 4L, 3L));
        dailyStatusRepository.save(new DailyStatus(1L, startOfMonth.plusDays(10), 1L, 2L, 1L));

        // userId = 2
        dailyStatusRepository.save(new DailyStatus(2L, startOfMonth.plusDays(3), 4L, 3L, 2L));
        dailyStatusRepository.save(new DailyStatus(2L, startOfMonth.plusDays(15), 5L, 1L, 0L));

        // ---------------------------------------------------------
        // 2) JobParameters 설정
        // ---------------------------------------------------------
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", startOfMonth.toString())
                .addString("endDate", startOfNextMonth.toString()) // 미포함
                .toJobParameters();

        jobLauncherTestUtils.setJob(monthlyStatsAggregationJob);

        // ---------------------------------------------------------
        // 3) Job 실행
        // ---------------------------------------------------------
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getExitStatus().getExitCode())
                .isEqualTo("COMPLETED");

        // ---------------------------------------------------------
        // 4) 결과 검증
        // ---------------------------------------------------------
        List<MonthlyStatus> list = monthlyStatusRepository.findAll();
        assertThat(list).hasSize(2); // user 2명

        MonthlyStatus u1 = list.stream().filter(m -> m.getUserId() == 1L).findFirst().get();
        MonthlyStatus u2 = list.stream().filter(m -> m.getUserId() == 2L).findFirst().get();

        // user1 검증
        assertThat(u1.getYear()).isEqualTo(2025);
        assertThat(u1.getMonth()).isEqualTo(7);
        assertThat(u1.getLoginCount()).isEqualTo(3 + 2 + 1);
        assertThat(u1.getViewCount()).isEqualTo(1 + 4 + 2);
        assertThat(u1.getOrderCount()).isEqualTo(2 + 3 + 1);

        // user2 검증
        assertThat(u2.getYear()).isEqualTo(2025);
        assertThat(u2.getMonth()).isEqualTo(7);
        assertThat(u2.getLoginCount()).isEqualTo(4 + 5);
        assertThat(u2.getViewCount()).isEqualTo(3 + 1);
        assertThat(u2.getOrderCount()).isEqualTo(2 + 0);

        System.out.println("✔ Monthly aggregation job test SUCCESS");
    }
}
