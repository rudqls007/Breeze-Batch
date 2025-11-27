package com.example.kybatch.service;

import com.example.kybatch.domain.activity.UserActivityRepository;
import com.example.kybatch.dto.DailyAggregationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDailyAggregationService {

    private final UserActivityRepository repository;

    public List<DailyAggregationDTO> aggregateDaily(LocalDate date){

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return repository.aggregateDaily(date ,start, end);
    }
}
