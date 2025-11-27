package com.example.kybatch;

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

        repo.save(new UserActivity(
                null, 1L, 1, 2, 3,
                target.atTime(10, 0),
                48, 11
        ));

        repo.save(new UserActivity(
                null, 1L, 2, 1, 1,
                target.atTime(15, 0),
                48, 11
        ));

        List<DailyAggregationDTO> dto =
                repo.aggregateDaily(
                        target,
                        target.atStartOfDay(),
                        target.plusDays(1).atStartOfDay()
                );

        assertThat(dto).hasSize(1);
        assertThat(dto.get(0).getLoginCount()).isEqualTo(3);
        assertThat(dto.get(0).getViewCount()).isEqualTo(3);
        assertThat(dto.get(0).getOrderCount()).isEqualTo(4);
    }
}
