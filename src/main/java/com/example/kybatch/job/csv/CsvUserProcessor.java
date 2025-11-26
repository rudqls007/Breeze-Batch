package com.example.kybatch.job.csv;

import com.example.kybatch.domain.user.User;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CsvUserProcessor implements ItemProcessor<User, User> {


    @Override
    public User process(User item) throws Exception {
        /* 필요시 검증 / 전처리 추가 기능 */
        return item;
    }
}
