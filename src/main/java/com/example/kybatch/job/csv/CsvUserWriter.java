package com.example.kybatch.job.csv;

import com.example.kybatch.domain.user.User;
import com.example.kybatch.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * STEP 3 - CSV → DB 저장에서 쓰이는 Writer
 * ----------------------------------------
 * - Reader가 읽어온 User 들을 Chunk 단위로 전달받아서
 *   JPA(UserRepository)를 이용해 DB에 한번에 저장하는 역할.
 */
@Component
@RequiredArgsConstructor
public class CsvUserWriter implements ItemWriter<User> {

    // JPA Repository (User 엔티티 저장 담당)
    private final UserRepository userRepository;

    /**
     * Spring Batch 5 기준 ItemWriter 메서드 시그니처
     * - 예전: write(List<? extends User> items)
     * - 지금: write(Chunk<? extends User> chunk)
     *
     * @param chunk - 이번 트랜잭션에서 처리할 User 목록 (chunk size 만큼 모여 있음)
     */
    @Override
    public void write(Chunk<? extends User> chunk) {
        // Chunk 안에 들어있는 User 리스트 꺼내서 JPA saveAll
        userRepository.saveAll(chunk.getItems());
    }
}
