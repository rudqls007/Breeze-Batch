package com.example.kybatch.admin.controller;

import com.example.kybatch.admin.dto.BatchRestartRequest;
import com.example.kybatch.admin.dto.BatchRestartResponse;
import com.example.kybatch.admin.service.BatchRestartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/batch")
@RequiredArgsConstructor
public class BatchAdminController {

    private final BatchRestartService batchRestartService;

    /**
     * 운영자 수동 배치 재실행 API
     *
     * - 실패한 JobExecutionId 기준
     * - 재실행 사유 필수
     * - force 옵션으로 가드 우회 가능
     */
    @PostMapping("/restart")
    public ResponseEntity<BatchRestartResponse> restart(
            @RequestBody BatchRestartRequest request
    ) {
        Long originJobExecutionId = request.getJobExecutionId(); // 🔧 변경

        Long newJobExecutionId = batchRestartService.restart(originJobExecutionId); // 🔧 변경

        return ResponseEntity.ok(
                new BatchRestartResponse(
                        originJobExecutionId,   // 🔧 변경: 원본 실행 ID
                        newJobExecutionId       // 🔧 변경: 새로 생성된 실행 ID
                )
        );
    }
}