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
        writer.setResource(new FileSystemResource("output/users-report.csv"));
        writer.setAppendAllowed(false); // 기존 파일 덮어쓰기

        writer.setLineAggregator(new DelimitedLineAggregator<User>() {{
            setDelimiter(",");

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
