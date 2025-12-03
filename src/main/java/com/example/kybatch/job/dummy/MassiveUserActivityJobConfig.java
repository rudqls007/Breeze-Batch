package com.example.kybatch.job.dummy;

import com.example.kybatch.domain.activity.UserActivity;
import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.domain.user.User;
import com.example.kybatch.domain.user.UserRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class MassiveUserActivityJobConfig {

    private final UserRepository userRepository;
    private final UserActivityRepository activityRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    /**
     * 🧩 대량 UserActivity Dummy 생성 Job
     * - 지난 30일 간
     * - 모든 유저에 대해
     * - 하루 50~200건의 랜덤 활동 내역 생성
     */
    @Bean
    public Job massiveUserActivityJob(Step massiveActivityStep) {
        return new JobBuilder("massiveUserActivityJob", jobRepository)
                .start(massiveActivityStep)
                .build();
    }

    /**
     * 🧩 대량 활동 로그 생성 Step (Tasklet)
     * Reader/Writer 기반이 아닌 순수 생성/반복 로직이므로 Tasklet 사용
     */
    @Bean
    public Step massiveActivityStep() {
        return new StepBuilder("massiveActivityStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    //
                    // 1. 유저 전체 조회
                    // ----------------------------------------------------
                    //
                    List<User> users = userRepository.findAll();
                    if (users.isEmpty()) return RepeatStatus.FINISHED;


                    //
                    // 2. 날짜 범위 설정 (최근 30일)
                    // ----------------------------------------------------
                    //
                    LocalDate start = LocalDate.now().minusDays(30);
                    LocalDate end = LocalDate.now();

                    Random random = new Random();

                    // Batch Insert buffer & size 설정
                    List<UserActivity> buffer = new ArrayList<>();
                    int batchSize = 5000; // 대량 처리 고려한 큰 청크 사이즈


                    //
                    // 3. 날짜 루프 (start → end)
                    // ----------------------------------------------------
                    //
                    LocalDate cursor = start;

                    while (!cursor.isAfter(end)) {

                        //
                        // 3-1. 전체 유저 루프
                        // ------------------------------------------------
                        //
                        for (User user : users) {

                            //
                            // 3-2. 하루당 활동 개수 랜덤 생성 (50~200)
                            // ------------------------------------------------
                            //
                            int perDay = random.nextInt(150) + 50; // 50~200건


                            //
                            // 3-3. 유저별 활동 로그 생성 루프
                            // ------------------------------------------------
                            //
                            for (int i = 0; i < perDay; i++) {

                                // createdAt → 해당 날짜의 랜덤 시간
                                LocalDateTime createdAt = cursor.atStartOfDay()
                                        .plusHours(random.nextInt(24))
                                        .plusMinutes(random.nextInt(60));

                                //
                                // UserActivity 엔티티 생성
                                //
                                buffer.add(UserActivity.builder()
                                        .userId(user.getId())             // 외래키 직접 저장
                                        .user(user)                       // 연관관계 엔티티 저장
                                        .loginCount(random.nextInt(2))    // 0~1
                                        .viewCount(random.nextInt(20))    // 0~19
                                        .orderCount(random.nextInt(3))    // 0~2
                                        .createdAt(createdAt)
                                        .build()
                                );

                                //
                                // 3-4. 일정량 모이면 batch insert
                                //
                                if (buffer.size() >= batchSize) {
                                    activityRepository.saveAll(buffer);
                                    buffer.clear();
                                }
                            }
                        }

                        // 날짜 +1
                        cursor = cursor.plusDays(1);
                    }


                    //
                    // 4. 마지막 잔여 데이터 flush
                    //
                    if (!buffer.isEmpty()) {
                        activityRepository.saveAll(buffer);
                    }

                    return RepeatStatus.FINISHED;
                }, tm) // 트랜잭션 매니저 지정
                .build();
    }
}
