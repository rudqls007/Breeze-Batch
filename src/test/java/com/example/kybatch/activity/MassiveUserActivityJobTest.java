package com.example.kybatch.activity;

import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.domain.user.User;
import com.example.kybatch.domain.user.UserRepository;
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
class MassiveUserActivityJobTest {

    @Autowired
    JobLauncher launcher;

    @Autowired
    Job massiveUserActivityJob;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserActivityRepository activityRepository;


    @BeforeEach
    void initDummyUsers() {
        // 매 테스트마다 초기화
        activityRepository.deleteAll();
        userRepository.deleteAll();

        // 더미 유저 3명 생성
        userRepository.save(new User("A", "a@test.com", "ACTIVE"));
        userRepository.save(new User("B", "b@test.com", "ACTIVE"));
        userRepository.save(new User("C", "c@test.com", "ACTIVE"));
    }


    @Test
    void generateMassiveActivities() throws Exception {

        // -------------------------------
        // 1. Job 파라미터 구성
        // -------------------------------
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis()) // 동일 파라미터 실행 방지
                .toJobParameters();


        // -------------------------------
        // 2. Job 실행
        // -------------------------------
        JobExecution execution = launcher.run(massiveUserActivityJob, params);
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");


        // -------------------------------
        // 3. 실행 후 user_activity 검증
        // -------------------------------
        long count = activityRepository.count();

        System.out.println("=== MassiveActivity Generated Count ===");
        System.out.println("Total Activity Logs = " + count);

        // 유저 3명 × 30일 × 최소 50건 = 4,500건 이상 보장
        assertThat(count).isGreaterThan(4000);


        // -------------------------------
        // 4. 날짜 분포 검증
        // -------------------------------
        LocalDate minDate = activityRepository.findMinCreatedAt().toLocalDate();
        LocalDate maxDate = activityRepository.findMaxCreatedAt().toLocalDate();

        assertThat(minDate).isEqualTo(LocalDate.now().minusDays(30));
        assertThat(maxDate).isEqualTo(LocalDate.now());
    }
}