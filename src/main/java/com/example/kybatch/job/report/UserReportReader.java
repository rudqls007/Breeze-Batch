package com.example.kybatch.job.report;

import com.example.kybatch.domain.user.User;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;

/**
 * STEP 5 - JPA 기반 데이터 읽기
 * --------------------------------------------------
 * - JpaPagingItemReader 를 사용해 users 테이블을 페이징으로 조회한다.
 * - 대량 데이터에서도 안정적인 페이징 기반 Batch 처리 가능.
 * - JPQL 기반 쿼리 사용 (select u from User u)
 */
@Component
@RequiredArgsConstructor
public class UserReportReader {

    private final EntityManagerFactory emf;

    public JpaPagingItemReader<User> reader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("userReportReader")
                .entityManagerFactory(emf)
                .queryString("SELECT u FROM USER u ORDER BY u.id ASC")
                .pageSize(10)
                .build();
    }
}
