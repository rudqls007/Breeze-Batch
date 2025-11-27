package com.example.kybatch.service;

import com.example.kybatch.domain.activity.UserActivity;
import com.example.kybatch.domain.activity.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserActivityRepository repository;

    public void recordLogin(Long userId){
        saveActivity(userId, 1, 0, 0);
    }

    public void recordView(Long userId){
        saveActivity(userId, 0, 1, 0);
    }

    public void recordOrder(Long userId){
        saveActivity(userId, 0, 0, 1);
    }

    /**
     *  공통 처리 로직
     * */
    private void saveActivity(Long userId, int login, int view, int order) {
        LocalDateTime now = LocalDateTime.now();

        UserActivity activity = UserActivity.builder()
                .userId(userId)
                .loginCount(login)
                .viewCount(view)
                .orderCount(order)
                .weekOfYear(now.get(WeekFields.ISO.weekOfWeekBasedYear()))
                .month(now.getMonthValue())
                .createdAt(now)
                .build();

        repository.save(activity);


    }


}
