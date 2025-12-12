package com.example.kybatch.job.stats.weekly;

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
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
class WeeklyStatsAggregationJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DailyStatusRepository dailyStatusRepository;

    @Autowired
    private WeeklyStatusRepository weeklyStatusRepository;

    @Autowired
    private Job weeklyStatsAggregationJob;

    @BeforeEach
    void setup() {
        weeklyStatusRepository.deleteAll();
        dailyStatusRepository.deleteAll();
    }

    @Test
    void testWeeklyAggregationJob() throws Exception {

        // ------------------------------------------------------
        // 1) 테스트 데이터 준비
        // ------------------------------------------------------
        // 주: 2025년 1월 6일(월) ~ 1월 12일(일)
        // endDate는 미포함 → 1월 13일
        LocalDate startDate = LocalDate.of(2025, 1, 6);
        LocalDate endDate   = LocalDate.of(2025, 1, 13);

        // userId = 1번
        dailyStatusRepository.save(new DailyStatus(1L, startDate.plusDays(0), 3L, 1L, 2L));
        dailyStatusRepository.save(new DailyStatus(1L, startDate.plusDays(1), 2L, 4L, 0L));
        dailyStatusRepository.save(new DailyStatus(1L, startDate.plusDays(2), 5L, 2L, 1L));

        // userId = 2번
        dailyStatusRepository.save(new DailyStatus(2L, startDate.plusDays(0), 1L, 1L, 1L));
        dailyStatusRepository.save(new DailyStatus(2L, startDate.plusDays(3), 4L, 3L, 2L));

        // ------------------------------------------------------
        // 2) JobParameter 설정 후 Job 실행
        // ------------------------------------------------------
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", startDate.toString())
                .addString("endDate", endDate.toString()) // 미포함
                .toJobParameters();

        jobLauncherTestUtils.setJob(weeklyStatsAggregationJob);

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");


        // ------------------------------------------------------
        // 3) DB 결과 검증
        // ------------------------------------------------------
        List<WeeklyStatus> list = weeklyStatusRepository.findAll();

        assertThat(list).hasSize(2);  // 유저 2명

        WeeklyStatus u1 = list.stream().filter(ws -> ws.getUserId() == 1L).findFirst().get();
        WeeklyStatus u2 = list.stream().filter(ws -> ws.getUserId() == 2L).findFirst().get();

        // user1: login = 3+2+5 = 10
        assertThat(u1.getLoginCount()).isEqualTo(10);
        assertThat(u1.getViewCount()).isEqualTo(1 + 4 + 2);
        assertThat(u1.getOrderCount()).isEqualTo(2 + 0 + 1);

        // user2: login = 1 + 4 = 5
        assertThat(u2.getLoginCount()).isEqualTo(5);
        assertThat(u2.getViewCount()).isEqualTo(1 + 3);
        assertThat(u2.getOrderCount()).isEqualTo(1 + 2);

        // 4) 날짜 검증
        assertThat(u1.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 6));
        assertThat(u1.getEndDate()).isEqualTo(LocalDate.of(2025, 1, 12)); // endDate - 1Day

        System.out.println("✔ Weekly aggregation job 테스트 성공");
    }
}
