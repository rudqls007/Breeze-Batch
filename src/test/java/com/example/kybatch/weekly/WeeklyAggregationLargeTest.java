package com.example.kybatch.weekly;

import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
import com.example.kybatch.domain.stats.WeeklyStatus;
import com.example.kybatch.domain.stats.WeeklyStatusRepository;
import com.example.kybatch.service.WeeklyAggregationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class WeeklyAggregationLargeTest {

    @Autowired
    DailyStatusRepository dailyRepo;

    @Autowired

    WeeklyStatusRepository weeklyRepo;

    @Autowired
    WeeklyAggregationService service;

    @AfterEach
    void tearDown(){
        weeklyRepo.deleteAll();
        dailyRepo.deleteAll();
    }

    @Test
    void weeklyAggregation_largeDateTest(){

        /* GIVEN */
        int year = 2025;
        int week = 48;


        /* ISO 기준 주차 계산 */
        LocalDate startOfWeek = LocalDate.of(year, 1,4)
                .with(WeekFields.ISO.weekOfWeekBasedYear(), week)
                .with(WeekFields.ISO.dayOfWeek(), 1); /* 월요일 */

        /* 주차별 데이터 추출 */
        LocalDate endOfWeek = startOfWeek.plusDays(7);

        dailyRepo.save(
                DailyStatus.builder()
                        .userId(1L)
                        .date(startOfWeek.plusDays(0))
                        .loginCount(2L)
                        .viewCount(5L)
                        .orderCount(3L)
                        .build()
        );

        dailyRepo.save(
                DailyStatus.builder()
                        .userId(1L)
                        .date(startOfWeek.plusDays(1))
                        .loginCount(1L)
                        .viewCount(2L)
                        .orderCount(1L)
                        .build()
        );

        dailyRepo.save(
                DailyStatus.builder()
                        .userId(2L)
                        .date(startOfWeek.plusDays(3))
                        .loginCount(3L)
                        .viewCount(4L)
                        .orderCount(1L)
                        .build()
        );

        dailyRepo.save(
                DailyStatus.builder()
                        .userId(2L)
                        .date(startOfWeek.minusDays(2))
                        .loginCount(10L)
                        .viewCount(10L)
                        .orderCount(10L)
                        .build()
        );


        System.out.println("### DailyStatus all = " + dailyRepo.findAll());


        /* WHEN */

        service.aggregateWeekly(year, week);

        List<WeeklyStatus> weeklyList = weeklyRepo.findAll();
        System.out.println("### WeeklyStatus all = " + weeklyList);

        /* THEN */

        /* USER 1, USER 2 */
        assertEquals(2, weeklyList.size());

        WeeklyStatus user1 = weeklyList.stream()
                .filter(w -> w.getUserId() == 1L)
                .findFirst().orElseThrow();

        WeeklyStatus user2 = weeklyList.stream()
                .filter(w -> w.getUserId() == 2L)
                .findFirst().orElseThrow();

        /* user 1 합계 */
        assertEquals(3, user1.getLoginCount()); // 2 + 1
        assertEquals(7, user1.getViewCount());  // 5 + 2
        assertEquals(4, user1.getOrderCount()); // 3 + 1

        /* user 2 합계 */
        assertEquals(3, user2.getLoginCount()); // 3
        assertEquals(4, user2.getViewCount());  // 4
        assertEquals(1, user2.getOrderCount()); // 1

        /* 날짜 범위 검증 */
        assertEquals(startOfWeek, user1.getStartDate());
        assertEquals(startOfWeek, user2.getStartDate());

        assertEquals(endOfWeek.minusDays(1), user1.getEndDate());
        assertEquals(endOfWeek.minusDays(1), user2.getEndDate());

        System.out.println("### 테스트 통과!");

    }

}
