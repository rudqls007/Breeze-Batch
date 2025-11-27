package com.example.kybatch.job.daily;

import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.dto.DailyAggregationDTO;
import org.springframework.batch.item.ItemReader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * DailyAggregationReader
 * - 특정 targetDate에 대해 user_activity를 집계해서
 *   DailyAggregationDTO 리스트를 한 번에 읽어온 뒤
 *   ItemReader 형태로 하나씩 반환
 */
public class DailyAggregationReader implements ItemReader<DailyAggregationDTO> {

    private final Iterator<DailyAggregationDTO> iterator;

    public DailyAggregationReader(LocalDate targetDate,
                                  UserActivityRepository repository) {

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        /* 처음 생성자에서 한 번만 DB를 조회해서 List를 만듦 */
        List<DailyAggregationDTO> result =
                repository.aggregateDaily(targetDate, startOfDay, endOfDay);

        this.iterator = result.iterator();
    }

    /* read() 호출마다 하나씩 꺼내다 -> 다 쓰면 null 리턴 후 종료 */
    @Override
    public DailyAggregationDTO read() {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
