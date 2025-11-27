package com.example.kybatch.job.daily;

import com.example.kybatch.domain.stats.DailyStatus;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;

/**
 * JpaItemWriter
 * - DailyStatus 엔티티를 DB에 저장
 * - JPA가 트랜잭션 범위 안에서 INSERT 처리
 */
public class DailyAggregationWriter extends JpaItemWriter<DailyStatus> {

    public DailyAggregationWriter(EntityManagerFactory emf){
        setEntityManagerFactory(emf);
    }

}
