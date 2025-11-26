package com.example.kybatch.job.csv;

import com.example.kybatch.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * STEP 4 - 데이터 정제 / 필터링 / 가공 Processor
 * -----------------------------------------------
 * - Reader → Processor → Writer 흐름 중 Processor는
 *   한 건씩 데이터를 검증하고 가공하는 단계.
 *
 * - return null → Writer로 전달되지 않음 (필터링 처리)
 */
@Slf4j
@Component
public class CsvUserProcessor implements ItemProcessor<User, User> {


    @Override
    public User process(User user) throws Exception {

        log.info("Processing User: {}", user.getEmail());

        /* 1 - 이메일 형식 검증 (간단 버전) */
        if (!user.getEmail().contains("@")){
            log.warn("Invalid email, filtered: {}", user.getEmail());
            return null; // Writer로 전달되지 않음.
        }

        /* 2 - 상태값 강제 변환 (정제) */
        if (user.getStatus() == null){
            user.updateStatus("ACTIVE");
        }

        /* 3 - 이름 정제 (트림) */
        user.updateName(user.getName().trim());

        /* 정상 데이터만 Writer로 전달 */
        return user;
    }
}
