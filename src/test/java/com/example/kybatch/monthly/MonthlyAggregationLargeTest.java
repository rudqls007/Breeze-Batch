package com.example.kybatch.monthly;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
import com.example.kybatch.domain.stats.MonthlyStatus;
import com.example.kybatch.domain.stats.MonthlyStatusRepository;
import com.example.kybatch.service.MonthlyAggregationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class MonthlyAggregationLargeTest {

    @Autowired
    DailyStatusRepository dailyRepo;

    @Autowired
    MonthlyStatusRepository monthlyRepo;

    @Autowired
    MonthlyAggregationService service;

    @AfterEach
    void clean() {
        dailyRepo.deleteAll();
        monthlyRepo.deleteAll();
    }

    @Test
    void monthlyAggregation_largeTest() {

        // 2025년 11월 데이터
        LocalDate base = LocalDate.of(2025, 11, 10);

        dailyRepo.save(new DailyStatus(1L, base, 1L, 3L, 1L));
        dailyRepo.save(new DailyStatus(1L, base.plusDays(1), 2L, 4L, 3L));
        dailyRepo.save(new DailyStatus(2L, base, 5L, 2L, 1L));

        // 다른 달(집계되면 안 됨)
        dailyRepo.save(new DailyStatus(1L,
                LocalDate.of(2025, 10, 31), 10L, 10L, 10L));

        // when
        service.aggregateMonthly(2025, 11);

        // then
        List<MonthlyStatus> all = monthlyRepo.findAll();
        assertEquals(2, all.size()); // user 1,2 각각

        MonthlyStatus user1 = all.stream()
                .filter(s -> s.getUserId() == 1L)
                .findFirst().orElseThrow();

        assertEquals(3L, user1.getLoginCount()); // 1 + 2
        assertEquals(7L, user1.getViewCount());  // 3 + 4
        assertEquals(4L, user1.getOrderCount()); // 1 + 3
    }
}
