package com.tastyhouse.adminapi.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.admin.request.AdminCreateRequest;
import com.tastyhouse.adminapi.admin.response.AdminCreateResponse;

@Tag(name = "Admin", description = "관리자 계정 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admins")
public class AdminApiController {

    private final AdminAccountService adminAccountService;

    @Operation(summary = "관리자 계정 생성", description = "신규 관리자 계정을 생성합니다. 최고관리자(SUPER_ADMIN)만 호출할 수 있습니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공", content = @Content(schema = @Schema(implementation = AdminCreateResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (SUPER_ADMIN 전용)", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "아이디 중복", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<AdminCreateResponse>> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        Long id = adminAccountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(AdminCreateResponse.from(id)));
    }
}
