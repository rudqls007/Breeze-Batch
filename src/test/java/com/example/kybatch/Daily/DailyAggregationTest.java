package com.example.kybatch.Daily;

import com.example.kybatch.domain.activity.UserActivity;
import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.dto.DailyAggregationDTO;
import com.example.kybatch.service.DailyActivityAggregationService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class DailyAggregationTest {

    @Autowired
    EntityManager em;



    @Test
    void showTables() {
        List<Object[]> result = em.createNativeQuery("SHOW TABLES").getResultList();
        System.out.println(result);
    }

    @Autowired
    UserActivityRepository repo;

    @Autowired
    DailyActivityAggregationService service;

    @Test
    void dailyAggregationTest() {

        // given - Raw 로그 저장
        repo.save(UserActivity.builder()
                .userId(1L)
                .loginCount(1)
                .viewCount(3)
                .orderCount(2)
                .createdAt(LocalDateTime.of(2025, 11, 26, 10, 30))
                .build());

        repo.save(UserActivity.builder()
                .userId(1L)
                .loginCount(2)
                .viewCount(1)
                .orderCount(1)
                .createdAt(LocalDateTime.of(2025, 11, 26, 14, 10))
                .build());

        // when
        List<DailyAggregationDTO> result =
                service.aggregateDaily(LocalDate.of(2025, 11, 26));

        // then
        assert result.size() == 1;
        DailyAggregationDTO dto = result.get(0);

        assert dto.getLoginCount() == 3;
        assert dto.getViewCount() == 4;
        assert dto.getOrderCount() == 3;

        System.out.println("테스트 성공: " + dto);
    }
}
