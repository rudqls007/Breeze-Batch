package com.example.kybatch.job.daily;

import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.dto.DailyAggregationDTO;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;


/**
 * DailyAggregationJobConfig
 * ------------------------------------------------------------
 * 목적:
 *   - UserActivityLog 테이블에 쌓여 있는 사용자 이벤트 데이터를 기준으로
 *     "어제 날짜"의 Daily 통계(DailyStatus)를 생성하는 배치 Job.
 *
 * 처리 흐름:
 *   [Reader]    어제 날짜 기준 UserActivity 데이터를 읽어서 DailyAggregationDTO로 변환
 *   [Processor] DTO → DailyStatus 엔티티로 매핑
 *   [Writer]    JPA 기반으로 DailyStatus 테이블에 insert
 *
 * 구성 요소:
 *   - Reader:  UserActivityRepository 로부터 어제 날짜의 이벤트 집계 조회
 *   - Processor: DTO → 엔티티 변환
 *   - Writer: JPA(EntityManagerFactory) 기반으로 DailyStatus 저장
 *
 * 배치 시나리오 예:
 *   1) 매일 새벽 2시에 실행되며
 *   2) 어제 날짜의 이벤트 로그를 읽어서
 *   3) DailyStatus 테이블에 저장
 */
@Configuration
@RequiredArgsConstructor
public class DailyAggregationStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;
    private final UserActivityRepository  userActivityRepository;

    /**
     * 실제로는 JobParameter로 날짜를 받거나,
     * 기본 값으로 어제 날짜를 사용하게 할 수 있음.
     * */
    private LocalDate defaultTargetDate(){
        /* 어제 기준 집계 */
       return  LocalDate.now().minusDays(1);
    }

    /**
     * DailyAggregation Job 구성
     * ------------------------------------------------------------
     * - 스텝 하나로 구성된 단일 Job
     * - Step: dailyAggregationStep()
     */
    @Bean
    public Job dailyAggregationJob(){
        return new JobBuilder("dailyAggregationJob", jobRepository)
                .start(dailyAggregationStep())
                .build();
    }

    /**
     * Step 구성
     * ------------------------------------------------------------
     * Chunk 기반 처리 흐름:
     *   Reader → Processor → Writer
     *
     * Chunk Size = 100
     *   - Reader가 DailyAggregationDTO 100개 읽고
     *   - Processor로 변환 후
     *   - Writer가 한 트랜잭션으로 100개 저장
     *
     * 트랜잭션 경계:
     *   - chunk(100) 동작 시, 트랜잭션 매니저(transactionManager) 기준으로
     *     100건 단위로 Commit 수행
     */
    @Bean
    public Step dailyAggregationStep() {

        return new StepBuilder("dailyAggregationStep", jobRepository)
                .<DailyAggregationDTO, DailyStatus>chunk(100, transactionManager)
                .reader(dailyAggregationReader())
                .processor(dailyAggregationProcessor())
                .writer(dailyAggregationWriter())
                .build();

    }

    /**
     * Writer
     * ------------------------------------------------------------
     * 역할:
     *   - DailyStatus 엔티티를 JPA 기반으로 DB에 저장
     *   - 내부적으로 EntityManager를 생성하여 persist 처리
     *
     * EntityManagerFactory가 필요한 이유:
     *   - JPA는 EntityManagerFactory → EntityManager 로 DB 접근
     *   - Chunk(100) 단위 트랜잭션마다 새로운 EntityManager 생성하여 저장
     */
    @Bean
    public DailyAggregationWriter dailyAggregationWriter() {
        return new DailyAggregationWriter(emf);
    }

    /**
     * Processor
     * ------------------------------------------------------------
     * 역할:
     *   - Reader가 반환한 DailyAggregationDTO를
     *     저장 가능한 DailyStatus 엔티티로 변환
     *
     * 주 목적:
     *   - DTO → Entity 전환 (순수 매핑 책임)
     */
    @Bean
    public DailyAggregationProcessor dailyAggregationProcessor() {
        return new DailyAggregationProcessor();
    }

    /**
     * Reader
     * ------------------------------------------------------------
     * 역할:
     *   - UserActivityRepository를 사용하여
     *     어제 날짜의 활동 로그를 집계하여 DTO 형태로 반환
     *
     * 반환 타입:
     *   - DailyAggregationDTO
     *     (집계된 값: 로그인 수, 조회 수, 주문 수 등)
     */
    @Bean
    public DailyAggregationReader dailyAggregationReader() {
        return new DailyAggregationReader(defaultTargetDate(), userActivityRepository);
    }
}
