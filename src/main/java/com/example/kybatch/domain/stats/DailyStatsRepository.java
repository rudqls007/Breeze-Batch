package com.example.kybatch.domain.stats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DailyStatsRepository extends JpaRepository<DailyStatus, Long> {

    void deleteByDate(LocalDate date);
}
