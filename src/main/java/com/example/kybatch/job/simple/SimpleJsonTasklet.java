package com.example.kybatch.job.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Slf4j
@Component
public class SimpleJsonTasklet implements Tasklet {

    private final ObjectMapper objectMapper;

    public SimpleJsonTasklet(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * ===============================================
     * SimpleJsonTasklet
     * -----------------------------------------------
     * [역할]
     * - 간단한 데이터를 메모리에서 생성한 뒤
     * - JSON 파일로 변환하여 저장하는 Tasklet
     *
     * [실행 흐름]
     * 1) 데이터 생성
     * 2) ObjectMapper 로 JSON 문자열 변환
     * 3) 파일 생성
     * 4) RepeatStatus.FINISHED 반환 → Step 종료
     * ===============================================
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info(" STEP 2-2 : Simple JSON Writer Tasklet Started ! ");

        /* 1 - 데이터 생성
        *  Map HashMap 컬렉션을 사용하는 이유 ?
        *  Map으로 타입을 받은 User 객체는 나중에 LinkedHashMap Or TreeMap 구조로
        *  객체를 변경해도 타입을 변경하지 않아도 된다는 장점을 가지고 있음.
        *  Why ?
        *  Map이 모두의 조상이기 때문
        * */
        Map<String, Object> user = new HashMap<>();
        user.put("name", "KyungBin");
        user.put("age", 29);
        user.put("role", "Developer");
        user.put("createdAt", new Date().toString());

        /* Collections.singletonList
        *  user 객체에 할당된 Map Data 들을 단 건으로 묶어서 리스트화 시키기 위해 사용됨.
        *  리스트 자체는 불변하지만, 리스트 안에 담겨진 데이터 자체는 변경 가능함. */
        List<Map<String, Object>> data = Collections.singletonList(user);

        /* 2 - JSON 변환 작업
        * List 타입으로 받은 data를 ObjectMapper를 통해 JSON 형태으 데이터로 변환
        *  */
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(data);

        File output = new File("output/simple-data.json");
        output.getParentFile().mkdirs(); // 디렉토리 없으면 생성
        objectMapper.writeValue(output, data);

        log.info(" JSON 파일 생성 완료 : {}", output.getAbsolutePath());

        return RepeatStatus.FINISHED;
    }
}
