package com.example.kybatch.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchLockService {

    private final JdbcTemplate jdbcTemplate;

    public boolean acquireLock(String lockName) {
        try {
            jdbcTemplate.update("INSERT INTO batch_lock(lock_name) VALUES (?)",
                    lockName); return true;
        } catch (DuplicateKeyException e) {
            return false;
        }

    }

    public void releaseLock(String lockName) {
        jdbcTemplate.update("DELETE FROM batch_lock WHERE lock_name = ?", lockName);
    }
}
