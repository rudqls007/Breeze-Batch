package com.example.kybatch.api.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DummyBatchController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    /**
     * 🔥 대량 UserActivity Dummy 생성
     *
     * ✔ dev / test 전용
     * ✔ Raw 로그(user_activity) 생성
     * ✔ Daily / Weekly / Monthly 집계 검증용
     */
    @PostMapping("/batch/dummy/user-activity/run")
    public String runUserActivityDummy() throws Exception {

        Job job = jobRegistry.getJob("massiveUserActivityJob");

        JobParameters params = new JobParametersBuilder()
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        jobLauncher.run(job, params);

        log.info("[API] Massive UserActivity dummy batch triggered");
        return "UserActivity dummy batch started";
    }
}
