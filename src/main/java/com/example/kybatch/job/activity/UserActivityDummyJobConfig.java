package com.example.kybatch.job.activity;

import com.example.kybatch.domain.activity.UserActivity;
import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.domain.user.User;
import com.example.kybatch.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserActivityDummyJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tm;

    private final UserRepository userRepository;
    private final UserActivityRepository userActivityRepository;

    /**
     * 🧩 UserActivity Dummy Data 생성 Job
     * - 날짜 범위(startDate ~ endDate)
     * - 유저 전체
     * - 하루 perDay N건씩 더미 활동데이터 생성
     */
    @Bean
    public Job userActivityDummyJob(Step userActivityDummyStep) {
        return new JobBuilder("userActivityDummyJob", jobRepository)
                .start(userActivityDummyStep) // 단일 Step Job
                .build();
    }

    /**
     * 🧩 Tasklet Step
     * → Reader/Processor/Writer 구조가 아닌
     *   단순 반복(Loop) 기반의 로직을 실행해야 할 때 적합
     */
    @Bean
    public Step userActivityDummyStep() {
        return new org.springframework.batch.core.step.builder.StepBuilder("userActivityDummyStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    //
                    // 1. Job Parameter 파싱
                    // ----------------------------------------------------
                    // 실행 시 JobParameters 로부터 startDate, endDate, perDay 등을 읽어옴
                    //
                    var params = chunkContext.getStepContext()
                            .getStepExecution()
                            .getJobParameters();

                    String start = params.getString("startDate");   // yyyy-MM-dd
                    String end = params.getString("endDate");       // yyyy-MM-dd
                    Long perDay = params.getLong("activityPerUserPerDay", 5L); // 기본값 5

                    LocalDate startDate = LocalDate.parse(start);
                    LocalDate endDate = LocalDate.parse(end);

                    log.info("[UserActivityDummyJob] start={}, end={}, perDay={}",
                            startDate, endDate, perDay);


                    //
                    // 2. 유저 전체 조회
                    // ----------------------------------------------------
                    // 더미 데이터는 모든 사용자 대상으로 생성해야함
                    //
                    List<User> users = userRepository.findAll();
                    if (users.isEmpty()) {
                        log.warn("No users found. Skipping dummy creation.");
                        return RepeatStatus.FINISHED;
                    }


                    //
                    // 3. 랜덤 데이터 생성에 필요한 준비
                    // ----------------------------------------------------
                    //
                    Random random = new Random();
                    List<UserActivity> buffer = new ArrayList<>();  // batch insert buffer
                    int batchSize = 1000; // 청크 DB save 용량

                    LocalDate cursor = startDate; // 날짜 이동 포인터


                    //
                    // 4. 날짜 루프 (start → end)
                    // ----------------------------------------------------
                    //
                    while (!cursor.isAfter(endDate)) {

                        //
                        // 4-1. 유저 단위 루프
                        // ------------------------------------------------
                        //
                        for (User user : users) {

                            //
                            // 4-2. 유저 1명당 perDay 개수 활동 로그 생성
                            // ------------------------------------------------
                            //
                            for (int i = 0; i < perDay; i++) {

                                //
                                // createdAt 랜덤 생성 (해당 날짜 중 임의의 시간)
                                //
                                LocalDateTime createdAt = cursor.atStartOfDay()
                                        .plusHours(random.nextInt(24))
                                        .plusMinutes(random.nextInt(60));

                                //
                                // 랜덤 카운트 생성 (로그인, 조회수, 주문)
                                //
                                int login = random.nextDouble() < 0.2 ? 1 : 0; // 20% 확률 로그인
                                int view = random.nextInt(21);                // 0~20 랜덤 조회수
                                int order = random.nextDouble() < 0.8 ? 0 : random.nextInt(4); // 낮은 확률 주문 발생


                                //
                                // 4-3. UserActivity 엔티티 생성
                                //
                                UserActivity activity = UserActivity.builder()
                                        .userId(user.getId())      // 외래키 FK 직접 기록
                                        .user(user)                // 엔티티 연관관계도 저장
                                        .createdAt(createdAt)
                                        .loginCount(login)
                                        .viewCount(view)
                                        .orderCount(order)
                                        .build();

                                buffer.add(activity);

                                //
                                // 4-4. 일정량 모이면 배치 insert
                                //
                                if (buffer.size() >= batchSize) {
                                    userActivityRepository.saveAll(buffer);
                                    buffer.clear();
                                }
                            }
                        }

                        // 날짜 +1
                        cursor = cursor.plusDays(1);
                    }

                    //
                    // 5. 마지막 남은 데이터 flush
                    //
                    if (!buffer.isEmpty()) {
                        userActivityRepository.saveAll(buffer);
                    }

                    log.info("[UserActivityDummyJob] Completed.");

                    return RepeatStatus.FINISHED;
                }, tm)  // Tasklet 트랜잭션 매니저 등록
                .build();
    }

}
