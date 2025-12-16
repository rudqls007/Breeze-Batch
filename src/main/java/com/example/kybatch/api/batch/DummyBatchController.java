package com.example.kybatch.api.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DummyBatchController {

    private final JobLauncher jobLauncher;
    private final Job massiveUserActivityJob;

    /**
     * 🔥 대량 UserActivity Dummy 생성
     *
     * ✔ dev / test 전용
     * ✔ Raw 로그(user_activity) 생성
     * ✔ Daily / Weekly / Monthly 집계 검증용
     */
    @PostMapping("/batch/dummy/user-activity/run")
    public String runUserActivityDummy() throws Exception {

        JobParameters params = new JobParametersBuilder()
                // 매번 실행 가능하도록 유니크 파라미터
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(massiveUserActivityJob, params);

        log.info("[API] Massive UserActivity dummy batch triggered");
        return "UserActivity dummy batch started";
    }
}

