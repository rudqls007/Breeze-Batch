package com.example.kybatch.daily;

import com.example.kybatch.domain.activity.UserActivity;
import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.dto.DailyAggregationDTO;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;


import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class DailyAggregationJobTest {

    @Autowired
    private UserActivityRepository repo;

    @Autowired
    private EntityManagerFactory emf;


    @Test
    void dailyAggregationTest() {

        LocalDate target = LocalDate.of(2025, 11, 27);

        // userId = 1, 로그인 1, 조회 2, 주문 3
        repo.save(UserActivity.builder()
                .userId(1L)
                .loginCount(1)
                .viewCount(2)
                .orderCount(3)
                .createdAt(target.atTime(10, 0))
                .build()
        );

        // userId = 1, 로그인 2, 조회 1, 주문 1
        repo.save(UserActivity.builder()
                .userId(1L)
                .loginCount(2)
                .viewCount(1)
                .orderCount(1)
                .createdAt(target.atTime(15, 0))
                .build()
        );

        // when
        List<DailyAggregationDTO> dto =
                repo.aggregateDaily(
                        target.atStartOfDay(),
                        target.plusDays(1).atStartOfDay()
                );

        assertThat(dto).hasSize(1);
        assertThat(dto.get(0).getLoginCount()).isEqualTo(3);
        assertThat(dto.get(0).getViewCount()).isEqualTo(3);
        assertThat(dto.get(0).getOrderCount()).isEqualTo(4);
    }
}
