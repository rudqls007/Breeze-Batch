package com.example.kybatch.job.logging;

import com.example.kybatch.domain.batchlog.BatchJobLogRepository;
import com.example.kybatch.domain.batchlog.BatchStepLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BatchLoggingIntegrationTest {

    @Autowired
    JobLauncher launcher;

    @Autowired
    Job fullAutoPipelineJob;

    @Autowired
    BatchJobLogRepository jobLogRepository;

    @Autowired
    BatchStepLogRepository stepLogRepository;

    @Test
    void fullAutoPipelineJob_실행_시_Job과_Step_로그가_DB에_저장된다() throws Exception {
        // given
        JobParameters params = new JobParametersBuilder()
                .addLong("testTime", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution execution = launcher.run(fullAutoPipelineJob, params);

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 최소 1개 이상의 Job 로그가 쌓였는지
        assertThat(jobLogRepository.count()).isGreaterThan(0);

        // 최소 1개 이상의 Step 로그가 쌓였는지
        assertThat(stepLogRepository.count()).isGreaterThan(0);
    }
}
