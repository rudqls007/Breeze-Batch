package com.example.kybatch.job.report;

import com.example.kybatch.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

/**
 * STEP 5 - CSV Writer
 * --------------------------------------------------
 * - users-report.csv 파일을 생성한다.
 * - User 엔티티의 출력 필드를 정의하고 CSV 라인으로 변환한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserReportWriter {

    public FlatFileItemWriter<User> writer() {

        FlatFileItemWriter<User> writer = new FlatFileItemWriter<>();
        /* 파일 위치 */
        writer.setResource(new FileSystemResource("output/users-report.csv"));
        /* Job 돌릴 때마다 기존 파일 지우고 새로 갱신 */
        writer.setAppendAllowed(false);

        /* DelimitedLineAggregator : 구분자 역할 "," */
        writer.setLineAggregator(new DelimitedLineAggregator<User>() {{
            setDelimiter(",");

            /* FieldExtractor<User> : User 객체에서 어떤 순서로 뽑을지 정의 */
            setFieldExtractor((FieldExtractor<User>) user -> new Object[]{
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getStatus()
            });
        }});

        return writer;
    }
}
