package com.example.kybatch.domain.lock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchLockRepository extends JpaRepository<BatchLock, String> {

}
