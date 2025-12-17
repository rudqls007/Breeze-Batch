package com.example.kybatch.admin.controller;

import com.example.kybatch.admin.dto.BatchRestartRequest;
import com.example.kybatch.admin.service.BatchRestartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/batch")
@RequiredArgsConstructor
public class BatchAdminController {

    private final BatchRestartService batchRestartService;

    @PostMapping("/restart")
    public ResponseEntity<Void> restart(@RequestBody BatchRestartRequest request) {
        batchRestartService.restart(request);
        return ResponseEntity.ok().build();
    }
}
