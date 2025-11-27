package com.example.kybatch.api;

import com.example.kybatch.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
public class ActivityController {

    private final UserActivityService  userActivityService;

    @PostMapping("/login")
    public void login(@RequestParam Long userId) {
        userActivityService.recordLogin(userId);
    }

    @PostMapping("/view")
    public void view(@RequestParam Long userId) {
        userActivityService.recordView(userId);
    }

    @PostMapping("/order")
    public void order(@RequestParam Long userId) {
        userActivityService.recordOrder(userId);
    }
}
