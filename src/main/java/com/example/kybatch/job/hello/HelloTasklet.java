package com.example.kybatch.job.hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * ==========================================
 * HelloTasklet (STEP 1)
 * ------------------------------------------
 * [이 클래스의 역할]
 * - 가장 기본적인 배치 실행 단위(Tasklet) 구현체.
 * - "배치가 정상적으로 실행된다"는 것을 확인하기 위해
 *   단순히 로그 하나 찍고 종료하는 역할을 한다.
 *
 * [실행 흐름]
 * 1) helloJob → helloStep 실행
 * 2) helloStep 내부에서 이 Tasklet의 execute()가 호출됨
 * 3) 로그 출력 후 RepeatStatus.FINISHED 반환
 * 4) Step/Job 정상 종료
 * ==========================================
 */
@Slf4j
@Component
public class HelloTasklet implements Tasklet {

    /**
     * Tasklet의 메인 로직.
     *
     * @param contribution 현재 Step에 대한 실행 정보(통계, 상태 등)
     * @param chunkContext Step/Job의 컨텍스트 정보(파라미터 등)
     * @return RepeatStatus.FINISHED → 한 번 실행 후 종료하겠다는 의미
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("🌱 BreezeFlow: STEP 1 - HelloTasklet 실행 완료.");

        // FINISHED를 반환하면 이 STEP은 더 이상 반복 실행되지 않고 종료
        return RepeatStatus.FINISHED;
    }
}
