package com.example.kybatch.job.stats.daily;

import com.example.kybatch.batch.exception.NonCriticalBatchException;
import com.example.kybatch.domain.lock.BatchLockPolicy;
import com.example.kybatch.domain.stats.DailyStatus;
import com.example.kybatch.domain.stats.DailyStatusRepository;
import com.example.kybatch.dto.DailyAggregationDTO;
import com.example.kybatch.job.common.AbstractRetryableTasklet;
import com.example.kybatch.lock.BatchLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStatsAggregationTasklet extends AbstractRetryableTasklet {

    // 일간 통계의 읽기/집계/저장을 담당하는 저장소
    private final DailyStatusRepository dailyRepository;

    // 배치 동시 실행을 제어하기 위한 락 서비스
    private final BatchLockService lockService;

    @Override
    protected void doExecute(StepContribution contribution, ChunkContext context) {

        // 동일한 키("DAILY_STATS")에 대해 배치가 겹쳐 실행되지 않도록 배타적 락을 획득
        boolean locked = lockService.acquireLock(
                "DAILY_STATS",                 // 락 키: 일간 통계 배치 식별자
                BatchLockPolicy.EXCLUSIVE,     // 정책: 단일 실행만 허용
                "DAILY",                       // 락 그룹/카테고리
                "일간 통계 중복 실행 방지"           // 락 목적/설명 (로그/모니터링용)
        );

        // 락 획득 실패 시 비치명적 예외로 종료
        // 주의: AbstractRetryableTasklet은 RetryableBatchException만 재시도 대상으로 처리.
        // NonCriticalBatchException은 재시도 없이 즉시 실패로 종료됨.
        if (!locked) {
            throw new NonCriticalBatchException("Daily Lock 획득 실패");
        }



        try {


            // 🔥 STEP 34 테스트용 강제 실패
            if (true) {
                throw new RuntimeException("daily stats forced fail");
            }
            // 집계 대상 날짜를 '어제'로 설정 (당일은 데이터가 아직 변동 중일 수 있음)
            LocalDate targetDate = LocalDate.now().minusDays(1);

            // 집계 구간: [어제 00:00, 오늘 00:00) — 즉 어제 하루
            LocalDateTime start = targetDate.atStartOfDay();
            LocalDateTime end   = targetDate.plusDays(1).atStartOfDay();

            // 같은 날짜의 기존 결과가 있다면 삭제하여 멱등성 보장
            // (배치 재실행 시 중복 저장 방지)
            dailyRepository.deleteByDate(targetDate);

            // 원천 데이터에서 어제 하루에 대한 사용자별 집계 결과 생성
            List<DailyAggregationDTO> aggregates = dailyRepository.aggregateDaily(start, end);

            // 집계 결과가 비어있으면 비치명적 예외로 종료 (데이터 부재 상황)
            if (aggregates.isEmpty()) {
                throw new NonCriticalBatchException("Daily 집계 결과 없음");
            }

            // 집계된 각 사용자별 결과를 일간 통계 테이블에 저장
            for (DailyAggregationDTO dto : aggregates) {
                dailyRepository.save(
                        DailyStatus.builder()
                                .userId(dto.getUserId())
                                .date(targetDate)                  // 통계 기준일(어제)
                                .loginCount(dto.getLoginCount())   // 로그인 횟수
                                .viewCount(dto.getViewCount())     // 조회(페이지/콘텐츠) 횟수
                                .orderCount(dto.getOrderCount())   // 주문 횟수
                                .build()
                );
            }

        } finally {
            // 정상/예외 상관없이 락은 반드시 해제 (deadlock, 장기 점유 방지)
            lockService.releaseLock("DAILY_STATS");
        }
    }
}