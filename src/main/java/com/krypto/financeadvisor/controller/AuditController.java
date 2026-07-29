package com.krypto.financeadvisor.controller;

import com.krypto.financeadvisor.dto.response.ApiResponse;
import com.krypto.financeadvisor.dto.response.AuditLogResponse;
import com.krypto.financeadvisor.service.AuditService;
import com.krypto.financeadvisor.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getMyAuditLogs() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                auditService.getUserAuditLogs(userId)));
    }

    @GetMapping("/entity/{type}/{id}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getEntityHistory(
            @PathVariable String type,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                auditService.getEntityHistory(type, id)));
    }
}
