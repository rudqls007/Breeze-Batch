package com.example.kybatch.job.dummy;

import com.example.kybatch.domain.user.User;
import com.example.kybatch.domain.user.UserRepository;
import com.example.kybatch.job.listener.JobExecutionLoggingListener;
import com.example.kybatch.job.listener.StepExecutionLoggingListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class UserDummyJobConfig {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    private final JobExecutionLoggingListener jobExecutionLoggingListener;
    private final StepExecutionLoggingListener stepExecutionLoggingListener;

    /**
     * 🧩 User Dummy Data 생성 Job
     * - 단일 Step(generateUsersStep)을 실행함
     * - 테스트용 유저 5,000명 생성에 활용
     */
    @Bean
    public Job userDummyJob(Step generateUsersStep) {
        return new JobBuilder("userDummyJob", jobRepository)
                .listener(jobExecutionLoggingListener)
                .start(generateUsersStep)
                .build();
    }

    /**
     * 🧩 User 더미 생성 Step (Tasklet 기반)
     * Reader/Processor/Writer가 필요 없는 단순 반복 로직이기 때문에 Tasklet이 적합
     */
    @Bean
    public Step generateUsersStep() {
        return new StepBuilder("generateUsersStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    //
                    // 1. 생성할 유저 수 및 기본 설정
                    // ----------------------------------------------------
                    //
                    int size = 5000; // 총 생성할 유저 수
                    List<User> buffer = new ArrayList<>(); // batch insert buffer
                    Random random = new Random();          // 랜덤 활성/비활성 상태 생성용


                    //
                    // 2. 유저 생성 메인 루프
                    // ----------------------------------------------------
                    //
                    for (int i = 1; i <= size; i++) {

                        // 유저 이름 → User_00001, User_00002 ...
                        String name = "User_" + String.format("%05d", i);

                        // 이메일 → user1@example.com
                        String email = "user" + i + "@example.com";

                        // 상태 랜덤 부여 (90% ACTIVE, 10% INACTIVE)
                        String status = random.nextDouble() < 0.9 ? "ACTIVE" : "INACTIVE";


                        //
                        // 2-1. User 엔티티 생성 후 buffer에 삽입
                        //
                        buffer.add(new User(name, email, status));


                        //
                        // 2-2. 1000건 단위로 Batch Insert
                        //     DB 성능 최적화 목적
                        //
                        if (buffer.size() >= 1000) {
                            userRepository.saveAll(buffer); // bulk insert
                            buffer.clear();                 // buffer 초기화
                        }
                    }


                    //
                    // 3. 루프 종료 후, 잔여 데이터 마지막 flush
                    //
                    if (!buffer.isEmpty()) {
                        userRepository.saveAll(buffer);
                    }

                    // Step 정상 종료
                    return RepeatStatus.FINISHED;

                }, tm)  // tasklet 트랜잭션 매니저 지정
                .listener(stepExecutionLoggingListener)
                .build();
    }
}
