package com.example.kybatch.api.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * StatsBatchController
 * ---------------------
 * - 통계 집계 배치 실행 전용 Controller
 * - Daily / Weekly / Monthly 통계 Job을 수동으로 트리거
 *
 * 사용 목적
 * 1) dev 환경에서 배치 직접 실행 테스트
 * 2) 운영에서는 Scheduler가 내부적으로 호출
 *
 * 주의
 * - 데이터 생성(Dummy)은 여기서 절대 다루지 않는다
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Profile({"dev", "batch"})
public class StatsBatchController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    /**
     * Daily 통계 배치 실행
     * - user_activity → daily_status 집계
     */
    @PostMapping("/batch/stats/daily/run")
    public String runDaily() throws Exception {

        runJob("dailyStatsAggregationJob");

        log.info("[API] Daily stats batch triggered");
        return "Daily stats batch started";
    }

    /**
     * Weekly 통계 배치 실행
     * - daily_status → weekly_status 집계
     * - 기준: 지난 주 (월 ~ 일)
     */
    @PostMapping("/batch/stats/weekly/run")
    public String runWeekly() throws Exception {

        runJob("weeklyStatsAggregationJob");

        log.info("[API] Weekly stats batch triggered");
        return "Weekly stats batch started";
    }

    /**
     * Monthly 통계 배치 실행
     * - daily_status → monthly_status 집계
     * - 기준: 지난 달
     */
    @PostMapping("/batch/stats/monthly/run")
    public String runMonthly() throws Exception {

        runJob("monthlyStatsAggregationJob");

        log.info("[API] Monthly stats batch triggered");
        return "Monthly stats batch started";
    }

    /**
     * 공통 Job 실행 로직
     * - JobRegistry에서 Job 조회
     * - runAt 파라미터로 매 실행을 유니크하게 보장
     */
    private void runJob(String jobName) throws Exception {

        Job job = jobRegistry.getJob(jobName);

        JobParameters params = new JobParametersBuilder()
                .addLocalDateTime("runAt", LocalDateTime.now())
 //               .addString("forceFail", "Y")
                .toJobParameters();

        jobLauncher.run(job, params);
    }
}
