package com.example.kybatch.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Chunk Step에서 JpaPagingItemReader → 내부적으로 JPA를 사용하여 Entity 기반으로 데이터를 가져옴
 * 따라서 JPA Repository가 반드시 필요함.
*/
public interface UserRepository extends JpaRepository<User, Long> {
}
