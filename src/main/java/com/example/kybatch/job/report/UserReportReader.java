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

    /**
     * Spring Batch에서 제공하는 JPA용 Paging Reader 빌더
     * 내부에서 JpaPagingItemReader<User>를 생성함.
     * */
    public JpaPagingItemReader<User> reader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("userReportReader")
                /* JPA를 통해 DB 접근할 수 있게, EntityManagerFactory 주입 */
                .entityManagerFactory(emf)
                /* JPQL로 User 엔티티 전체를 조회 */
                .queryString("SELECT u FROM User u ORDER BY u.id ASC")
                /* Reader가 한 번에 DB에서 가져오는 “페이지 크기” */
                .pageSize(10)
                .build();
    }
}
