package com.example.kybatch.domain.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

}
