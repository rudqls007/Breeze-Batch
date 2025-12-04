package com.example.kybatch.daily;

import com.example.kybatch.domain.activity.UserActivity;
import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
import com.example.kybatch.service.DailyActivityAggregationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class DailyAggregationLargeTest {

    @Autowired
    UserActivityRepository activityRepo;

    @Autowired
    DailyActivityAggregationService service;

    @Autowired
    DailyStatusRepository dailyRepo;

    @AfterEach
    void tearDown() {
        activityRepo.deleteAll();
        dailyRepo.deleteAll();
    }

    @Test
    void dailyAggregation_largeDataTest() {

        // given
        LocalDate targetDate = LocalDate.of(2025, 11, 26);

        // userId = 1
        activityRepo.save(UserActivity.builder()
                .userId(1L)
                .loginCount(1)
                .viewCount(3)
                .orderCount(2)
                .createdAt(targetDate.atTime(10, 30))
                .build());

        activityRepo.save(UserActivity.builder()
                .userId(1L)
                .loginCount(1)
                .viewCount(2)
                .orderCount(1)
                .createdAt(targetDate.atTime(11, 10))
                .build());

        // userId = 2
        activityRepo.save(UserActivity.builder()
                .userId(2L)
                .loginCount(2)
                .viewCount(4)
                .orderCount(1)
                .createdAt(targetDate.atTime(9, 10))
                .build());

        // 다른 날짜 (집계 제외)
        activityRepo.save(UserActivity.builder()
                .userId(2L)
                .loginCount(10)
                .viewCount(10)
                .orderCount(10)
                .createdAt(LocalDate.of(2025, 11, 25).atTime(8, 00))
                .build());


        // when
        service.aggregateDaily(targetDate);


        // then
        List<DailyStatus> statuses = dailyRepo.findAll();
        assertEquals(2, statuses.size()); // userId 1,2 각각 1건씩

        /* statuses.stream()
        → List<DailyStatus>를 Stream<DailyStatus> 로 변환

        ,filter(s -> s.getUserId() == 1L)
        → userId가 1인 것만 통과시키고 나머지는 버림
        → 결과: “userId가 1인 DailyStatus만 흐르는 스트림”

        .findFirst()
        → 그 중 첫 번째 요소를 꺼냄
        → 결과 타입: Optional<DailyStatus>

        .orElseThrow()
        → 값이 있으면 그걸 꺼내고,
        → 없으면 즉시 예외 던짐 (테스트 실패)

        즉, 이 한 줄이 의미하는 바는:

        “statuses 중에 userId == 1 인 DailyStatus를 하나 찾아서 가져와라.
                        그런 게 하나도 없으면 테스트는 실패해야 한다.” */

        DailyStatus user1 = statuses.stream()
                .filter(s -> s.getUserId() == 1L)
                .findFirst().orElseThrow();

        DailyStatus user2 = statuses.stream()
                .filter(s -> s.getUserId() == 2L)
                .findFirst().orElseThrow();

        // user1 검증
        assertEquals(2, user1.getLoginCount());   // 1+1
        assertEquals(5, user1.getViewCount());    // 3+2
        assertEquals(3, user1.getOrderCount());   // 2+1

        // user2 검증
        assertEquals(2, user2.getLoginCount());
        assertEquals(4, user2.getViewCount());
        assertEquals(1, user2.getOrderCount());
    }

}
