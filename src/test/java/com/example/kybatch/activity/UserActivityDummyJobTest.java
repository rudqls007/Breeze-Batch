package com.example.kybatch.activity;

import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.domain.user.User;
import com.example.kybatch.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserActivityDummyJobTest {

    @Autowired
    JobLauncher launcher;

    @Autowired
    Job userActivityDummyJob;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserActivityRepository activityRepository;

    @BeforeEach
    void setup() {
        activityRepository.deleteAll();

        if (userRepository.count() == 0) {
            userRepository.save(new User("TestUser1", "test1@example.com", "ACTIVE"));
            userRepository.save(new User("TestUser2", "test2@example.com", "ACTIVE"));

        }
    }

    @Test
    void createDummyActivities() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2025-11-01")
                .addString("endDate", "2025-11-03")
                .addLong("activityPerUserPerDay", 5L)
                .toJobParameters();

        launcher.run(userActivityDummyJob, params);

        long count = activityRepository.count();

        assertThat(count).isGreaterThan(10L);  // 대략적 검증
    }
}

