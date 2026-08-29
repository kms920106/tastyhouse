package com.tastyhouse.adminapi.admin.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.admin.adapter.in.web.request.AdminCreateRequest;
import com.tastyhouse.adminapi.admin.application.port.in.AdminCommandUseCase;
import com.tastyhouse.adminapi.admin.application.port.in.AdminCreateCommand;

@Tag(name = "Admin", description = "관리자 계정 관리 API")
@RestController
@RequestMapping("/api/admins")
public class AdminApiController {

    private final AdminCommandUseCase adminCommandUseCase;

    public AdminApiController(AdminCommandUseCase adminCommandUseCase) {
        this.adminCommandUseCase = adminCommandUseCase;
    }

    @Operation(summary = "관리자 계정 생성", description = "신규 관리자 계정을 생성합니다. 최고관리자(SUPER_ADMIN)만 호출할 수 있습니다. 생성된 관리자 ID를 반환합니다.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        AdminCreateCommand command = request.toCommand();
        Long id = adminCommandUseCase.createAdmin(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(id));
    }
}
