package com.example.kybatch.job.common;

import com.example.kybatch.batch.exception.RetryableBatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * @Slf4j : Lombok이 제공하는 어노테이션으로, log 객체를 자동으로 생성해줌.
 *          log.info(), log.warn(), log.error() 등을 사용할 수 있게 해줌.
 */
@Slf4j
public abstract class AbstractRetryableTasklet implements Tasklet {

    /**
     * 최대 재시도 횟수 상수.
     * RetryableBatchException 발생 시, 이 횟수까지 재시도 후 실패 처리.
     */
    protected static final int MAX_RETRY = 3;

    /**
     * Tasklet 인터페이스의 핵심 메서드.
     * Spring Batch에서 Step 실행 시 호출되는 메서드.
     *
     * @param contribution : Step 실행에 대한 기여도(상태, 카운트 등 기록)
     * @param context      : Chunk 실행 컨텍스트(실행 중인 Step의 상태 정보)
     * @return RepeatStatus : FINISHED 반환 시 Step이 정상 종료됨.
     */
    @Override
    public final RepeatStatus execute(StepContribution contribution,
                                      ChunkContext context) {

        int attempt = 0; // 현재 시도 횟수 기록

        // 무한 루프: 성공하거나 재시도 횟수를 초과할 때까지 반복
        while (true) {
            try {
                attempt++; // 시도 횟수 증가

                // 실제 비즈니스 로직 실행 (하위 클래스에서 구현)
                doExecute(contribution, context);

                // 성공적으로 실행되면 Step 종료
                return RepeatStatus.FINISHED;

            } catch (RetryableBatchException e) {
                // RetryableBatchException 발생 시 재시도 로직 수행

                if (attempt >= MAX_RETRY) {
                    // 최대 재시도 횟수를 초과하면 에러 로그 출력 후 예외 재발생
                    log.error("[Batch] Retry exceeded ({} attempts)", attempt);
                    throw e; // Step 실패 처리
                }

                // 아직 재시도 가능하면 경고 로그 출력 후 루프 계속
                log.warn("[Batch] Retry {}/{} - {}",
                        attempt, MAX_RETRY, e.getMessage());
            }
        }
    }

    /**
     * 추상 메서드: 실제 비즈니스 로직을 하위 Tasklet이 구현해야 함.
     *
     * @param contribution : Step 실행 관련 상태 기록 객체
     * @param context      : Chunk 실행 컨텍스트
     */
    protected abstract void doExecute(
            StepContribution contribution,
            ChunkContext context
    );
}