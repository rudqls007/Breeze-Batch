package com.example.kybatch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * ==========================================
 * BatchConfig (STEP 1)
 * ------------------------------------------
 * - Spring Batch 기능을 활성화하는 설정 클래스.
 * - @EnableBatchProcessing을 통해
 *   JobRepository, JobLauncher 등 배치 핵심 컴포넌트들이
 *   자동으로 스프링 컨테이너에 등록된다.
 *
 * [중요 포인트]
 * - 여기서는 별도 메서드를 만들지 않아도 됨.
 * - "배치 프로젝트입니다" 라고 스프링에게 알려주는 역할.
 * ==========================================
 */
@Configuration          // 이 클래스는 설정 클래스야(Bean 정의하는 곳)
@EnableBatchProcessing  // Spring Batch 인프라 활성화
public class BatchConfig {
    // STEP 2 이후에 필요하면 여기에 커스텀 설정을 추가할 예정.
}
