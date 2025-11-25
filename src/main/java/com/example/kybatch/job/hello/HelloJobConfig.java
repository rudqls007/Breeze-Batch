package com.example.kybatch.job.hello;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ==========================================
 * HelloJobConfig (STEP 1)
 * ------------------------------------------
 * [이 클래스의 역할]
 * - "어떤 Job을 어떤 Step으로 구성할지"를 정의하는 설정 클래스.
 * - Spring Batch 5.0 스타일의 Job/Step 생성 방식 사용.
 *
 * [실행 흐름 개념]
 * 1) 스프링부트가 실행되면서 이 클래스가 로딩된다.
 * 2) @Bean 메서드들을 호출해서 Job/Step Bean이 컨테이너에 등록된다.
 * 3) 애플리케이션 실행 시
 *    --spring.batch.job.name=helloJob 을 전달하면
 *    스프링이 이름이 "helloJob"인 Job Bean을 찾아 실행한다.
 * 4) helloJob → helloStep → HelloTasklet.execute() 순서로 호출된다.
 * ==========================================
 */
@Configuration                // 설정 클래스임을 표시
@RequiredArgsConstructor      // final 필드(HelloTasklet)를 생성자로 자동 주입
public class HelloJobConfig {

    private final HelloTasklet helloTasklet;

    /**
     * [Job Bean 정의]
     * - "helloJob" 이라는 이름의 배치 작업(Job)을 정의한다.
     * - JobRepository를 통해 이 Job의 실행 이력/상태가 DB에 저장된다.
     *
     * @param jobRepository Job 메타데이터를 저장하는 저장소(Spring이 자동 주입)
     * @param helloStep    아래에서 정의한 Step Bean
     * @return Job 객체
     */
    @Bean
    public Job helloJob(JobRepository jobRepository, Step helloStep) {
        return new JobBuilder("helloJob", jobRepository)
                .start(helloStep)
                .build();
    }


    /**
     * [Step Bean 정의]
     * - "helloStep" 이라는 이름의 Step을 정의한다.
     * - Tasklet 방식으로 동작하며, 트랜잭션 매니저를 함께 사용한다.
     *
     * @param jobRepository Job/Step 실행 정보를 DB에 기록하는 저장소
     * @param txManager     트랜잭션 관리 (롤백/커밋 담당)
     * @return Step 객체
     */
    @Bean
    public Step helloStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("helloStep", jobRepository)
                // Tasklet + 트랜잭션 매니저를 넣어 Step 구성
                .tasklet(helloTasklet, txManager)
                .build();
    }
}
